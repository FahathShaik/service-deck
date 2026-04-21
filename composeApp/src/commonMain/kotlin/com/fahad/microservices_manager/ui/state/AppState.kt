package com.fahad.microservices_manager.ui.state

import com.fahad.microservices_manager.domain.LogEntry
import com.fahad.microservices_manager.domain.Project
import com.fahad.microservices_manager.domain.Service
import com.fahad.microservices_manager.domain.ServiceStatus

data class ToastData(
    val id: String,
    val message: String,
    val type: ToastType
)

enum class ToastType { SUCCESS, ERROR, INFO, WARNING }

enum class ViewMode { GRID, LIST }

enum class ThemeMode { LIGHT, DARK }

data class AppState(
    val projects: List<Project> = emptyList(),
    val services: List<Service> = emptyList(),
    val logs: Map<String, List<LogEntry>> = emptyMap(),
    val selectedProjectId: String? = null,
    val selectedServiceId: String? = null,
    val logServiceId: String? = null,
    val searchQuery: String = "",
    val statusFilter: ServiceStatus? = null,
    val viewMode: ViewMode = ViewMode.GRID,
    val isLogPanelExpanded: Boolean = false,
    val logPanelHeight: Int = 280,
    val autoScrollLogs: Boolean = true,
    val isDrawerOpen: Boolean = false,
    val drawerServiceId: String? = null,
    val themeMode: ThemeMode = ThemeMode.LIGHT,
    val sleepIdleEnabled: Boolean = false,
    val showAddServiceModal: Boolean = false,
    val showAddProjectModal: Boolean = false,
    val showShortcutsModal: Boolean = false,
    val showExitConfirmation: Boolean = false,
    val portConflictService: Service? = null,
    val toasts: List<ToastData> = emptyList()
)
