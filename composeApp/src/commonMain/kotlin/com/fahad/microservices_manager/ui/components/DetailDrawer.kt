package com.fahad.microservices_manager.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fahad.microservices_manager.domain.Service
import com.fahad.microservices_manager.domain.ServiceStatus
import com.fahad.microservices_manager.ui.theme.DevPilotColors
import com.fahad.microservices_manager.ui.theme.DevPilotRadius
import com.fahad.microservices_manager.ui.theme.DevPilotSpacing

@Composable
fun DetailDrawer(
    service: Service,
    allServices: List<Service>,
    onClose: () -> Unit,
    onStart: () -> Unit,
    onStop: () -> Unit,
    onRestart: () -> Unit,
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

    Column(
        modifier = Modifier
            .width(460.dp)
            .fillMaxHeight()
            .background(DevPilotColors.bg1)
            .border(
                width = 1.dp,
                color = DevPilotColors.border2,
                shape = RoundedCornerShape(topStart = DevPilotRadius.lg, bottomStart = DevPilotRadius.lg)
            )
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(DevPilotColors.bg2)
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(statusColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (service.status) {
                        ServiceStatus.RUNNING -> Icons.Default.PlayArrow
                        ServiceStatus.ERROR -> Icons.Default.Error
                        ServiceStatus.STARTING -> Icons.Default.Refresh
                        else -> Icons.Default.Stop
                    },
                    contentDescription = null,
                    tint = statusColor,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = service.name,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    ),
                    color = DevPilotColors.text0
                )
                Text(
                    text = service.framework,
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 11.5.sp),
                    color = DevPilotColors.text2
                )
            }
            StatusBadge(status = service.status)
            Spacer(Modifier.width(12.dp))
            IconButton(onClick = onClose, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = DevPilotColors.text2, modifier = Modifier.size(16.dp))
            }
        }

        // Divider
        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(DevPilotColors.border1))

        // Scrollable body
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Configuration Section
            DrawerSection(title = "Configuration") {
                DrawerRow(label = "Port", value = service.port.toString(), valueColor = DevPilotColors.blue, mono = true)
                DrawerRow(label = "Framework", value = service.framework, valueColor = DevPilotColors.text0)
                DrawerRow(label = "Path", value = service.path, valueColor = DevPilotColors.text1, mono = true)
                if (service.jvmArgs.isNotBlank()) {
                    DrawerRow(label = "JVM Args", value = service.jvmArgs, valueColor = DevPilotColors.text1, mono = true)
                }
                if (service.version.isNotBlank()) {
                    DrawerRow(label = "Version", value = service.version, valueColor = DevPilotColors.text2)
                }
            }

            // Runtime Section
            DrawerSection(title = "Runtime") {
                DrawerRow(label = "Status", value = service.status.name, valueColor = statusColor)
                if (service.status == ServiceStatus.RUNNING) {
                    DrawerRow(
                        label = "CPU",
                        value = "${"%.1f".format(service.cpu)}%",
                        valueColor = if (service.cpu > 80) DevPilotColors.red else DevPilotColors.green,
                        mono = true
                    )
                    DrawerRow(
                        label = "Heap",
                        value = "${service.heapMb} MB",
                        valueColor = DevPilotColors.blue,
                        mono = true
                    )
                    DrawerRow(
                        label = "Health",
                        value = "${service.healthPercent}%",
                        valueColor = DevPilotColors.green,
                        mono = true
                    )
                }
            }

            // Dependencies Section
            if (service.dependencies.isNotEmpty()) {
                DrawerSection(title = "Dependencies (${service.dependencies.size})") {
                    service.dependencies.forEach { depName ->
                        val dep = allServices.find { it.name == depName }
                        val isRunning = dep?.status == ServiceStatus.RUNNING
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(androidx.compose.foundation.shape.CircleShape)
                                    .background(if (isRunning) DevPilotColors.green else DevPilotColors.red)
                            )
                            Text(
                                text = depName,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 13.sp
                                ),
                                color = DevPilotColors.text1
                            )
                            Spacer(Modifier.weight(1f))
                            Text(
                                text = dep?.status?.name ?: "UNKNOWN",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isRunning) DevPilotColors.green else DevPilotColors.text3
                            )
                        }
                    }
                }
            }
        }

        // Bottom Actions
        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(DevPilotColors.border1))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(DevPilotColors.bg2)
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (service.status == ServiceStatus.RUNNING || service.status == ServiceStatus.STARTING) {
                ActionButton(
                    label = "Stop",
                    icon = Icons.Default.Stop,
                    onClick = onStop,
                    containerColor = DevPilotColors.redDim,
                    contentColor = DevPilotColors.red,
                    borderColor = DevPilotColors.redMid
                )
            } else {
                ActionButton(
                    label = "Start",
                    icon = Icons.Default.PlayArrow,
                    onClick = onStart,
                    containerColor = DevPilotColors.greenDim,
                    contentColor = DevPilotColors.green,
                    borderColor = DevPilotColors.greenMid
                )
            }
            ActionButton(
                label = "Restart",
                icon = Icons.Default.Refresh,
                onClick = onRestart,
                containerColor = DevPilotColors.bgHover,
                contentColor = DevPilotColors.text1,
                borderColor = DevPilotColors.border2
            )
            Spacer(Modifier.weight(1f))
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(DevPilotRadius.sm))
                    .background(DevPilotColors.redDim)
                    .border(1.dp, DevPilotColors.redMid, RoundedCornerShape(DevPilotRadius.sm)),
                contentAlignment = Alignment.Center
            ) {
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Remove", tint = DevPilotColors.red, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
private fun DrawerSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(DevPilotRadius.md))
            .background(DevPilotColors.bg2)
            .border(1.dp, DevPilotColors.border1, RoundedCornerShape(DevPilotRadius.md))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelMedium,
            color = DevPilotColors.text2
        )
        content()
    }
}

@Composable
private fun DrawerRow(label: String, value: String, valueColor: Color, mono: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.5.sp),
            color = DevPilotColors.text2
        )
        Text(
            text = value,
            style = if (mono) {
                MaterialTheme.typography.bodyLarge.copy(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Medium
                )
            } else {
                MaterialTheme.typography.bodyLarge.copy(fontSize = 13.sp)
            },
            color = valueColor,
            maxLines = 1,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
            modifier = Modifier.widthIn(max = 260.dp)
        )
    }
}
