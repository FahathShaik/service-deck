package com.fahad.microservices_manager.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fahad.microservices_manager.domain.Project
import com.fahad.microservices_manager.domain.Service
import com.fahad.microservices_manager.domain.ServiceStatus
import com.fahad.microservices_manager.ui.theme.DevPilotColors
import com.fahad.microservices_manager.ui.theme.DevPilotRadius
import com.fahad.microservices_manager.ui.theme.DevPilotSpacing

@Composable
fun Sidebar(
    projects: List<Project>,
    services: List<Service>,
    selectedProjectId: String?,
    onProjectClick: (String) -> Unit,
    onAddProjectClick: () -> Unit,
    onDeleteProjectClick: (String) -> Unit,
    onSettingsClick: () -> Unit
) {
    val selectedProjectServices = remember(projects, services, selectedProjectId) {
        val project = projects.find { it.id == selectedProjectId }
        if (project == null) emptyList() else services.filter { it.id in project.serviceIds }
    }
    val runningServices = selectedProjectServices.filter { it.status == ServiceStatus.RUNNING }
    val totalHeapMb = runningServices.sumOf { it.heapMb }
    val activePorts = runningServices.map { it.port }.distinct()

    Column(
        modifier = Modifier
            .width(280.dp)
            .fillMaxHeight()
            .background(DevPilotColors.bg1)
            .border(width = (0.5).dp, color = DevPilotColors.border1, shape = RoundedCornerShape(0.dp))
    ) {
        // Logo Section
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp)
                .padding(horizontal = 18.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(DevPilotColors.accent, Color(0xFF7C3AED))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Bolt,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(Modifier.width(10.dp))
            Column {
                Text(
                    "ServiceDeck",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = DevPilotColors.text0
                )
                Text(
                    "Professional v2.4.1",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp
                    ),
                    color = DevPilotColors.text2
                )
            }
            Spacer(Modifier.weight(1f))
            IconButton(onClick = onSettingsClick, modifier = Modifier.size(32.dp)) {
                Icon(
                    Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = DevPilotColors.text2,
                    modifier = Modifier.size(17.dp)
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // Projects Section Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "PROJECTS",
                style = MaterialTheme.typography.labelMedium,
                color = DevPilotColors.text2
            )
            IconButton(
                onClick = onAddProjectClick,
                modifier = Modifier.size(20.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = DevPilotColors.accent, modifier = Modifier.size(16.dp))
            }
        }

        Column(modifier = Modifier.weight(1f)) {
            projects.forEach { project ->
                var showDeleteMenu by remember { mutableStateOf(false) }
                
                Box {
                    SidebarItem(
                        title = project.name,
                        count = project.serviceIds.size,
                        isSelected = project.id == selectedProjectId,
                        color = Color(project.color),
                        onClick = { onProjectClick(project.id) },
                        onSecondaryClick = { showDeleteMenu = true }
                    )
                    
                    DropdownMenu(
                        expanded = showDeleteMenu,
                        onDismissRequest = { showDeleteMenu = false },
                        modifier = Modifier.background(DevPilotColors.bg3).border(1.dp, DevPilotColors.border2)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Delete Project", color = DevPilotColors.red) },
                            onClick = {
                                onDeleteProjectClick(project.id)
                                showDeleteMenu = false
                            },
                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = DevPilotColors.red, modifier = Modifier.size(16.dp)) }
                        )
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .padding(18.dp)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(DevPilotRadius.md))
                    .background(DevPilotColors.bg2)
                    .border(1.dp, DevPilotColors.border1, RoundedCornerShape(DevPilotRadius.md))
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.Computer,
                    contentDescription = null,
                    tint = DevPilotColors.accent,
                    modifier = Modifier.size(16.dp)
                )
                Column {
                    Text(
                        "System Overview",
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.5.sp, fontWeight = FontWeight.Bold),
                        color = DevPilotColors.text0
                    )
                    Text(
                        "Runtime: JDK 21.0.2",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp
                        ),
                        color = DevPilotColors.text2
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(DevPilotRadius.md))
                    .background(DevPilotColors.bg2)
                    .border(1.dp, DevPilotColors.border1, RoundedCornerShape(DevPilotRadius.md))
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    StatColumn(
                        "Up",
                        "${runningServices.size}/${selectedProjectServices.size}",
                        if (runningServices.isNotEmpty()) DevPilotColors.green else DevPilotColors.amber
                    )
                    StatColumn(
                        "Heap",
                        if (totalHeapMb > 0) "${"%.1f".format(totalHeapMb / 1024f)} GB" else "0.0 GB",
                        DevPilotColors.text0
                    )
                    StatColumn(
                        "Ports",
                        if (activePorts.isEmpty()) "—" else activePorts.size.toString(),
                        DevPilotColors.text0
                    )
                }
            }
        }
    }
}

@Composable
fun StatColumn(label: String, value: String, valueColor: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            label.uppercase(),
            style = MaterialTheme.typography.labelMedium.copy(fontSize = 9.sp),
            color = DevPilotColors.text3
        )
        Text(
            value,
            style = MaterialTheme.typography.titleLarge.copy(fontSize = 14.sp),
            color = valueColor
        )
    }
}

@Composable
fun SidebarItem(
    title: String,
    count: Int,
    isSelected: Boolean,
    color: Color,
    onClick: () -> Unit,
    onSecondaryClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 2.dp)
            .clip(RoundedCornerShape(DevPilotRadius.sm))
            .background(if (isSelected) DevPilotColors.bg3 else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(Modifier.width(12.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = if (isSelected) DevPilotColors.text0 else DevPilotColors.text1,
            modifier = Modifier.weight(1f)
        )
        if (count > 0) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(DevPilotColors.bg0)
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = count.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    color = DevPilotColors.text2
                )
            }
        }
    }
}
