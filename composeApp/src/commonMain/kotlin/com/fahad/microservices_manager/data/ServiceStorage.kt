package com.fahad.microservices_manager.data

import com.fahad.microservices_manager.domain.Project
import com.fahad.microservices_manager.domain.Service
import com.fahad.microservices_manager.domain.ServiceStatus
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

class ServiceStorage(
    private val servicesFile: File = File("services.json"),
    private val projectsFile: File = File("projects.json")
) {
    private val json = Json { prettyPrint = true; ignoreUnknownKeys = true }

    fun saveServices(services: List<Service>) {
        val jsonString = json.encodeToString(services.map { it.copy(status = ServiceStatus.STOPPED) })
        servicesFile.writeText(jsonString)
    }

    fun loadServices(): List<Service> {
        if (!servicesFile.exists()) return emptyList()
        return try {
            json.decodeFromString<List<Service>>(servicesFile.readText())
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun saveProjects(projects: List<Project>) {
        val jsonString = json.encodeToString(projects)
        projectsFile.writeText(jsonString)
    }

    fun loadProjects(): List<Project> {
        if (!projectsFile.exists()) return emptyList()
        return try {
            json.decodeFromString<List<Project>>(projectsFile.readText())
        } catch (e: Exception) {
            emptyList()
        }
    }
}
