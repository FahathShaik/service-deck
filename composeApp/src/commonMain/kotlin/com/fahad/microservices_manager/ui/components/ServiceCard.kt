package com.fahad.microservices_manager.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fahad.microservices_manager.domain.Service
import com.fahad.microservices_manager.domain.ServiceStatus
import com.fahad.microservices_manager.ui.theme.DevPilotColors
import com.fahad.microservices_manager.ui.theme.DevPilotRadius
import com.fahad.microservices_manager.ui.theme.DevPilotSpacing

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ServiceCard(
    service: Service,
    isSelected: Boolean,
    allServices: List<Service>,
    onClick: () -> Unit,
    onDoubleClick: () -> Unit,
    onStart: () -> Unit,
    onStop: () -> Unit,
    onRestart: () -> Unit,
    onShowLogs: () -> Unit,
    onOpenFolder: () -> Unit,
    onOpenDrawer: () -> Unit,
    onDelete: () -> Unit
) {
    val statusColor = when (service.status) {
        ServiceStatus.STOPPED -> DevPilotColors.red
        ServiceStatus.STARTING -> DevPilotColors.amber
        ServiceStatus.STOPPING -> DevPilotColors.amber
        ServiceStatus.RUNNING -> DevPilotColors.green
        ServiceStatus.ERROR -> DevPilotColors.red
        ServiceStatus.BUILDING -> DevPilotColors.blue
    }
    val statusDim = when (service.status) {
        ServiceStatus.STOPPED -> DevPilotColors.redDim
        ServiceStatus.STARTING -> DevPilotColors.amberDim
        ServiceStatus.STOPPING -> DevPilotColors.amberDim
        ServiceStatus.RUNNING -> DevPilotColors.greenDim
        ServiceStatus.ERROR -> DevPilotColors.redDim
        ServiceStatus.BUILDING -> DevPilotColors.blueDim
    }

    // Spinner animation for STARTING
    val infiniteTransition = rememberInfiniteTransition(label = "spinner")
    val spinnerAngle by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(1000, easing = LinearEasing)),
        label = "spin"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 0.4f,
        animationSpec = infiniteRepeatable(tween(1500), RepeatMode.Reverse),
        label = "pulse"
    )

    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    var showContextMenu by remember { mutableStateOf(false) }

    Box {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(DevPilotRadius.lg))
                .background(if (isSelected) DevPilotColors.bg3 else DevPilotColors.bg2)
                .border(
                    width = 1.dp,
                    color = when {
                        isSelected -> DevPilotColors.accentMid
                        isHovered -> DevPilotColors.border2
                        else -> DevPilotColors.border1
                    },
                    shape = RoundedCornerShape(DevPilotRadius.lg)
                )
                .hoverable(interactionSource)
                .combinedClickable(
                    onClick = onClick,
                    onDoubleClick = onDoubleClick
                )
        ) {
            // Top accent bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .background(
                        if (service.status == ServiceStatus.STARTING || service.status == ServiceStatus.STOPPING)
                            Brush.horizontalGradient(listOf(statusColor, DevPilotColors.bg2, statusColor))
                        else
                            Brush.horizontalGradient(listOf(statusColor, statusColor))
                    )
                    .alpha(if (service.status == ServiceStatus.STARTING || service.status == ServiceStatus.STOPPING) pulseAlpha else 1f)
            )

            Column(modifier = Modifier.padding(14.dp, 12.dp, 14.dp, 14.dp)) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Status icon box
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(statusDim),
                        contentAlignment = Alignment.Center
                    ) {
                        when (service.status) {
                            ServiceStatus.RUNNING -> {
                                Icon(
                                    Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    tint = statusColor,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            ServiceStatus.STARTING, ServiceStatus.STOPPING -> {
                                Icon(
                                    Icons.Default.Refresh,
                                    contentDescription = null,
                                    tint = statusColor,
                                    modifier = Modifier
                                        .size(20.dp)
                                        .graphicsLayer { rotationZ = spinnerAngle }
                                )
                            }
                            ServiceStatus.ERROR -> {
                                Icon(
                                    Icons.Default.ErrorOutline,
                                    contentDescription = null,
                                    tint = statusColor,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            ServiceStatus.BUILDING -> {
                                Icon(
                                    Icons.Default.Build,
                                    contentDescription = null,
                                    tint = statusColor,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            else -> {
                                Icon(
                                    Icons.Default.Stop,
                                    contentDescription = null,
                                    tint = statusColor,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    Spacer(Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = service.name,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.5.sp
                            ),
                            color = DevPilotColors.text0,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            val displayFramework = if (service.version.contains(service.framework, ignoreCase = true)) "" else service.framework
                            if (displayFramework.isNotBlank()) {
                                Text(
                                    text = displayFramework,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 12.5.sp
                                    ),
                                    color = DevPilotColors.text2
                                )
                            }
                            if (service.version.isNotBlank()) {
                                if (displayFramework.isNotBlank()) {
                                    Text(
                                        text = "·",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.5.sp),
                                        color = DevPilotColors.text3
                                    )
                                }
                                Text(
                                    text = service.version,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 12.5.sp
                                    ),
                                    color = DevPilotColors.text2,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    Spacer(Modifier.width(8.dp))
                    StatusBadge(status = service.status)

                    // Context menu trigger
                    IconButton(
                        onClick = { showContextMenu = true },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            Icons.Default.MoreVert,
                            contentDescription = "More",
                            tint = DevPilotColors.text3,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(Modifier.height(10.dp))

                // Health bar
                HealthBar(
                    percent = if (service.status == ServiceStatus.RUNNING || service.status == ServiceStatus.BUILDING)
                        service.healthPercent else 0,
                    color = statusColor,
                    animated = service.status == ServiceStatus.STARTING || service.status == ServiceStatus.STOPPING
                )

                Spacer(Modifier.height(10.dp))

                // Metrics row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = 1.dp,
                            color = DevPilotColors.border1,
                            shape = RoundedCornerShape(DevPilotRadius.sm)
                        )
                        .padding(vertical = 10.dp, horizontal = 6.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    val isActive = service.status == ServiceStatus.RUNNING
                    MetricItem(
                        label = "CPU",
                        value = if (isActive) "${"%.1f".format(service.cpu)}%" else "—",
                        color = if (isActive) (if (service.cpu > 80) DevPilotColors.red else DevPilotColors.green) else DevPilotColors.text3
                    )
                    Box(modifier = Modifier.width(1.dp).height(24.dp).background(DevPilotColors.border1))
                    MetricItem(
                        label = "HEAP",
                        value = if (isActive) "${service.heapMb}MB" else "—",
                        color = if (isActive) DevPilotColors.blue else DevPilotColors.text3
                    )
                    Box(modifier = Modifier.width(1.dp).height(24.dp).background(DevPilotColors.border1))
                    MetricItem(
                        label = "PORT",
                        value = if (service.port == 0) "Auto" else service.port.toString(),
                        color = DevPilotColors.blue
                    )
                }

                Spacer(Modifier.height(10.dp))

                // Dependencies
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Link,
                        contentDescription = null,
                        tint = DevPilotColors.text3,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    if (service.dependencies.isEmpty()) {
                        Text(
                            text = "No dependencies",
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.5.sp),
                            color = DevPilotColors.text3
                        )
                    } else {
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            service.dependencies.take(3).forEach { depName ->
                                val depRunning = allServices.any { it.name == depName && it.status == ServiceStatus.RUNNING }
                                DependencyChip(name = depName, isRunning = depRunning)
                            }
                            if (service.dependencies.size > 3) {
                                Text(
                                    text = "+${service.dependencies.size - 3}",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 11.5.sp),
                                    color = DevPilotColors.text2
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                // Actions row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    when (service.status) {
                        ServiceStatus.RUNNING, ServiceStatus.STARTING, ServiceStatus.STOPPING -> {
                            ActionButton(
                                label = if (service.status == ServiceStatus.STOPPING) "Stopping…" else "Stop",
                                icon = Icons.Default.Stop,
                                onClick = onStop,
                                enabled = service.status != ServiceStatus.STOPPING,
                                containerColor = DevPilotColors.redDim,
                                contentColor = DevPilotColors.red,
                                borderColor = DevPilotColors.redMid
                            )
                        }
                        else -> {
                            ActionButton(
                                label = "Start",
                                icon = Icons.Default.PlayArrow,
                                onClick = onStart,
                                containerColor = DevPilotColors.accent,
                                contentColor = DevPilotColors.bg0,
                                borderColor = Color.Transparent
                            )
                        }
                    }

                    // Restart icon button
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(DevPilotRadius.sm))
                            .background(DevPilotColors.bgHover)
                            .border(1.dp, DevPilotColors.border2, RoundedCornerShape(DevPilotRadius.sm))
                            .clickable(onClick = onRestart),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "Restart",
                            tint = DevPilotColors.text1,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Spacer(Modifier.weight(1f))

                    // Open folder
                    CardIconButton(onClick = onOpenFolder, icon = Icons.Default.FolderOpen, desc = "Open Folder")

                    // Show logs
                    CardIconButton(onClick = onShowLogs, icon = Icons.Default.Terminal, desc = "Logs")

                    // Open drawer
                    CardIconButton(onClick = onOpenDrawer, icon = Icons.Default.OpenInFull, desc = "Details")
                }
            }
        }

        // Context menu
        DropdownMenu(
            expanded = showContextMenu,
            onDismissRequest = { showContextMenu = false },
            modifier = Modifier
                .background(DevPilotColors.bg3)
                .border(1.dp, DevPilotColors.border2, RoundedCornerShape(DevPilotRadius.md))
        ) {
            if (service.status != ServiceStatus.RUNNING) {
                DropdownMenuItem(
                    text = { Text("Start", color = DevPilotColors.green, style = MaterialTheme.typography.bodyLarge.copy(fontSize = 13.5.sp)) },
                    onClick = { onStart(); showContextMenu = false },
                    leadingIcon = { Icon(Icons.Default.PlayArrow, null, tint = DevPilotColors.green, modifier = Modifier.size(14.dp)) }
                )
            } else {
                DropdownMenuItem(
                    text = { Text("Stop", color = DevPilotColors.red, style = MaterialTheme.typography.bodyLarge.copy(fontSize = 13.5.sp)) },
                    onClick = { onStop(); showContextMenu = false },
                    leadingIcon = { Icon(Icons.Default.Stop, null, tint = DevPilotColors.red, modifier = Modifier.size(14.dp)) }
                )
            }
            DropdownMenuItem(
                text = { Text("Restart", color = DevPilotColors.text1, style = MaterialTheme.typography.bodyLarge.copy(fontSize = 13.5.sp)) },
                onClick = { onRestart(); showContextMenu = false },
                leadingIcon = { Icon(Icons.Default.Refresh, null, tint = DevPilotColors.text2, modifier = Modifier.size(14.dp)) }
            )
            DropdownMenuItem(
                text = { Text("Details", color = DevPilotColors.text1, style = MaterialTheme.typography.bodyLarge.copy(fontSize = 13.5.sp)) },
                onClick = { onOpenDrawer(); showContextMenu = false },
                leadingIcon = { Icon(Icons.Default.OpenInFull, null, tint = DevPilotColors.text2, modifier = Modifier.size(14.dp)) }
            )
            DropdownMenuItem(
                text = { Text("Open Folder", color = DevPilotColors.text1, style = MaterialTheme.typography.bodyLarge.copy(fontSize = 13.5.sp)) },
                onClick = { onOpenFolder(); showContextMenu = false },
                leadingIcon = { Icon(Icons.Default.FolderOpen, null, tint = DevPilotColors.text2, modifier = Modifier.size(14.dp)) }
            )
            DropdownMenuItem(
                text = { Text("View Logs", color = DevPilotColors.text1, style = MaterialTheme.typography.bodyLarge.copy(fontSize = 13.5.sp)) },
                onClick = { onShowLogs(); showContextMenu = false },
                leadingIcon = { Icon(Icons.Default.Terminal, null, tint = DevPilotColors.text2, modifier = Modifier.size(14.dp)) }
            )
            HorizontalDivider(color = DevPilotColors.border1, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
            DropdownMenuItem(
                text = { Text("Remove Service", color = DevPilotColors.red, style = MaterialTheme.typography.bodyLarge.copy(fontSize = 13.5.sp)) },
                onClick = { onDelete(); showContextMenu = false },
                leadingIcon = { Icon(Icons.Default.Delete, null, tint = DevPilotColors.red, modifier = Modifier.size(14.dp)) }
            )
        }
    }
}

@Composable
fun CardIconButton(
    onClick: () -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    desc: String
) {
    Box(
        modifier = Modifier
            .size(34.dp)
            .clip(RoundedCornerShape(DevPilotRadius.sm))
            .border(1.dp, DevPilotColors.border1, RoundedCornerShape(DevPilotRadius.sm))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = desc, tint = DevPilotColors.text2, modifier = Modifier.size(16.dp))
    }
}

@Composable
fun HealthBar(percent: Int, color: Color, animated: Boolean = false) {
    val infiniteTransition = rememberInfiniteTransition(label = "health")
    val animatedWidth by infiniteTransition.animateFloat(
        initialValue = 0.3f, targetValue = 0.7f,
        animationSpec = infiniteRepeatable(tween(1500), RepeatMode.Reverse),
        label = "healthAnim"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(3.dp)
            .clip(RoundedCornerShape(999.dp))
            .background(DevPilotColors.border1)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(if (animated) animatedWidth else percent / 100f)
                .fillMaxHeight()
                .clip(RoundedCornerShape(999.dp))
                .background(
                    if (animated) Brush.horizontalGradient(listOf(color, color.copy(alpha = 0.4f)))
                    else Brush.horizontalGradient(listOf(color, color))
                )
        )
    }
}

@Composable
fun MetricItem(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(fontSize = 11.5.sp),
            color = DevPilotColors.text3
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.5.sp
            ),
            color = color
        )
    }
}

@Composable
fun ActionButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    containerColor: Color,
    contentColor: Color,
    borderColor: Color,
    enabled: Boolean = true
) {
    val alpha = if (enabled) 1f else 0.5f
    Box(
        modifier = Modifier
            .height(36.dp)
            .clip(RoundedCornerShape(DevPilotRadius.sm))
            .background(containerColor.copy(alpha = containerColor.alpha * alpha))
            .border(1.dp, borderColor.copy(alpha = borderColor.alpha * alpha), RoundedCornerShape(DevPilotRadius.sm))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            Icon(icon, contentDescription = null, tint = contentColor.copy(alpha = contentColor.alpha * alpha), modifier = Modifier.size(13.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium, fontSize = 12.5.sp),
                color = contentColor.copy(alpha = contentColor.alpha * alpha)
            )
        }
    }
}
