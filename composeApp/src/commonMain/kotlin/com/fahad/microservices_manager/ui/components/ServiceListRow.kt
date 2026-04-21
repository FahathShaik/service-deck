package com.fahad.microservices_manager.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fahad.microservices_manager.domain.Service
import com.fahad.microservices_manager.domain.ServiceStatus
import com.fahad.microservices_manager.ui.theme.DevPilotColors
import com.fahad.microservices_manager.ui.theme.DevPilotRadius

// Header row for list view
@Composable
fun ServiceListHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(Modifier.width(30.dp))
        Text(
            "NAME",
            style = MaterialTheme.typography.labelMedium,
            color = DevPilotColors.text3,
            modifier = Modifier.weight(1.5f)
        )
        Text(
            "STATUS",
            style = MaterialTheme.typography.labelMedium,
            color = DevPilotColors.text3,
            modifier = Modifier.width(80.dp)
        )
        Text(
            "PORT",
            style = MaterialTheme.typography.labelMedium,
            color = DevPilotColors.text3,
            modifier = Modifier.width(70.dp)
        )
        Text(
            "CPU",
            style = MaterialTheme.typography.labelMedium,
            color = DevPilotColors.text3,
            modifier = Modifier.width(70.dp)
        )
        Text(
            "HEAP",
            style = MaterialTheme.typography.labelMedium,
            color = DevPilotColors.text3,
            modifier = Modifier.width(70.dp)
        )
        Text(
            "DEPENDENCIES",
            style = MaterialTheme.typography.labelMedium,
            color = DevPilotColors.text3,
            modifier = Modifier.weight(1f)
        )
        Spacer(Modifier.width(80.dp))
    }
    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(DevPilotColors.border1))
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ServiceListRow(
    service: Service,
    isSelected: Boolean,
    allServices: List<Service>,
    onClick: () -> Unit,
    onDoubleClick: () -> Unit,
    onStart: () -> Unit,
    onStop: () -> Unit,
    onRestart: () -> Unit
) {
    val statusColor = when (service.status) {
        ServiceStatus.STOPPED -> DevPilotColors.red
        ServiceStatus.STARTING -> DevPilotColors.amber
        ServiceStatus.STOPPING -> DevPilotColors.amber
        ServiceStatus.RUNNING -> DevPilotColors.green
        ServiceStatus.ERROR -> DevPilotColors.red
        ServiceStatus.BUILDING -> DevPilotColors.blue
    }

    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .hoverable(interactionSource)
            .background(
                when {
                    isSelected -> DevPilotColors.bg3
                    isHovered -> DevPilotColors.bgHover
                    else -> Color.Transparent
                }
            )
            .border(
                width = 1.dp,
                color = if (isSelected) DevPilotColors.accentMid else Color.Transparent,
                shape = RoundedCornerShape(DevPilotRadius.xs)
            )
            .combinedClickable(onClick = onClick, onDoubleClick = onDoubleClick)
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Status dot
        Box(
            modifier = Modifier.width(30.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(statusColor)
            )
        }

        // Name
        Text(
            text = service.name,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp
            ),
            color = DevPilotColors.text0,
            modifier = Modifier.weight(1.5f)
        )

        // Status text
        Box(modifier = Modifier.width(80.dp)) {
            StatusBadge(status = service.status)
        }

        // Port
        Text(
            text = if (service.port == 0) "Auto" else service.port.toString(),
            style = MaterialTheme.typography.bodyLarge.copy(
                fontFamily = FontFamily.Monospace,
                fontSize = 13.sp
            ),
            color = DevPilotColors.blue,
            modifier = Modifier.width(70.dp)
        )

        // CPU
        val isActive = service.status == ServiceStatus.RUNNING
        Text(
            text = if (isActive && service.cpu >= 0f) "${"%.1f".format(service.cpu)}%" else "—",
            style = MaterialTheme.typography.bodyLarge.copy(
                fontFamily = FontFamily.Monospace,
                fontSize = 13.sp
            ),
            color = if (isActive && service.cpu >= 0f)
                (if (service.cpu > 80) DevPilotColors.red else DevPilotColors.green)
            else DevPilotColors.text3,
            modifier = Modifier.width(70.dp)
        )

        // Heap
        Text(
            text = if (isActive && service.heapMb >= 0) "${service.heapMb}MB" else "—",
            style = MaterialTheme.typography.bodyLarge.copy(
                fontFamily = FontFamily.Monospace,
                fontSize = 13.sp
            ),
            color = if (isActive && service.heapMb >= 0) DevPilotColors.blue else DevPilotColors.text3,
            modifier = Modifier.width(70.dp)
        )

        // Dependencies
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (service.dependencies.isEmpty()) {
                Text(
                    text = "—",
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 11.5.sp),
                    color = DevPilotColors.text3
                )
            } else {
                service.dependencies.take(2).forEach { depName ->
                    val depRunning = allServices.any { it.name == depName && it.status == ServiceStatus.RUNNING }
                    DependencyChip(name = depName, isRunning = depRunning)
                }
                if (service.dependencies.size > 2) {
                    Text(
                        text = "+${service.dependencies.size - 2}",
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 10.5.sp),
                        color = DevPilotColors.text2
                    )
                }
            }
        }

        // Actions
        Row(
            modifier = Modifier.width(80.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (service.status == ServiceStatus.RUNNING || service.status == ServiceStatus.STARTING) {
                IconButton(onClick = onStop, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Stop, contentDescription = "Stop", tint = DevPilotColors.red, modifier = Modifier.size(14.dp))
                }
            } else {
                IconButton(onClick = onStart, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.PlayArrow, contentDescription = "Start", tint = DevPilotColors.green, modifier = Modifier.size(14.dp))
                }
            }
            IconButton(onClick = onRestart, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Default.Refresh, contentDescription = "Restart", tint = DevPilotColors.text2, modifier = Modifier.size(14.dp))
            }
        }
    }

    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(DevPilotColors.border1.copy(alpha = 0.5f)))
}
