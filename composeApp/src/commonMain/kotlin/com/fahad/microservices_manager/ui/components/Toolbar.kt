package com.fahad.microservices_manager.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fahad.microservices_manager.domain.ServiceStatus
import com.fahad.microservices_manager.ui.state.ViewMode
import com.fahad.microservices_manager.ui.theme.DevPilotColors
import com.fahad.microservices_manager.ui.theme.DevPilotRadius

@Composable
fun Toolbar(
    onStartAll: () -> Unit,
    onStopAll: () -> Unit,
    onRestartAll: () -> Unit,
    onAddServiceClick: () -> Unit,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    statusFilter: ServiceStatus?,
    onStatusFilter: (ServiceStatus?) -> Unit,
    viewMode: ViewMode,
    onViewModeChange: (ViewMode) -> Unit,
    searchFocusRequester: FocusRequester
) {
    var isSearchFocused by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp)
            .background(DevPilotColors.bg0)
            .border(width = 1.dp, color = DevPilotColors.border1, shape = RoundedCornerShape(0.dp))
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Start All
        ActionButton(
            label = "Start All",
            icon = Icons.Default.PlayArrow,
            onClick = onStartAll,
            containerColor = DevPilotColors.accent,
            contentColor = DevPilotColors.bg0,
            borderColor = Color.Transparent
        )

        // Stop All
        ActionButton(
            label = "Stop All",
            icon = Icons.Default.Stop,
            onClick = onStopAll,
            containerColor = Color.Transparent,
            contentColor = DevPilotColors.red,
            borderColor = DevPilotColors.redMid
        )

        // Restart All
        ActionButton(
            label = "Restart",
            icon = Icons.Default.Refresh,
            onClick = onRestartAll,
            containerColor = Color.Transparent,
            contentColor = DevPilotColors.text1,
            borderColor = DevPilotColors.border2
        )

        // Vertical divider
        Box(
            modifier = Modifier
                .width(1.dp)
                .height(24.dp)
                .background(DevPilotColors.border1)
        )

        // Search input
        Box(
            modifier = Modifier
                .width(320.dp)
                .height(38.dp)
                .clip(RoundedCornerShape(DevPilotRadius.md))
                .background(DevPilotColors.bg1)
                .border(
                    1.dp,
                    if (isSearchFocused) DevPilotColors.accentMid else DevPilotColors.border1,
                    RoundedCornerShape(DevPilotRadius.md)
                )
                .padding(horizontal = 12.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Search,
                    contentDescription = null,
                    tint = if (isSearchFocused) DevPilotColors.accent else DevPilotColors.text3,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(8.dp))
                Box(modifier = Modifier.weight(1f)) {
                    if (searchQuery.isEmpty()) {
                        Text(
                            text = "Search services…  ⌘K",
                            style = TextStyle(fontSize = 12.5.sp, color = DevPilotColors.text3)
                        )
                    }
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChange,
                        singleLine = true,
                        cursorBrush = SolidColor(DevPilotColors.accent),
                        textStyle = TextStyle(
                            fontSize = 13.sp,
                            color = DevPilotColors.text0
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(searchFocusRequester)
                            .onFocusChanged { isSearchFocused = it.isFocused }
                    )
                }
                if (searchQuery.isNotBlank()) {
                    IconButton(
                        onClick = { onSearchQueryChange("") },
                        modifier = Modifier.size(18.dp)
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Clear search",
                            tint = DevPilotColors.text3,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }
        }

        // Status filter group
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(DevPilotRadius.md))
                .background(DevPilotColors.bg1)
                .border(1.dp, DevPilotColors.border1, RoundedCornerShape(DevPilotRadius.md))
                .height(36.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilterChip(label = "All", isActive = statusFilter == null) { onStatusFilter(null) }
            FilterChipDivider()
            FilterChip(
                label = "Running",
                isActive = statusFilter == ServiceStatus.RUNNING,
                activeColor = DevPilotColors.green
            ) { onStatusFilter(ServiceStatus.RUNNING) }
            FilterChipDivider()
            FilterChip(
                label = "Stopped",
                isActive = statusFilter == ServiceStatus.STOPPED,
                activeColor = DevPilotColors.red
            ) { onStatusFilter(ServiceStatus.STOPPED) }
            FilterChipDivider()
            FilterChip(
                label = "Error",
                isActive = statusFilter == ServiceStatus.ERROR,
                activeColor = DevPilotColors.red
            ) { onStatusFilter(ServiceStatus.ERROR) }
        }

        Spacer(Modifier.weight(1f))

        // Add Service button
        ActionButton(
            label = "Add Service",
            icon = Icons.Default.Add,
            onClick = onAddServiceClick,
            containerColor = DevPilotColors.blueDim,
            contentColor = DevPilotColors.blue,
            borderColor = DevPilotColors.blueMid
        )

        // View toggle
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(DevPilotRadius.md))
                .background(DevPilotColors.bg1)
                .border(1.dp, DevPilotColors.border1, RoundedCornerShape(DevPilotRadius.md))
                .height(36.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = { onViewModeChange(ViewMode.GRID) },
                    modifier = Modifier
                        .size(36.dp)
                        .background(if (viewMode == ViewMode.GRID) DevPilotColors.bg3 else Color.Transparent)
                ) {
                    Icon(
                        Icons.Default.GridView,
                        contentDescription = "Grid",
                        tint = if (viewMode == ViewMode.GRID) DevPilotColors.accent else DevPilotColors.text2,
                        modifier = Modifier.size(18.dp)
                    )
                }
                IconButton(
                    onClick = { onViewModeChange(ViewMode.LIST) },
                    modifier = Modifier
                        .size(36.dp)
                        .background(if (viewMode == ViewMode.LIST) DevPilotColors.bg3 else Color.Transparent)
                ) {
                    Icon(
                        Icons.Default.ViewList,
                        contentDescription = "List",
                        tint = if (viewMode == ViewMode.LIST) DevPilotColors.accent else DevPilotColors.text2,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun FilterChip(
    label: String,
    isActive: Boolean,
    activeColor: androidx.compose.ui.graphics.Color = DevPilotColors.accent,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(DevPilotRadius.sm))
            .background(if (isActive) activeColor.copy(alpha = 0.12f) else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 5.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
            color = if (isActive) activeColor else DevPilotColors.text2
        )
    }
}

@Composable
private fun FilterChipDivider() {
    Box(modifier = Modifier.width(1.dp).height(16.dp).background(DevPilotColors.border1))
}
