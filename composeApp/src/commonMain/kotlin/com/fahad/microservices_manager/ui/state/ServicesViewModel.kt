package com.fahad.microservices_manager.ui.state

import com.fahad.microservices_manager.domain.*
import com.fahad.microservices_manager.data.ServiceStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID

class ServicesViewModel(
    private val manager: ServiceManager,
    private val storage: ServiceStorage,
    private val scope: CoroutineScope
) {
    private val _state = MutableStateFlow(AppState())
    val state: StateFlow<AppState> = _state.asStateFlow()

    init {
        loadData()
        observeServiceUpdates()
        observeLogs()
        startDiscoveryLoop()
    }

    private fun loadData() {
        val loadedServices = storage.loadServices()
        val loadedProjects = storage.loadProjects()
        
        _state.update {
            it.copy(
                services = loadedServices,
                projects = loadedProjects,
                selectedProjectId = loadedProjects.firstOrNull()?.id
            )
        }
        
        // Initial discovery
        discoverRunningServices()
    }

    private fun startDiscoveryLoop() {
        scope.launch {
            while (true) {
                delay(15000) // Check every 15 seconds
                discoverRunningServices()
            }
        }
    }

    private fun discoverRunningServices() {
        val currentServices = _state.value.services
        if (currentServices.isEmpty()) return
        
        val discovered = manager.discoverRunningServices(currentServices)
        
        // Only update if something changed
        if (discovered != currentServices) {
            _state.update { it.copy(services = discovered) }
        }
    }

    private fun observeServiceUpdates() {
        scope.launch {
            manager.serviceUpdates.collect { updatedService ->
                _state.update { s ->
                    val nextServices = s.services.map { existing -> 
                        if (existing.id == updatedService.id) {
                            // ... existing merge logic ...
                            val merged = existing.copy(
                                port = if (updatedService.port > 0) updatedService.port else existing.port,
                                status = updatedService.status,
                                cpu = if (updatedService.cpu > 0) updatedService.cpu else existing.cpu,
                                heapMb = if (updatedService.heapMb > 0) updatedService.heapMb else existing.heapMb,
                                healthPercent = if (updatedService.healthPercent > 0) updatedService.healthPercent else existing.healthPercent,
                                framework = if (updatedService.framework.isNotBlank()) updatedService.framework else existing.framework,
                                version = if (updatedService.version.isNotBlank()) updatedService.version else existing.version
                            )
                            if (updatedService.status == ServiceStatus.ERROR) {
                                val serviceLogs = s.logs[updatedService.id] ?: emptyList()
                                val isConflict = serviceLogs.any { 
                                    it.message.contains("already in use", ignoreCase = true) ||
                                    it.message.contains("listening on port", ignoreCase = true) ||
                                    it.message.contains("Web server failed to start", ignoreCase = true)
                                }
                                if (isConflict) {
                                    scope.launch {
                                        _state.update { it.copy(portConflictService = merged) }
                                    }
                                }
                            }
                            merged
                        } else existing 
                    }
                    
                    // If port changed, persist it
                    val portChanged = nextServices.any { next -> 
                        val prev = s.services.find { it.id == next.id }
                        prev != null && prev.port != next.port && next.port > 0
                    }
                    if (portChanged) {
                        storage.saveServices(nextServices)
                    }
                    
                    s.copy(services = nextServices)
                }
            }
        }
    }

    private fun observeLogs() {
        scope.launch {
            manager.logStream.collect { (serviceId, entry) ->
                _state.update { s ->
                    val existing = s.logs[serviceId] ?: emptyList()
                    val trimmed = if (existing.size >= 500) existing.drop(200) else existing
                    s.copy(logs = s.logs + (serviceId to (trimmed + entry)))
                }
            }
        }
    }

    fun startService(service: Service) {
        // 1. Check if port is in use by another managed service (only if port is not default/auto)
        if (service.port != 0 && service.port != 8080) {
            val conflict = _state.value.services.find { it.id != service.id && it.port == service.port && it.status == ServiceStatus.RUNNING }
            if (conflict != null) {
                showToast("Port ${service.port} is already used by ${conflict.name}", ToastType.ERROR)
                return
            }

            // 2. Check if port is in use at OS level
            if (manager.isPortInUse(service.port)) {
                _state.update { it.copy(portConflictService = service) }
                return
            }
        }
        
        // 3. Start normally - if it's 8080 or 0, we'll find out the real port from logs
        manager.startService(service)
        showToast("Starting ${service.name}…", ToastType.INFO)
        
        // The observeServiceUpdates loop handles the rest (Success or Runtime Conflict)
    }

    fun resolvePortConflict(service: Service) {
        _state.update { it.copy(portConflictService = null) }
        scope.launch {
            showToast("Killing process on port ${service.port}…", ToastType.WARNING)
            val killed = manager.killProcessOnPort(service.port)
            if (killed) {
                delay(1000)
                startService(service)
            } else {
                showToast("Failed to kill process on port ${service.port}", ToastType.ERROR)
            }
        }
    }

    fun cancelPortConflict() {
        _state.update { it.copy(portConflictService = null) }
    }

    fun stopService(service: Service) {
        _state.update { s ->
            s.copy(services = s.services.map { 
                if (it.id == service.id) it.copy(status = ServiceStatus.STOPPING) else it 
            })
        }
        manager.stopService(service)
        showToast("Stopping ${service.name}…", ToastType.INFO)
    }

    fun restartService(service: Service) {
        manager.restartService(service)
        showToast("Restarting ${service.name}…", ToastType.WARNING)
    }

    fun selectService(serviceId: String) {
        _state.update {
            it.copy(
                selectedServiceId = serviceId,
                logServiceId = serviceId,
                isLogPanelExpanded = true
            )
        }
    }

    fun openDrawer(serviceId: String) {
        _state.update { it.copy(isDrawerOpen = true, drawerServiceId = serviceId) }
    }

    fun closeDrawer() {
        _state.update { it.copy(isDrawerOpen = false, drawerServiceId = null) }
    }

    fun onSearchQueryChanged(query: String) {
        _state.update { it.copy(searchQuery = query) }
    }

    fun setStatusFilter(filter: ServiceStatus?) {
        _state.update { it.copy(statusFilter = filter) }
    }

    fun onProjectSelected(projectId: String) {
        _state.update { it.copy(selectedProjectId = projectId) }
    }

    fun toggleViewMode() {
        _state.update {
            it.copy(viewMode = if (it.viewMode == ViewMode.GRID) ViewMode.LIST else ViewMode.GRID)
        }
    }

    fun toggleLogPanel() {
        _state.update { it.copy(isLogPanelExpanded = !it.isLogPanelExpanded) }
    }

    fun adjustLogPanelHeight(delta: Int) {
        _state.update { it.copy(logPanelHeight = (it.logPanelHeight + delta).coerceIn(80, 600)) }
    }

    fun toggleAutoScroll() {
        _state.update { it.copy(autoScrollLogs = !it.autoScrollLogs) }
    }

    fun clearLogs() {
        val serviceId = _state.value.logServiceId ?: return
        _state.update { it.copy(logs = it.logs - serviceId) }
    }

    fun setLogService(serviceId: String) {
        _state.update { it.copy(logServiceId = serviceId) }
    }

    fun showAddServiceModal(show: Boolean) {
        _state.update { it.copy(showAddServiceModal = show) }
    }

    fun showAddProjectModal(show: Boolean) {
        _state.update { it.copy(showAddProjectModal = show) }
    }

    fun showExitConfirmation(show: Boolean) {
        _state.update { it.copy(showExitConfirmation = show) }
    }

    fun hasRunningServices(): Boolean {
        return _state.value.services.any { it.status == ServiceStatus.RUNNING || it.status == ServiceStatus.STARTING }
    }

    fun confirmExit(onConfirmed: () -> Unit) {
        if (hasRunningServices()) {
            stopAll()
        }
        onConfirmed()
    }

    fun showShortcutsModal(show: Boolean) {
        _state.update { it.copy(showShortcutsModal = show) }
    }

    fun toggleSleepIdle() {
        _state.update { it.copy(sleepIdleEnabled = !it.sleepIdleEnabled) }
    }

    fun toggleThemeMode() {
        _state.update {
            it.copy(
                themeMode = if (it.themeMode == ThemeMode.LIGHT) ThemeMode.DARK else ThemeMode.LIGHT
            )
        }
    }

    fun addProject(name: String, path: String, color: Long) {
        val projectId = UUID.randomUUID().toString()
        
        // 1. Detect services in the root folder
        val discoveredServices = manager.detectServices(path).map { 
            it.copy(projectId = projectId) 
        }

        val newProject = Project(
            id = projectId,
            name = name,
            path = path,
            color = color,
            serviceIds = discoveredServices.map { it.id }
        )

        // 2. Update state: add project and all discovered services
        _state.update { s ->
            // Filter out services that might already exist by path to avoid duplicates
            val uniqueDiscovered = discoveredServices.filter { ds -> 
                s.services.none { it.path == ds.path } 
            }
            
            s.copy(
                projects = s.projects + newProject,
                services = s.services + uniqueDiscovered,
                selectedProjectId = newProject.id,
                showAddProjectModal = false
            )
        }

        // 3. Persist
        storage.saveProjects(_state.value.projects)
        storage.saveServices(_state.value.services)
        
        showToast(
            if (discoveredServices.isNotEmpty()) "Project created with ${discoveredServices.size} services" 
            else "Project created", 
            ToastType.SUCCESS
        )
    }

    fun deleteProject(projectId: String) {
        val newProjects = _state.value.projects.filter { it.id != projectId }
        _state.update {
            it.copy(
                projects = newProjects,
                selectedProjectId = if (it.selectedProjectId == projectId) newProjects.firstOrNull()?.id else it.selectedProjectId
            )
        }
        storage.saveProjects(newProjects)
    }

    fun deleteService(serviceId: String) {
        val service = _state.value.services.find { it.id == serviceId } ?: return
        if (service.status == ServiceStatus.RUNNING) manager.stopService(service)
        val newServices = _state.value.services.filter { it.id != serviceId }
        val updatedProjects = _state.value.projects.map { p ->
            p.copy(serviceIds = p.serviceIds.filter { it != serviceId })
        }
        _state.update {
            it.copy(
                services = newServices,
                projects = updatedProjects,
                selectedServiceId = if (it.selectedServiceId == serviceId) null else it.selectedServiceId,
                drawerServiceId = if (it.drawerServiceId == serviceId) null else it.drawerServiceId,
                isDrawerOpen = if (it.drawerServiceId == serviceId) false else it.isDrawerOpen
            )
        }
        storage.saveServices(newServices)
        storage.saveProjects(updatedProjects)
        showToast("${service.name} removed", ToastType.INFO)
    }

    fun addServices(services: List<Service>) {
        if (services.isEmpty()) return
        
        val projectId = _state.value.selectedProjectId
        val updatedServicesToAdd = services.map { it.copy(projectId = projectId) }

        _state.update { s ->
            // Avoid adding services that already exist by path
            val uniqueToAdd = updatedServicesToAdd.filter { toAdd -> 
                s.services.none { it.path == toAdd.path } 
            }
            
            val nextServices = s.services + uniqueToAdd
            
            // Link new services to the current project
            val updatedProjects = if (projectId != null) {
                s.projects.map { p ->
                    if (p.id == projectId) {
                        p.copy(serviceIds = (p.serviceIds + uniqueToAdd.map { it.id }).distinct())
                    } else p
                }
            } else s.projects

            s.copy(
                services = nextServices,
                projects = updatedProjects,
                showAddServiceModal = false
            )
        }

        storage.saveServices(_state.value.services)
        storage.saveProjects(_state.value.projects)
        
        showToast("Added ${services.size} services", ToastType.SUCCESS)
    }

    fun addService(
        name: String,
        port: Int,
        framework: String,
        path: String,
        dependencies: List<String>,
        jvmArgs: String
    ) {
        val trimmedName = name.trim()
        val trimmedPath = path.trim()
        if (trimmedName.isBlank() || port <= 0) return
        if (_state.value.services.any { it.name.equals(trimmedName, ignoreCase = true) }) {
            showToast("Service '$trimmedName' already exists", ToastType.ERROR)
            return
        }
        if (trimmedPath.isNotBlank() && _state.value.services.any { it.path == trimmedPath }) {
            showToast("A service already uses that path", ToastType.ERROR)
            return
        }

        var newService = Service(
            id = UUID.randomUUID().toString(),
            name = trimmedName,
            path = trimmedPath.ifBlank { File(trimmedName).absolutePath },
            framework = framework,
            status = ServiceStatus.STOPPED,
            port = port,
            dependencies = dependencies.filter { it.isNotBlank() }.distinct(),
            projectId = _state.value.selectedProjectId,
            jvmArgs = jvmArgs.trim()
        )
        newService = manager.scanMetadata(newService)
        
        if (_state.value.services.any { it.port == newService.port }) {
            showToast("Port ${newService.port} is already assigned to another service", ToastType.ERROR)
            return
        }

        val newServices = _state.value.services + newService
        _state.update { it.copy(services = newServices, showAddServiceModal = false) }
        storage.saveServices(newServices)
        _state.value.selectedProjectId?.let { projectId ->
            val updatedProjects = _state.value.projects.map {
                if (it.id == projectId) it.copy(serviceIds = it.serviceIds + newService.id) else it
            }
            _state.update { it.copy(projects = updatedProjects) }
            storage.saveProjects(updatedProjects)
        }
        showToast("'$trimmedName' added", ToastType.SUCCESS)
    }

    fun startAllInOrder() {
        val currentProjectId = _state.value.selectedProjectId
        val allServices = _state.value.services
        val projectServices = if (currentProjectId != null)
            allServices.filter { it.projectId == currentProjectId }
        else allServices

        val ordered = topologicalSort(projectServices)
        scope.launch {
            ordered.forEach { service ->
                if (service.status != ServiceStatus.RUNNING) {
                    manager.startService(service)
                    delay(800)
                }
            }
        }
        showToast("Starting all services…", ToastType.INFO)
    }

    fun stopAll() {
        manager.stopAll()
        // Update all running services to STOPPED in state
        _state.update { s ->
            s.copy(services = s.services.map {
                if (it.status == ServiceStatus.RUNNING || it.status == ServiceStatus.STARTING)
                    it.copy(status = ServiceStatus.STOPPED, cpu = 0f, heapMb = 0, healthPercent = 0)
                else it
            })
        }
        showToast("All services stopped", ToastType.INFO)
    }

    fun restartAll() {
        val currentProjectId = _state.value.selectedProjectId
        val runningServices = _state.value.services.filter {
            it.status == ServiceStatus.RUNNING &&
                    (currentProjectId == null || it.projectId == currentProjectId)
        }
        runningServices.forEach { manager.restartService(it) }
        showToast("Restarting ${runningServices.size} services…", ToastType.WARNING)
    }

    fun openFolder(service: Service) {
        manager.openFolder(service)
    }

    fun detectServices(path: String): List<Service> {
        return manager.detectServices(path)
    }

    fun scanMetadata(path: String): Service {
        val tempService = Service(
            id = "",
            name = "",
            path = path,
            framework = "",
            status = ServiceStatus.STOPPED,
            port = 8080
        )
        return manager.scanMetadata(tempService)
    }

    fun showToast(message: String, type: ToastType) {
        val toast = ToastData(id = UUID.randomUUID().toString(), message = message, type = type)
        _state.update { it.copy(toasts = it.toasts + toast) }
        scope.launch {
            delay(4000)
            dismissToast(toast.id)
        }
    }

    fun dismissToast(id: String) {
        _state.update { it.copy(toasts = it.toasts.filter { t -> t.id != id }) }
    }

    private fun topologicalSort(services: List<Service>): List<Service> {
        val nameToService = services.associateBy { it.name }
        val visited = mutableSetOf<String>()
        val result = mutableListOf<Service>()

        fun visit(service: Service) {
            if (service.id in visited) return
            visited.add(service.id)
            service.dependencies.forEach { depName ->
                nameToService[depName]?.let { visit(it) }
            }
            result.add(service)
        }
        services.forEach { visit(it) }
        return result
    }
}
