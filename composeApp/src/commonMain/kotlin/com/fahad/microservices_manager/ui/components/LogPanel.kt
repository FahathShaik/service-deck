package com.fahad.microservices_manager.ui.components

import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fahad.microservices_manager.domain.LogEntry
import com.fahad.microservices_manager.domain.LogLevel
import com.fahad.microservices_manager.domain.Service
import com.fahad.microservices_manager.domain.ServiceStatus
import com.fahad.microservices_manager.ui.theme.DevPilotColors
import com.fahad.microservices_manager.ui.theme.DevPilotRadius
import com.fahad.microservices_manager.ui.theme.DevPilotSpacing

@Composable
fun LogPanel(
    logs: List<LogEntry>,
    services: List<Service>,
    logServiceId: String?,
    autoScroll: Boolean,
    panelHeight: Int,
    onClose: () -> Unit,
    onResize: (Int) -> Unit,
    onClear: () -> Unit,
    onToggleAutoScroll: () -> Unit,
    onServiceSelected: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    val displayedLogs = remember(logs, searchQuery) {
        if (searchQuery.isNotBlank()) {
            logs.filter { it.message.contains(searchQuery, ignoreCase = true) }
        } else {
            logs
        }
    }

    LaunchedEffect(displayedLogs.size) {
        if (autoScroll && displayedLogs.isNotEmpty()) {
            listState.scrollToItem(displayedLogs.size - 1)
        }
    }

    val currentService = services.find { it.id == logServiceId }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(panelHeight.dp)
            .background(DevPilotColors.bg0) // Dark console background
            .border(width = 1.dp, color = DevPilotColors.border1, shape = RoundedCornerShape(0.dp))
    ) {
        // Resize Handle
        ResizeHandle(onResize)

        // Toolbar (IntelliJ style)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(36.dp)
                .background(DevPilotColors.bg2)
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Service Selector (Left)
            ServiceSelectorChip(
                currentService = currentService,
                services = services,
                onServiceSelected = onServiceSelected
            )

            Spacer(Modifier.width(16.dp))

            // Log Filter (Search)
            LogSearchField(
                query = searchQuery,
                onQueryChange = { searchQuery = it }
            )

            Spacer(Modifier.weight(1f))

            // Actions (Right)
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                LogActionButton(
                    icon = if (autoScroll) Icons.Default.VerticalAlignBottom else Icons.Default.VerticalAlignCenter,
                    tooltip = "Auto-scroll",
                    isActive = autoScroll,
                    onClick = onToggleAutoScroll
                )
                LogActionButton(
                    icon = Icons.Default.DeleteSweep,
                    tooltip = "Clear Console",
                    onClick = onClear
                )
                Box(modifier = Modifier.width(1.dp).height(20.dp).background(DevPilotColors.border1))
                LogActionButton(
                    icon = Icons.Default.Close,
                    tooltip = "Close Panel",
                    onClick = onClose
                )
            }
        }

        // Main Console Area
        Row(modifier = Modifier.fillMaxSize()) {
            // Left sidebar for Console actions (IntelliJ style)
            Column(
                modifier = Modifier
                    .width(36.dp)
                    .fillMaxHeight()
                    .background(DevPilotColors.bg2)
                    .border(width = 1.dp, color = DevPilotColors.border1, shape = RoundedCornerShape(0.dp))
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Console specific icons
                Icon(
                    Icons.Default.Terminal,
                    null,
                    tint = DevPilotColors.accent,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.height(4.dp))
                // You could add more sidebar items here like "Soft Wrap", "Scroll to End"
            }

            // The actual log stream
            Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                if (displayedLogs.isEmpty()) {
                    ConsoleEmptyState(logServiceId, searchQuery)
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(12.dp)
                    ) {
                        items(displayedLogs) { entry ->
                            IntelliJLogLine(entry, searchQuery)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ResizeHandle(onResize: (Int) -> Unit) {
    val handleInteraction = remember { MutableInteractionSource() }
    val isHandleHovered by handleInteraction.collectIsHoveredAsState()
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(3.dp)
            .background(if (isHandleHovered) DevPilotColors.accentMid else Color.Transparent)
            .hoverable(handleInteraction)
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    onResize((-dragAmount.y).toInt())
                }
            }
    )
}

@Composable
private fun ServiceSelectorChip(
    currentService: Service?,
    services: List<Service>,
    onServiceSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    Box {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(DevPilotRadius.sm))
                .clickable { expanded = true }
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val statusColor = when (currentService?.status) {
                ServiceStatus.RUNNING -> DevPilotColors.green
                ServiceStatus.STOPPED -> DevPilotColors.red
                else -> DevPilotColors.amber
            }
            Box(Modifier.size(8.dp).clip(androidx.compose.foundation.shape.CircleShape).background(statusColor))
            Spacer(Modifier.width(8.dp))
            Text(
                currentService?.name ?: "Select Service",
                style = MaterialTheme.typography.labelLarge.copy(fontFamily = FontFamily.Monospace),
                color = DevPilotColors.text0
            )
            Icon(Icons.Default.ArrowDropDown, null, tint = DevPilotColors.text3, modifier = Modifier.size(16.dp))
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(DevPilotColors.bg3).border(1.dp, DevPilotColors.border2)
        ) {
            services.forEach { s ->
                DropdownMenuItem(
                    text = {
                        Text(s.name, style = MaterialTheme.typography.bodyLarge.copy(fontFamily = FontFamily.Monospace))
                    },
                    onClick = {
                        onServiceSelected(s.id)
                        expanded = false
                    },
                    leadingIcon = {
                        val color = when (s.status) {
                            ServiceStatus.RUNNING -> DevPilotColors.green
                            ServiceStatus.STOPPED -> DevPilotColors.red
                            else -> DevPilotColors.amber
                        }
                        Box(Modifier.size(6.dp).clip(androidx.compose.foundation.shape.CircleShape).background(color))
                    }
                )
            }
        }
    }
}

@Composable
private fun LogSearchField(query: String, onQueryChange: (String) -> Unit) {
    Box(
        modifier = Modifier
            .width(220.dp)
            .height(26.dp)
            .clip(RoundedCornerShape(DevPilotRadius.sm))
            .background(DevPilotColors.bg1)
            .border(1.dp, DevPilotColors.border2, RoundedCornerShape(DevPilotRadius.sm))
            .padding(horizontal = 8.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Search, null, tint = DevPilotColors.text3, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(6.dp))
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                singleLine = true,
                textStyle = TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    color = DevPilotColors.text1
                ),
                decorationBox = { innerTextField ->
                    if (query.isEmpty()) {
                        Text(
                            "Search console...",
                            style = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 12.sp),
                            color = DevPilotColors.text3
                        )
                    }
                    innerTextField()
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun LogActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tooltip: String,
    isActive: Boolean = false,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier.size(28.dp).clip(RoundedCornerShape(DevPilotRadius.xs))
            .background(if (isActive) DevPilotColors.accentDim else Color.Transparent)
    ) {
        Icon(
            icon,
            contentDescription = tooltip,
            tint = if (isActive) DevPilotColors.accent else DevPilotColors.text2,
            modifier = Modifier.size(16.dp)
        )
    }
}

@Composable
private fun IntelliJLogLine(entry: LogEntry, searchQuery: String) {
    val levelColor = when (entry.level) {
        LogLevel.INFO -> Color(0xFF4B9CFF)
        LogLevel.WARN -> Color(0xFFFFAD33)
        LogLevel.ERROR -> Color(0xFFFF5574)
        LogLevel.DEBUG -> Color(0xFFA78BFA)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 1.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Timestamp
        Text(
            text = entry.timestamp,
            style = TextStyle(
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                color = DevPilotColors.text3
            ),
            modifier = Modifier.width(90.dp)
        )

        // Level Badge (IntelliJ Style)
        Box(
            modifier = Modifier
                .width(52.dp)
                .padding(end = 8.dp)
        ) {
            Text(
                text = entry.level.name,
                style = TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = levelColor.copy(alpha = 0.9f)
                ),
                modifier = Modifier
                    .clip(RoundedCornerShape(2.dp))
                    .background(levelColor.copy(alpha = 0.15f))
                    .padding(horizontal = 4.dp, vertical = 1.dp)
                    .align(Alignment.CenterStart)
            )
        }

        // Message
        val annotatedMessage = buildAnnotatedString {
            val msg = entry.message
            if (searchQuery.isNotBlank()) {
                var idx = 0
                val lower = msg.lowercase()
                val qLower = searchQuery.lowercase()
                while (idx < msg.length) {
                    val found = lower.indexOf(qLower, idx)
                    if (found == -1) {
                        append(msg.substring(idx))
                        break
                    }
                    append(msg.substring(idx, found))
                    withStyle(SpanStyle(background = Color(0xFFFFFF00).copy(alpha = 0.3f), color = Color.White)) {
                        append(msg.substring(found, found + searchQuery.length))
                    }
                    idx = found + searchQuery.length
                }
            } else {
                append(msg)
            }
        }

        Text(
            text = annotatedMessage,
            style = TextStyle(
                fontFamily = FontFamily.Monospace,
                fontSize = 12.5.sp,
                lineHeight = 18.sp,
                color = if (entry.level == LogLevel.ERROR) Color(0xFFFF5574) else DevPilotColors.text1
            ),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun ConsoleEmptyState(logServiceId: String?, searchQuery: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Default.Terminal,
                null,
                modifier = Modifier.size(48.dp),
                tint = DevPilotColors.bg3
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = when {
                    logServiceId == null -> "Select a service to view logs"
                    searchQuery.isNotBlank() -> "No matching results found"
                    else -> "Console is empty"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = DevPilotColors.text3
            )
        }
    }
}
