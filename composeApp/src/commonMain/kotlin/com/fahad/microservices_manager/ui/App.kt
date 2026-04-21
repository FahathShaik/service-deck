package com.fahad.microservices_manager.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.focusable
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.unit.dp
import com.fahad.microservices_manager.domain.Service
import com.fahad.microservices_manager.domain.ServiceStatus
import com.fahad.microservices_manager.ui.components.AddProjectModal
import com.fahad.microservices_manager.ui.components.AddServiceModal
import com.fahad.microservices_manager.ui.components.DetailDrawer
import com.fahad.microservices_manager.ui.components.ExitConfirmationModal
import com.fahad.microservices_manager.ui.components.KeyboardShortcutsModal
import com.fahad.microservices_manager.ui.components.PortConflictModal
import com.fahad.microservices_manager.ui.components.LogPanel
import com.fahad.microservices_manager.ui.components.ServiceCard
import com.fahad.microservices_manager.ui.components.ServiceListHeader
import com.fahad.microservices_manager.ui.components.ServiceListRow
import com.fahad.microservices_manager.ui.components.Sidebar
import com.fahad.microservices_manager.ui.components.ToastStack
import com.fahad.microservices_manager.ui.components.Toolbar
import com.fahad.microservices_manager.ui.components.Topbar
import com.fahad.microservices_manager.ui.state.ServicesViewModel
import com.fahad.microservices_manager.ui.state.ThemeMode
import com.fahad.microservices_manager.ui.state.ViewMode
import com.fahad.microservices_manager.ui.theme.DevPilotColors
import com.fahad.microservices_manager.ui.theme.DevPilotSpacing

@Composable
fun App(viewModel: ServicesViewModel, onExit: () -> Unit) {
    val state by viewModel.state.collectAsState()
    val selectedProject = state.projects.find { it.id == state.selectedProjectId }
    val drawerService = state.services.find { it.id == state.drawerServiceId }
    val searchFocusRequester = remember { FocusRequester() }
    val rootFocusRequester = remember { FocusRequester() }

    val filteredServices = remember(
        state.services,
        state.searchQuery,
        state.statusFilter,
        state.selectedProjectId,
        state.projects
    ) {
        filterServices(
            services = state.services,
            selectedProjectId = state.selectedProjectId,
            projectServiceIds = selectedProject?.serviceIds.orEmpty(),
            searchQuery = state.searchQuery,
            statusFilter = state.statusFilter
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DevPilotColors.bg0)
            .focusRequester(rootFocusRequester)
            .focusable()
            .onPreviewKeyEvent { keyEvent ->
                when {
                    keyEvent.isCtrlPressed && !keyEvent.isShiftPressed && keyEvent.key == Key.K -> {
                        searchFocusRequester.requestFocus()
                        true
                    }

                    keyEvent.isCtrlPressed && !keyEvent.isShiftPressed && keyEvent.key == Key.N -> {
                        viewModel.showAddServiceModal(true)
                        true
                    }

                    keyEvent.isCtrlPressed && !keyEvent.isShiftPressed && keyEvent.key == Key.L -> {
                        viewModel.toggleLogPanel()
                        true
                    }

                    keyEvent.isCtrlPressed && keyEvent.isShiftPressed && keyEvent.key == Key.S -> {
                        viewModel.startAllInOrder()
                        true
                    }

                    keyEvent.isCtrlPressed && keyEvent.isShiftPressed && keyEvent.key == Key.X -> {
                        viewModel.stopAll()
                        true
                    }

                    keyEvent.key == Key.Escape -> {
                        when {
                            state.showShortcutsModal -> viewModel.showShortcutsModal(false)
                            state.showAddServiceModal -> viewModel.showAddServiceModal(false)
                            state.showAddProjectModal -> viewModel.showAddProjectModal(false)
                            state.showExitConfirmation -> viewModel.showExitConfirmation(false)
                            state.portConflictService != null -> viewModel.cancelPortConflict()
                            state.isDrawerOpen -> viewModel.closeDrawer()
                        }
                        true
                    }

                    else -> false
                }
            }
    ) {
        LaunchedEffect(Unit) {
            rootFocusRequester.requestFocus()
        }

        Row(modifier = Modifier.fillMaxSize()) {
            Sidebar(
                projects = state.projects,
                services = state.services,
                selectedProjectId = state.selectedProjectId,
                onProjectClick = viewModel::onProjectSelected,
                onAddProjectClick = { viewModel.showAddProjectModal(true) },
                onDeleteProjectClick = viewModel::deleteProject,
                onSettingsClick = { viewModel.showToast("Settings coming soon", com.fahad.microservices_manager.ui.state.ToastType.INFO) }
            )

            Column(modifier = Modifier.weight(1f)) {
                Topbar(
                    selectedProject = selectedProject,
                    themeMode = state.themeMode,
                    onToggleThemeMode = viewModel::toggleThemeMode,
                    sleepIdleEnabled = state.sleepIdleEnabled,
                    onToggleSleepIdle = viewModel::toggleSleepIdle,
                    onShowShortcuts = { viewModel.showShortcutsModal(true) },
                    onShowBranchInfo = { viewModel.showToast("Branch switching coming soon", com.fahad.microservices_manager.ui.state.ToastType.INFO) }
                )

                Toolbar(
                    onStartAll = viewModel::startAllInOrder,
                    onStopAll = viewModel::stopAll,
                    onRestartAll = viewModel::restartAll,
                    onAddServiceClick = { viewModel.showAddServiceModal(true) },
                    searchQuery = state.searchQuery,
                    onSearchQueryChange = viewModel::onSearchQueryChanged,
                    statusFilter = state.statusFilter,
                    onStatusFilter = viewModel::setStatusFilter,
                    viewMode = state.viewMode,
                    onViewModeChange = { mode ->
                        if (mode != state.viewMode) {
                            viewModel.toggleViewMode()
                        }
                    },
                    searchFocusRequester = searchFocusRequester
                )

                Box(modifier = Modifier.weight(1f)) {
                    when (state.viewMode) {
                        ViewMode.GRID -> {
                            if (filteredServices.isEmpty()) {
                                EmptyServicesState(
                                    statusFilter = state.statusFilter,
                                    onAddService = { viewModel.showAddServiceModal(true) }
                                )
                            } else {
                                Column(modifier = Modifier.fillMaxSize()) {
                                    ServicesCountRow(
                                        total = selectedProject?.serviceIds?.size ?: state.services.size,
                                        running = filteredServices.count { it.status == ServiceStatus.RUNNING }
                                    )
                                    LazyVerticalGrid(
                                        columns = GridCells.Adaptive(minSize = 320.dp),
                                        contentPadding = PaddingValues(DevPilotSpacing.xl),
                                        horizontalArrangement = Arrangement.spacedBy(DevPilotSpacing.lg),
                                        verticalArrangement = Arrangement.spacedBy(DevPilotSpacing.lg),
                                        modifier = Modifier.fillMaxSize()
                                    ) {
                                        items(filteredServices, key = Service::id) { service ->
                                            ServiceCard(
                                                service = service,
                                                isSelected = state.selectedServiceId == service.id,
                                                allServices = state.services,
                                                onClick = { viewModel.selectService(service.id) },
                                                onDoubleClick = { viewModel.openDrawer(service.id) },
                                                onStart = { viewModel.startService(service) },
                                                onStop = { viewModel.stopService(service) },
                                                onRestart = { viewModel.restartService(service) },
                                                onShowLogs = { viewModel.selectService(service.id) },
                                                onOpenFolder = { viewModel.openFolder(service) },
                                                onOpenDrawer = { viewModel.openDrawer(service.id) },
                                                onDelete = { viewModel.deleteService(service.id) }
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        ViewMode.LIST -> {
                            if (filteredServices.isEmpty()) {
                                EmptyServicesState(
                                    statusFilter = state.statusFilter,
                                    onAddService = { viewModel.showAddServiceModal(true) }
                                )
                            } else {
                                Column(modifier = Modifier.fillMaxSize()) {
                                    ServicesCountRow(
                                        total = selectedProject?.serviceIds?.size ?: state.services.size,
                                        running = filteredServices.count { it.status == ServiceStatus.RUNNING }
                                    )
                                    ServiceListHeader()
                                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                                        items(filteredServices, key = Service::id) { service ->
                                            ServiceListRow(
                                                service = service,
                                                isSelected = state.selectedServiceId == service.id,
                                                allServices = state.services,
                                                onClick = { viewModel.selectService(service.id) },
                                                onDoubleClick = { viewModel.openDrawer(service.id) },
                                                onStart = { viewModel.startService(service) },
                                                onStop = { viewModel.stopService(service) },
                                                onRestart = { viewModel.restartService(service) }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                if (state.isLogPanelExpanded) {
                    LogPanel(
                        logs = state.logServiceId?.let { state.logs[it] }.orEmpty(),
                        services = filteredServices.ifEmpty { state.services },
                        logServiceId = state.logServiceId,
                        autoScroll = state.autoScrollLogs,
                        panelHeight = state.logPanelHeight,
                        onClose = viewModel::toggleLogPanel,
                        onResize = viewModel::adjustLogPanelHeight,
                        onClear = viewModel::clearLogs,
                        onToggleAutoScroll = viewModel::toggleAutoScroll,
                        onServiceSelected = viewModel::setLogService
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = state.isDrawerOpen && drawerService != null,
            enter = slideInHorizontally(
                initialOffsetX = { it },
                animationSpec = tween(350)
            ) + fadeIn(animationSpec = tween(200)),
            exit = slideOutHorizontally(
                targetOffsetX = { it },
                animationSpec = tween(350)
            ) + fadeOut(animationSpec = tween(200)),
            modifier = Modifier.align(Alignment.CenterEnd)
        ) {
            drawerService?.let { service ->
                DetailDrawer(
                    service = service,
                    allServices = state.services,
                    onClose = viewModel::closeDrawer,
                    onStart = { viewModel.startService(service) },
                    onStop = { viewModel.stopService(service) },
                    onRestart = { viewModel.restartService(service) },
                    onDelete = { viewModel.deleteService(service.id) }
                )
            }
        }

        ToastStack(
            toasts = state.toasts,
            onDismiss = viewModel::dismissToast,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 16.dp, end = 16.dp)
        )

        if (state.showAddServiceModal) {
            AddServiceModal(
                onDismiss = { viewModel.showAddServiceModal(false) },
                onDetectServices = { path ->
                    viewModel.detectServices(path)
                },
                onAddMultiple = { services ->
                    viewModel.addServices(services)
                }
            )
        }

        if (state.showAddProjectModal) {
            AddProjectModal(
                onDismiss = { viewModel.showAddProjectModal(false) },
                onAdd = { name, path, color -> viewModel.addProject(name, path, color) }
            )
        }

        if (state.showShortcutsModal) {
            KeyboardShortcutsModal(
                onDismiss = { viewModel.showShortcutsModal(false) }
            )
        }

        if (state.showExitConfirmation) {
            ExitConfirmationModal(
                onDismiss = { viewModel.showExitConfirmation(false) },
                onConfirm = {
                    viewModel.confirmExit(onExit)
                }
            )
        }

        state.portConflictService?.let { service ->
            PortConflictModal(
                service = service,
                onDismiss = { viewModel.cancelPortConflict() },
                onConfirm = { viewModel.resolvePortConflict(service) }
            )
        }
    }
}

@Composable
private fun ServicesCountRow(total: Int, running: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        androidx.compose.material3.Text(
            text = "$total service${if (total != 1) "s" else ""} — $running running",
            style = androidx.compose.material3.MaterialTheme.typography.labelMedium,
            color = DevPilotColors.text3
        )
        Spacer(Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(DevPilotColors.border1)
        )
    }
}

@Composable
private fun EmptyServicesState(
    statusFilter: ServiceStatus?,
    onAddService: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .background(DevPilotColors.bg2, androidx.compose.foundation.shape.CircleShape)
                .border(1.dp, DevPilotColors.border1, androidx.compose.foundation.shape.CircleShape),
            contentAlignment = Alignment.Center
        ) {
            androidx.compose.material3.Text(":::", color = DevPilotColors.text2)
        }
        Spacer(Modifier.height(16.dp))
        androidx.compose.material3.Text(
            text = if (statusFilter != null) "No ${statusFilter.name.lowercase()} services" else "No services yet",
            style = androidx.compose.material3.MaterialTheme.typography.titleLarge,
            color = DevPilotColors.text0
        )
        Spacer(Modifier.height(8.dp))
        androidx.compose.material3.Text(
            text = if (statusFilter != null) {
                "Try a different filter or add a new service."
            } else {
                "Add your first microservice to get started."
            },
            style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
            color = DevPilotColors.text2
        )
        Spacer(Modifier.height(16.dp))
        com.fahad.microservices_manager.ui.components.ActionButton(
            label = "Add Service",
            icon = Icons.Default.Add,
            onClick = onAddService,
            containerColor = DevPilotColors.blueDim,
            contentColor = DevPilotColors.blue,
            borderColor = DevPilotColors.blueMid
        )
    }
}

private fun filterServices(
    services: List<Service>,
    selectedProjectId: String?,
    projectServiceIds: List<String>,
    searchQuery: String,
    statusFilter: com.fahad.microservices_manager.domain.ServiceStatus?
): List<Service> {
    return services.filter { service ->
        val matchesProject = selectedProjectId == null || service.id in projectServiceIds
        val matchesSearch = searchQuery.isBlank() || service.name.contains(searchQuery, ignoreCase = true)
        val matchesStatus = statusFilter == null || service.status == statusFilter
        matchesProject && matchesSearch && matchesStatus
    }
}
