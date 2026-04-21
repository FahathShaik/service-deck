package com.fahad.microservices_manager.domain

import kotlinx.coroutines.flow.SharedFlow

interface ServiceManager {
    val serviceUpdates: SharedFlow<Service>
    val logStream: SharedFlow<Pair<String, LogEntry>>

    fun startService(service: Service)
    fun stopService(service: Service)
    fun restartService(service: Service)
    fun buildService(service: Service)
    fun cleanService(service: Service)
    fun getLogs(service: Service): String
    fun stopAll()
    fun isPortInUse(port: Int): Boolean
    fun killProcessOnPort(port: Int): Boolean
    fun detectServices(rootPath: String): List<Service>
    fun discoverRunningServices(services: List<Service>): List<Service>
    fun openFolder(service: Service)
    fun scanMetadata(service: Service): Service
}
