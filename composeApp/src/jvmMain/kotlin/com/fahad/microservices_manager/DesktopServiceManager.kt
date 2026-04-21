package com.fahad.microservices_manager

import com.fahad.microservices_manager.domain.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import java.awt.Desktop
import java.io.File
import java.time.LocalTime
import java.util.concurrent.ConcurrentHashMap
import kotlin.random.Random

class DesktopServiceManager : ServiceManager {
    private val runningProcesses = ConcurrentHashMap<String, Process>()
    private val lastStoppedAt = ConcurrentHashMap<String, Long>()
    private val lastCpuCheck = ConcurrentHashMap<String, Pair<Long, Long>>() // serviceId to (timestamp, cpuTime)
    private val _serviceUpdates = MutableSharedFlow<Service>(extraBufferCapacity = 64)
    override val serviceUpdates: SharedFlow<Service> = _serviceUpdates
    private val _logStream = MutableSharedFlow<Pair<String, LogEntry>>(extraBufferCapacity = 512)
    override val logStream: SharedFlow<Pair<String, LogEntry>> = _logStream
    private val scope = CoroutineScope(Dispatchers.IO)

    override fun isPortInUse(port: Int): Boolean {
        if (port <= 0) return false
        return try {
            java.net.ServerSocket(port).use { false }
        } catch (e: java.io.IOException) {
            true
        }
    }

    override fun startService(service: Service) {
        scope.launch {
            try {
                // Stop if already running
                runningProcesses[service.id]?.let { proc ->
                    proc.destroyForcibly()
                    runningProcesses.remove(service.id)
                }

                // Check if port is in use by someone else (only if we know it's not a dynamic port)
                // We use 0 as 'Auto' port. 8080 is often a default that might be overridden.
                if (service.port != 0 && service.port != 8080 && isPortInUse(service.port)) {
                    _serviceUpdates.emit(service.copy(status = ServiceStatus.ERROR, cpu = 0f, heapMb = 0, healthPercent = 0))
                    emitLog(service.id, LogLevel.ERROR, "Port ${service.port} is already in use by another process")
                    return@launch
                }

                _serviceUpdates.emit(service.copy(status = ServiceStatus.STARTING, cpu = 0f, heapMb = 0, healthPercent = 0))

                // Emit startup log
                emitLog(service.id, LogLevel.INFO, "Starting ${service.name}…")

                val command = getRunCommand(service)
                val process = ProcessBuilder(*command.toTypedArray())
                    .directory(File(service.path).let { if (it.isDirectory) it else it.parentFile })
                    .redirectErrorStream(true)
                    .start()

                runningProcesses[service.id] = process

                // Stream logs from process stdout/stderr
                scope.launch(Dispatchers.IO) {
                    try {
                        process.inputStream.bufferedReader().use { reader ->
                            reader.forEachLine { line ->
                                if (line.isNotBlank()) {
                                    val entry = parseLogLine(service.id, line)
                                    scope.launch { _logStream.emit(Pair(service.id, entry)) }
                                }
                            }
                        }
                    } catch (_: Exception) {}
                }

                // Wait for the port to become active (service ready) OR for process to exit
                var isStarted = false
                repeat(45) { // Try for 45 seconds
                    if (!process.isAlive) return@repeat
                    if (service.port > 0 && isPortInUse(service.port)) {
                        isStarted = true
                        return@repeat
                    }
                    delay(1000)
                }

                if (process.isAlive) {
                    val runningService = service.copy(
                        status = ServiceStatus.RUNNING,
                        cpu = 0f,
                        heapMb = 0,
                        healthPercent = if (isStarted || service.port > 0) 100 else 70
                    )
                    _serviceUpdates.emit(runningService)
                    
                    if (isStarted) {
                        emitLog(service.id, LogLevel.INFO, "Service ${service.name} bound to port ${service.port} successfully")
                    } else if (service.port == 0) {
                        emitLog(service.id, LogLevel.WARN, "Service ${service.name} started, waiting for runtime port detection from logs…")
                    }

                    // Real metrics monitoring loop
                    scope.launch {
                        while (runningProcesses[service.id]?.isAlive == true) {
                            val proc = runningProcesses[service.id]!!
                            val handle = proc.toHandle()
                            val info = handle.info()
                            
                            // Resident Set Size (RSS) - available since Java 10
                            // Fallback if not available
                            val memoryMb = try {
                                // Using reflection to be safe or just standard long check
                                handle.info().totalCpuDuration().map { 
                                    // This is just a placeholder to check if we can access info
                                    // Real way for Java 10+ is info.residentUsedBytes()
                                    // But since we are on JDK 17, let's use the proper one with a cast if needed
                                    // or just use totalCpuDuration to verify info is there.
                                    0L
                                }
                                // Re-implementing with proper JDK 17 check
                                (handle.info().javaClass.getMethod("residentUsedBytes").invoke(handle.info()) as java.util.Optional<Long>).orElse(-1024L) / 1024 / 1024
                            } catch (e: Exception) {
                                -1L
                            }
                            
                            val currentCpuTime = info.totalCpuDuration().map { it.toMillis() }.orElse(-1L)
                            val currentTime = System.currentTimeMillis()
                            val last = lastCpuCheck[service.id]
                            
                            var cpuPercent = 0f
                            if (last != null && currentCpuTime != -1L) {
                                val deltaTime = currentTime - last.first
                                val deltaCpu = currentCpuTime - last.second
                                if (deltaTime > 0) {
                                    cpuPercent = (deltaCpu.toFloat() / deltaTime.toFloat() * 100f)
                                }
                            }
                            if (currentCpuTime != -1L) {
                                lastCpuCheck[service.id] = currentTime to currentCpuTime
                            }

                            val finalCpu = if (currentCpuTime == -1L) -1f else cpuPercent.coerceIn(0f, 1000f)
                            val finalMemory = if (memoryMb < 0) -1 else memoryMb.toInt()

                            _serviceUpdates.emit(Service(
                                id = service.id,
                                name = "", path = "", port = 0,
                                status = ServiceStatus.RUNNING,
                                cpu = finalCpu,
                                heapMb = finalMemory,
                                healthPercent = 100
                            ))
                            delay(3000)
                        }
                        lastCpuCheck.remove(service.id)
                    }

                    // Wait for process to end
                    Thread {
                        try { process.waitFor() } catch (_: InterruptedException) {}
                        runningProcesses.remove(service.id)
                        scope.launch {
                            _serviceUpdates.emit(service.copy(
                                status = ServiceStatus.STOPPED,
                                cpu = 0f, heapMb = 0, healthPercent = 0
                            ))
                            emitLog(service.id, LogLevel.WARN, "Service ${service.name} process exited")
                        }
                    }.start()
                } else {
                    runningProcesses.remove(service.id)
                    _serviceUpdates.emit(service.copy(status = ServiceStatus.ERROR, cpu = 0f, heapMb = 0, healthPercent = 0))
                    emitLog(service.id, LogLevel.ERROR, "Service ${service.name} failed to start (process exited immediately)")
                }
            } catch (e: Exception) {
                _serviceUpdates.emit(service.copy(status = ServiceStatus.ERROR, cpu = 0f, heapMb = 0, healthPercent = 0))
                emitLog(service.id, LogLevel.ERROR, "Error starting ${service.name}: ${e.message}")
            }
        }
    }

    private suspend fun emitLog(serviceId: String, level: LogLevel, message: String) {
        val now = LocalTime.now()
        val ts = String.format("%02d:%02d:%02d.%03d", now.hour, now.minute, now.second, now.nano / 1_000_000)
        _logStream.emit(Pair(serviceId, LogEntry(timestamp = ts, level = level, message = message)))
    }

    private fun stripAnsiCodes(text: String): String {
        return text.replace(Regex("\u001B\\[[;\\d]*m"), "")
            .replace(Regex("\u001B\\[[;\\d]*[A-Za-z]"), "")
    }

    private fun parseLogLine(serviceId: String, line: String): LogEntry {
        val cleanLine = stripAnsiCodes(line)
        
        // --- Runtime Port Conflict Detection ---
        val conflictRegex = Regex("(?:port|port\\(s\\):|on port)\\s*(\\d+)")
        val isConflictMessage = cleanLine.contains("already in use", ignoreCase = true) || 
                               cleanLine.contains("listening on port", ignoreCase = true) ||
                               cleanLine.contains("Web server failed to start", ignoreCase = true)

        if (isConflictMessage) {
            val detectedPort = conflictRegex.find(cleanLine)?.groupValues?.get(1)?.toIntOrNull()
            scope.launch {
                _serviceUpdates.emit(
                    Service(
                        id = serviceId,
                        name = "", path = "", 
                        port = detectedPort ?: 0,
                        status = ServiceStatus.ERROR,
                        healthPercent = 0
                    )
                )
                emitLog(serviceId, LogLevel.ERROR, "Port conflict detected: $cleanLine")
            }
        }

        // --- Runtime Port Detection ---
        val portRegex = Regex("(?:port\\(s\\):|port|Listening on:.*?[:])\\s*(\\d+)")
        if (cleanLine.contains("Started", ignoreCase = true) || cleanLine.contains("Listening", ignoreCase = true)) {
            portRegex.find(cleanLine)?.groupValues?.get(1)?.toIntOrNull()?.let { detectedPort ->
                scope.launch {
                    _serviceUpdates.emit(
                        Service(
                            id = serviceId,
                            name = "", path = "", 
                            port = detectedPort,
                            status = ServiceStatus.RUNNING,
                            healthPercent = 100
                        )
                    )
                    emitLog(serviceId, LogLevel.INFO, "Runtime port detection: Detected port $detectedPort")
                }
            }
        }

        val now = LocalTime.now()
        val ts = String.format("%02d:%02d:%02d.%03d", now.hour, now.minute, now.second, now.nano / 1_000_000)
        val level = when {
            cleanLine.contains("ERROR") || cleanLine.contains("Exception") || cleanLine.contains("FATAL") -> LogLevel.ERROR
            cleanLine.contains("WARN") || cleanLine.contains("WARNING") -> LogLevel.WARN
            cleanLine.contains("DEBUG") || cleanLine.contains("TRACE") -> LogLevel.DEBUG
            else -> LogLevel.INFO
        }
        return LogEntry(timestamp = ts, level = level, message = cleanLine)
    }

    private fun getRunCommand(service: Service): List<String> {
        val path = service.path
        val f = File(path)
        return when {
            f.isFile && f.extension == "jar" -> buildList {
                add("java")
                if (service.jvmArgs.isNotBlank()) addAll(service.jvmArgs.split(" "))
                add("-jar"); add(path)
            }
            service.framework.lowercase().contains("maven") || File(path, "pom.xml").exists() -> {
                val mvnw = if (isWindows()) "mvnw.cmd" else "./mvnw"
                if (File(path, mvnw.removePrefix("./")).exists()) {
                    if (isWindows()) listOf("cmd", "/c", mvnw, "spring-boot:run")
                    else listOf("sh", mvnw, "spring-boot:run")
                }
                else listOf("mvn", "spring-boot:run")
            }
            service.framework.lowercase().contains("gradle") ||
                    File(path, "build.gradle").exists() ||
                    File(path, "build.gradle.kts").exists() -> {
                val gw = if (isWindows()) "gradlew.bat" else "./gradlew"
                if (File(path, gw.removePrefix("./")).exists()) {
                    if (isWindows()) listOf("cmd", "/c", gw, "bootRun")
                    else listOf("sh", gw, "bootRun")
                }
                else listOf("gradle", "bootRun")
            }
            else -> listOf("java", "-jar", path)
        }
    }

    private fun isWindows() = System.getProperty("os.name").lowercase().contains("win")

    override fun stopService(service: Service) {
        scope.launch {
            runningProcesses[service.id]?.let { process ->
                process.destroy()
                var stopped = false
                repeat(20) {
                    if (!process.isAlive) {
                        stopped = true
                        return@repeat
                    }
                    delay(100)
                }
                if (!stopped) process.destroyForcibly()
                runningProcesses.remove(service.id)
            }
            lastStoppedAt[service.id] = System.currentTimeMillis()
            _serviceUpdates.emit(service.copy(status = ServiceStatus.STOPPED, cpu = 0f, heapMb = 0, healthPercent = 0))
            emitLog(service.id, LogLevel.INFO, "Service ${service.name} stopped")
        }
    }

    override fun restartService(service: Service) {
        stopService(service)
        scope.launch {
            delay(500)
            startService(service)
        }
    }

    override fun buildService(service: Service) {}
    override fun cleanService(service: Service) {}
    override fun getLogs(service: Service): String = ""

    override fun stopAll() {
        runningProcesses.values.forEach { it.destroy() }
        try {
            val start = System.currentTimeMillis()
            while (runningProcesses.values.any { it.isAlive } && (System.currentTimeMillis() - start < 2000)) {
                Thread.sleep(100)
            }
        } catch (_: Exception) {}
        runningProcesses.values.forEach { if (it.isAlive) it.destroyForcibly() }
        runningProcesses.clear()
    }

    override fun killProcessOnPort(port: Int): Boolean {
        if (port <= 0) return false
        return try {
            if (isWindows()) {
                val output = Runtime.getRuntime().exec("cmd /c netstat -aon | findstr :$port").inputStream.bufferedReader().readText()
                val lines = output.lines().filter { it.contains("LISTENING") }
                var killedAny = false
                lines.forEach { line ->
                    val pid = line.trim().split(Regex("\\s+")).lastOrNull()
                    if (pid != null && pid.all { it.isDigit() }) {
                        Runtime.getRuntime().exec("taskkill /F /PID $pid").waitFor()
                        killedAny = true
                    }
                }
                killedAny
            } else {
                val lsof = Runtime.getRuntime().exec(arrayOf("sh", "-c", "lsof -t -i :$port"))
                lsof.waitFor()
                val pids = lsof.inputStream.bufferedReader().readText().trim().split("\n").filter { it.isNotBlank() }
                if (pids.isNotEmpty()) {
                    pids.forEach { pid -> Runtime.getRuntime().exec(arrayOf("sh", "-c", "kill -9 $pid")).waitFor() }
                    true
                } else {
                    val fuser = Runtime.getRuntime().exec(arrayOf("sh", "-c", "fuser -k $port/tcp"))
                    fuser.waitFor()
                    fuser.exitValue() == 0
                }
            }
        } catch (e: Exception) { false }
    }

    private fun createServiceFromDir(dir: File): Service? {
        val hasPom = File(dir, "pom.xml").exists()
        val hasGradle = File(dir, "build.gradle").exists() || File(dir, "build.gradle.kts").exists()
        if (!hasPom && !hasGradle) return null
        val srcDir = File(dir, "src/main")
        if (!srcDir.exists() || (!File(srcDir, "java").exists() && !File(srcDir, "kotlin").exists())) return null
        
        var isRunnable = false
        if (hasPom) {
            val content = File(dir, "pom.xml").readText()
            isRunnable = content.contains("spring-boot") || content.contains("quarkus") || content.contains("micronaut")
        } else {
            val buildFile = File(dir, "build.gradle").takeIf { it.exists() } ?: File(dir, "build.gradle.kts")
            val content = buildFile.readText()
            isRunnable = content.contains("spring-boot") || content.contains("quarkus") || content.contains("micronaut")
        }

        if (isRunnable) {
            val service = Service(
                id = java.util.UUID.randomUUID().toString(),
                name = dir.name,
                path = dir.absolutePath,
                status = ServiceStatus.STOPPED,
                port = 0
            )
            return scanMetadata(service)
        }
        return null
    }

    override fun detectServices(rootPath: String): List<Service> {
        val root = File(rootPath)
        if (!root.isDirectory) return emptyList()
        val discovered = mutableListOf<Service>()
        createServiceFromDir(root)?.let { discovered.add(it) }
        root.listFiles()?.filter { it.isDirectory }?.forEach { subDir ->
            createServiceFromDir(subDir)?.let { service ->
                if (discovered.none { it.path == service.path }) discovered.add(service)
            }
        }
        return discovered
    }

    override fun discoverRunningServices(services: List<Service>): List<Service> {
        val updated = services.toMutableList()
        val now = System.currentTimeMillis()

        ProcessHandle.allProcesses().forEach { handle ->
            val info = handle.info()
            val command = info.command().orElse("")
            val arguments = info.arguments().orElse(emptyArray()).joinToString(" ")
            val fullPath = "$command $arguments"

            // Skip processes that are definitely not our microservices (like the manager itself)
            if (command.contains("microservices_manager") || command.contains("java") && arguments.contains("DevPilot")) {
                return@forEach
            }

            services.forEachIndexed { index, service ->
                // Stricter matching: must contain path OR be a Java process with the service name in args
                val pathMatch = service.path.isNotBlank() && fullPath.contains(service.path)
                val nameMatch = service.name.isNotBlank() && command.contains("java") && arguments.contains(service.name)
                
                if (pathMatch || nameMatch) {
                    // Check cooldown: if we just stopped this service, don't rediscover it for 5s
                    val stoppedAt = lastStoppedAt[service.id] ?: 0L
                    if (now - stoppedAt < 5000) {
                        return@forEachIndexed
                    }

                    if (service.status == ServiceStatus.STOPPED) {
                        val port = findPortForPid(handle.pid()) ?: service.port
                        updated[index] = service.copy(
                            status = ServiceStatus.RUNNING,
                            port = port,
                            healthPercent = 90
                        )
                        runningProcesses[service.id] = recoverProcess(handle)
                    }
                }
            }
        }
        return updated
    }

    private fun recoverProcess(handle: ProcessHandle): Process {
        return object : Process() {
            override fun destroy() { handle.destroy() }
            override fun destroyForcibly(): Process { handle.destroyForcibly(); return this }
            override fun exitValue(): Int = if (handle.isAlive) throw IllegalThreadStateException() else 0
            override fun waitFor(): Int { while (handle.isAlive) Thread.sleep(100); return 0 }
            override fun isAlive(): Boolean = handle.isAlive
            override fun getInputStream(): java.io.InputStream = java.io.ByteArrayInputStream(byteArrayOf())
            override fun getErrorStream(): java.io.InputStream = java.io.ByteArrayInputStream(byteArrayOf())
            override fun getOutputStream(): java.io.OutputStream = java.io.ByteArrayOutputStream()
        }
    }

    private fun findPortForPid(pid: Long): Int? {
        return try {
            if (isWindows()) {
                val output = Runtime.getRuntime().exec("cmd /c netstat -aon | findstr $pid").inputStream.bufferedReader().readText()
                output.lines().forEach { line ->
                    if (line.contains("LISTENING")) {
                        val parts = line.trim().split(Regex("\\s+"))
                        if (parts.size >= 2) {
                            val addr = parts[1]
                            val port = addr.substringAfterLast(":").toIntOrNull()
                            if (port != null) return port
                        }
                    }
                }
            } else {
                val output = Runtime.getRuntime().exec(arrayOf("sh", "-c", "lsof -nP -p $pid | grep LISTEN")).inputStream.bufferedReader().readText()
                val portRegex = Regex(":(\\d+)\\s+\\(LISTEN\\)")
                portRegex.find(output)?.groupValues?.get(1)?.toIntOrNull()
            }
            null
        } catch (_: Exception) { null }
    }

    override fun openFolder(service: Service) {
        try {
            val file = File(service.path)
            val target = if (file.isDirectory) file else file.parentFile
            if (target.exists() && Desktop.isDesktopSupported()) Desktop.getDesktop().open(target)
        } catch (_: Exception) {}
    }

    override fun scanMetadata(service: Service): Service {
        val dir = File(service.path)
        if (!dir.isDirectory) return service
        var port = service.port
        var name = service.name
        var framework = service.framework
        var version = service.version
        val pomFile = File(dir, "pom.xml")
        val buildGradle = File(dir, "build.gradle").takeIf { it.exists() } ?: File(dir, "build.gradle.kts").takeIf { it.exists() }

        if (pomFile.exists()) {
            val content = pomFile.readText()
            framework = when {
                content.contains("spring-boot") -> "Spring Boot"
                content.contains("quarkus") -> "Quarkus"
                content.contains("micronaut") -> "Micronaut"
                else -> "Maven"
            }
            val versionRegex = Regex("<spring-boot.version>(.*?)</spring-boot.version>")
            versionRegex.find(content)?.groupValues?.get(1)?.let { version = it }
            if (version.isBlank()) {
                val parentVersionRegex = Regex("<parent>[\\s\\S]*?<version>(.*?)</version>", RegexOption.MULTILINE)
                parentVersionRegex.find(content)?.groupValues?.get(1)?.let { version = it }
            }
        } else if (buildGradle != null) {
            val content = buildGradle.readText()
            framework = when {
                content.contains("spring-boot") -> "Spring Boot"
                content.contains("quarkus") -> "Quarkus"
                content.contains("micronaut") -> "Micronaut"
                else -> "Gradle"
            }
        }

        val resourcesDir = File(dir, "src/main/resources")
        if (resourcesDir.exists()) {
            val configFiles = listOf("application-dev.yml", "application-dev.yaml", "application-dev.properties", "application-local.yml", "application-local.yaml", "application-local.properties", "application.yml", "application.yaml", "application.properties")
            for (fileName in configFiles) {
                val file = File(resourcesDir, fileName)
                if (file.exists()) {
                    val map = if (fileName.endsWith(".properties")) parseProperties(file) else parseYaml(file)
                    if (name.isBlank() || name == dir.name) name = map["spring.application.name"] ?: map["application.name"] ?: name
                    if (port == 0) port = map["server.port"]?.toIntOrNull() ?: port
                    if (name.isNotBlank() && name != dir.name && port != 0) break
                }
            }
        }
        return service.copy(name = name, port = port, framework = framework, version = version)
    }

    private fun parseProperties(file: File): Map<String, String> {
        val map = mutableMapOf<String, String>()
        file.readLines().forEach { line ->
            val trimmed = line.trim()
            if (trimmed.isNotBlank() && !trimmed.startsWith("#")) {
                val parts = trimmed.split("=", limit = 2)
                if (parts.size == 2) map[parts[0].trim()] = parts[1].trim().removeSurrounding("\"").removeSurrounding("'")
            }
        }
        return map
    }

    private fun parseYaml(file: File): Map<String, String> {
        val map = mutableMapOf<String, String>()
        val stack = mutableListOf<String>()
        file.readLines().forEach { line ->
            val indent = line.takeWhile { it == ' ' }.length / 2
            val trimmed = line.trim()
            if (trimmed.isNotBlank() && !trimmed.startsWith("#") && trimmed.contains(":")) {
                val parts = trimmed.split(":", limit = 2)
                val key = parts[0].trim()
                val value = parts[1].trim().removeSurrounding("\"").removeSurrounding("'")
                while (stack.size > indent) stack.removeAt(stack.size - 1)
                if (value.isNotBlank()) map[(stack + key).joinToString(".")] = value else stack.add(key)
            }
        }
        return map
    }
}
