package com.fahad.microservices_manager.domain

import kotlinx.serialization.Serializable

@Serializable
enum class ServiceStatus { STOPPED, STARTING, RUNNING, ERROR, BUILDING, STOPPING }

@Serializable
enum class LogLevel { INFO, WARN, ERROR, DEBUG }

@Serializable
data class Project(
    val id: String,
    val name: String,
    val path: String = "",
    val color: Long,     // ARGB hex, e.g., 0xFF4B9CFF
    val isActive: Boolean = false,
    val serviceIds: List<String> = emptyList()
)

@Serializable
data class Service(
    val id: String,
    val name: String,
    val port: Int,
    val framework: String = "Unknown",
    val status: ServiceStatus,
    val cpu: Float = 0f,          // percentage, 0.0 when stopped
    val heapMb: Int = 0,         // megabytes, 0 when stopped
    val healthPercent: Int = 0,  // 0-100
    val dependencies: List<String> = emptyList(), // service names
    val projectId: String? = null,
    val jvmArgs: String = "",
    val path: String,
    val version: String = "",
    val logPath: String = ""
)

@Serializable
data class LogEntry(
    val timestamp: String,   // "HH:mm:ss.SSS"
    val level: LogLevel,
    val message: String
)
