package com.fahad.microservices_manager.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.fahad.microservices_manager.domain.Service
import com.fahad.microservices_manager.ui.theme.DevPilotColors
import com.fahad.microservices_manager.ui.theme.DevPilotRadius
import java.io.File
import javax.swing.JFileChooser

@Composable
fun AddServiceModal(
    onDismiss: () -> Unit,
    onDetectServices: (path: String) -> List<Service>,
    onAddMultiple: (services: List<Service>) -> Unit
) {
    var path by remember { mutableStateOf("") }
    val discoveredServices = remember { mutableStateListOf<Service>() }
    val selectedServiceIds = remember { mutableStateListOf<String>() }
    
    // For manual override if only one is found or none
    var name by remember { mutableStateOf("") }
    var port by remember { mutableStateOf("") }
    var framework by remember { mutableStateOf("Spring Boot") }
    var jvmArgs by remember { mutableStateOf("") }
    var dependencyInput by remember { mutableStateOf("") }
    val dependencies = remember { mutableStateListOf<String>() }
    var frameworkMenuExpanded by remember { mutableStateOf(false) }
    var showAdvanced by remember { mutableStateOf(false) }

    val frameworks = listOf("Spring Boot", "Quarkus", "Micronaut", "Maven", "Gradle")

    fun updateFromPath(newPath: String) {
        path = newPath
        if (newPath.isNotBlank()) {
            val detected = onDetectServices(newPath)
            discoveredServices.clear()
            discoveredServices.addAll(detected)
            selectedServiceIds.clear()
            selectedServiceIds.addAll(detected.map { it.id })
            
            if (detected.size == 1) {
                val s = detected[0]
                name = s.name
                port = s.port.toString()
                framework = s.framework
            } else if (detected.isEmpty()) {
                name = File(newPath).name
                port = "8080"
                framework = "Spring Boot"
            }
        }
    }

    fun addDependency(raw: String) {
        val value = raw.trim()
        if (value.isBlank() || value in dependencies) return
        dependencies += value
        dependencyInput = ""
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.width(560.dp),
            colors = CardDefaults.cardColors(containerColor = DevPilotColors.bg2),
            shape = RoundedCornerShape(DevPilotRadius.xl),
            border = BorderStroke(1.dp, DevPilotColors.border2)
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Add services",
                        style = MaterialTheme.typography.titleLarge,
                        color = DevPilotColors.text0,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Close",
                            tint = DevPilotColors.text2,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(DevPilotColors.border1)
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    ModalField(
                        label = "Project or Service Path",
                        value = path,
                        onValueChange = { updateFromPath(it) },
                        placeholder = "/path/to/project/root",
                        trailing = {
                            IconButton(onClick = { pickFolder()?.let { updateFromPath(it) } }) {
                                Icon(
                                    Icons.Default.FolderOpen,
                                    contentDescription = "Select folder",
                                    tint = DevPilotColors.text2
                                )
                            }
                        }
                    )

                    if (discoveredServices.size > 1) {
                        Text(
                            "Discovered ${discoveredServices.size} services. Select to add:",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = DevPilotColors.text1
                        )
                        
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(DevPilotRadius.md))
                                .background(DevPilotColors.bg1)
                                .border(1.dp, DevPilotColors.border2, RoundedCornerShape(DevPilotRadius.md))
                        ) {
                            discoveredServices.forEach { service ->
                                val isSelected = service.id in selectedServiceIds
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { 
                                            if (isSelected) selectedServiceIds.remove(service.id) 
                                            else selectedServiceIds.add(service.id)
                                        }
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = isSelected,
                                        onCheckedChange = null,
                                        colors = CheckboxDefaults.colors(
                                            checkedColor = DevPilotColors.accent,
                                            uncheckedColor = DevPilotColors.text3,
                                            checkmarkColor = DevPilotColors.bg0
                                        ),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            service.name,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontFamily = FontFamily.Monospace,
                                                fontWeight = FontWeight.Bold
                                            ),
                                            color = DevPilotColors.text0
                                        )
                                        Text(
                                            "${service.framework} · Port ${service.port}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = DevPilotColors.text2
                                        )
                                    }
                                }
                                if (service != discoveredServices.last()) {
                                    Box(Modifier.fillMaxWidth().height(1.dp).background(DevPilotColors.border1))
                                }
                            }
                        }
                    } else {
                        // Single service form (existing)
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            ModalField(
                                label = "Service Name",
                                value = name,
                                onValueChange = { name = it },
                                placeholder = "e.g. user-service",
                                modifier = Modifier.weight(1.2f)
                            )

                            ModalField(
                                label = "Port",
                                value = port,
                                onValueChange = { port = it.filter(Char::isDigit) },
                                placeholder = "8080",
                                modifier = Modifier.weight(0.8f),
                                keyboardType = KeyboardType.Number
                            )
                        }

                        TextButton(
                            onClick = { showAdvanced = !showAdvanced },
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    if (showAdvanced) Icons.Default.Close else Icons.Default.KeyboardArrowDown,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(if (showAdvanced) "Hide Advanced Options" else "Show Advanced Options", fontSize = 12.sp)
                            }
                        }

                        if (showAdvanced) {
                            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                Column {
                                    Text(
                                        "Framework",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 11.5.sp),
                                        color = DevPilotColors.text1
                                    )
                                    Spacer(Modifier.height(6.dp))
                                    Box {
                                        OutlinedTextField(
                                            value = framework,
                                            onValueChange = {},
                                            readOnly = true,
                                            modifier = Modifier.fillMaxWidth(),
                                            trailingIcon = {
                                                IconButton(onClick = { frameworkMenuExpanded = true }) {
                                                    Icon(
                                                        Icons.Default.KeyboardArrowDown,
                                                        contentDescription = null,
                                                        tint = DevPilotColors.text2
                                                    )
                                                }
                                            },
                                            colors = modalFieldColors()
                                        )
                                        DropdownMenu(
                                            expanded = frameworkMenuExpanded,
                                            onDismissRequest = { frameworkMenuExpanded = false },
                                            modifier = Modifier
                                                .background(DevPilotColors.bg3)
                                                .border(
                                                    1.dp,
                                                    DevPilotColors.border2,
                                                    RoundedCornerShape(DevPilotRadius.md)
                                                )
                                        ) {
                                            frameworks.forEach { item ->
                                                DropdownMenuItem(
                                                    text = { Text(item, color = DevPilotColors.text0) },
                                                    onClick = {
                                                        framework = item
                                                        frameworkMenuExpanded = false
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }

                                Column {
                                    Text(
                                        "Dependencies",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 11.5.sp),
                                        color = DevPilotColors.text1
                                    )
                                    Spacer(Modifier.height(6.dp))
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(DevPilotRadius.md))
                                            .background(DevPilotColors.bg1)
                                            .border(
                                                1.dp,
                                                DevPilotColors.border2,
                                                RoundedCornerShape(DevPilotRadius.md)
                                            )
                                            .padding(10.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        if (dependencies.isNotEmpty()) {
                                            FlowRow(
                                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                verticalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                dependencies.forEach { dep ->
                                                    Row(
                                                        modifier = Modifier
                                                            .clip(RoundedCornerShape(999.dp))
                                                            .background(DevPilotColors.blueDim)
                                                            .border(
                                                                1.dp,
                                                                DevPilotColors.blueMid,
                                                                RoundedCornerShape(999.dp)
                                                            )
                                                            .padding(
                                                                start = 8.dp,
                                                                end = 4.dp,
                                                                top = 4.dp,
                                                                bottom = 4.dp
                                                            ),
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Text(
                                                            dep,
                                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                                fontFamily = FontFamily.Monospace,
                                                                fontSize = 10.sp
                                                            ),
                                                            color = DevPilotColors.blue
                                                        )
                                                        Spacer(Modifier.width(4.dp))
                                                        IconButton(
                                                            onClick = { dependencies.remove(dep) },
                                                            modifier = Modifier.size(16.dp)
                                                        ) {
                                                            Icon(
                                                                Icons.Default.Close,
                                                                contentDescription = "Remove dependency",
                                                                tint = DevPilotColors.blue,
                                                                modifier = Modifier.size(10.dp)
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }

                                        OutlinedTextField(
                                            value = dependencyInput,
                                            onValueChange = { dependencyInput = it },
                                            placeholder = {
                                                Text(
                                                    "Type & press Enter",
                                                    color = DevPilotColors.text3
                                                )
                                            },
                                            singleLine = true,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .onPreviewKeyEvent {
                                                    if (it.type == KeyEventType.KeyUp && it.key == Key.Enter) {
                                                        addDependency(dependencyInput)
                                                        true
                                                    } else {
                                                        false
                                                    }
                                                },
                                            colors = modalFieldColors()
                                        )
                                    }
                                }

                                ModalField(
                                    label = "JVM arguments",
                                    value = jvmArgs,
                                    onValueChange = { jvmArgs = it },
                                    placeholder = "-Xmx512m -Dspring.profiles.active=dev"
                                )
                            }
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(DevPilotColors.border1)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = DevPilotColors.text2)
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (discoveredServices.size > 1) {
                                val selected = discoveredServices.filter { it.id in selectedServiceIds }
                                onAddMultiple(selected)
                            } else {
                                val finalName = if (name.isBlank()) File(path).name else name.trim()
                                val finalPort = port.toIntOrNull() ?: 8080
                                val service = Service(
                                    id = java.util.UUID.randomUUID().toString(),
                                    name = finalName,
                                    port = finalPort,
                                    framework = framework,
                                    status = com.fahad.microservices_manager.domain.ServiceStatus.STOPPED,
                                    path = path.trim(),
                                    dependencies = dependencies.toList(),
                                    jvmArgs = jvmArgs.trim()
                                )
                                onAddMultiple(listOf(service))
                            }
                        },
                        enabled = path.isNotBlank() && (discoveredServices.size <= 1 || selectedServiceIds.isNotEmpty()),
                        modifier = Modifier.height(44.dp).padding(horizontal = 12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DevPilotColors.accent,
                            contentColor = DevPilotColors.bg0,
                            disabledContainerColor = DevPilotColors.bg3,
                            disabledContentColor = DevPilotColors.text3
                        )
                    ) {
                        Text(if (discoveredServices.size > 1) "Add Selected (${selectedServiceIds.size})" else "Add Service")
                    }
                }
            }
        }
    }
}

@Composable
private fun ModalField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
    trailing: @Composable (() -> Unit)? = null
) {
    Column(modifier = modifier) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 11.5.sp),
            color = DevPilotColors.text1
        )
        Spacer(Modifier.height(6.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, color = DevPilotColors.text3) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            trailingIcon = trailing,
            modifier = Modifier.fillMaxWidth(),
            colors = modalFieldColors()
        )
    }
}

@Composable
private fun modalFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = DevPilotColors.text0,
    unfocusedTextColor = DevPilotColors.text1,
    focusedBorderColor = DevPilotColors.accentMid,
    unfocusedBorderColor = DevPilotColors.border2,
    cursorColor = DevPilotColors.accent,
    focusedContainerColor = DevPilotColors.bg1,
    unfocusedContainerColor = DevPilotColors.bg1
)

private fun pickFolder(): String? {
    val chooser = JFileChooser()
    chooser.fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
    chooser.dialogTitle = "Select Project or Service Folder"
    val result = chooser.showOpenDialog(null)
    return if (result == JFileChooser.APPROVE_OPTION) chooser.selectedFile.absolutePath else null
}
