package com.fahad.microservices_manager.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.fahad.microservices_manager.ui.theme.DevPilotColors
import com.fahad.microservices_manager.ui.theme.DevPilotRadius
import java.io.File
import javax.swing.JFileChooser

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun AddProjectModal(
    onDismiss: () -> Unit,
    onAdd: (name: String, path: String, color: Long) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var path by remember { mutableStateOf("") }

    val colors = listOf(
        0xFF00D4AA, // Teal/Accent
        0xFF4B9CFF, // Blue
        0xFFA78BFA, // Purple
        0xFFFF5574, // Red
        0xFFFFAD33, // Amber
        0xFF2DD881  // Green
    )
    var selectedColor by remember { mutableStateOf(colors[0]) }

    fun processPath(newPath: String) {
        val file = File(newPath)
        if (file.exists() && file.isDirectory) {
            path = file.absolutePath
            if (name.isBlank()) {
                name = file.name
            }
        }
    }

    fun selectFolder() {
        val chooser = JFileChooser()
        chooser.fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
        chooser.dialogTitle = "Select Project Root Folder"
        val result = chooser.showOpenDialog(null)
        if (result == JFileChooser.APPROVE_OPTION) {
            processPath(chooser.selectedFile.absolutePath)
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .width(460.dp)
                .wrapContentHeight(),
            colors = CardDefaults.cardColors(containerColor = DevPilotColors.bg2),
            shape = RoundedCornerShape(DevPilotRadius.xl),
            border = BorderStroke(1.dp, DevPilotColors.border2)
        ) {
            Column(modifier = Modifier.padding(28.dp)) {
                Text(
                    "Open Project",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = DevPilotColors.text0
                )
                
                Text(
                    "Select a root folder containing your services",
                    style = MaterialTheme.typography.bodyMedium,
                    color = DevPilotColors.text2
                )
                
                Spacer(Modifier.height(24.dp))

                // Modern "Drop Zone" / Opener
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .clip(RoundedCornerShape(DevPilotRadius.lg))
                        .background(if (path.isEmpty()) DevPilotColors.bg1 else DevPilotColors.accentDim)
                        .pointerHoverIcon(PointerIcon.Hand)
                        .clickable { selectFolder() }
                        .padding(2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val borderColor = if (path.isEmpty()) DevPilotColors.border2 else DevPilotColors.accent
                    val cornerRadius = DevPilotRadius.lg
                    
                    androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                        val stroke = Stroke(width = 2f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f))
                        drawRoundRect(
                            color = borderColor,
                            style = stroke,
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius.toPx())
                        )
                    }
                    
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Icon(
                            if (path.isEmpty()) Icons.Default.CreateNewFolder else Icons.Default.Folder,
                            contentDescription = null,
                            tint = if (path.isEmpty()) DevPilotColors.text3 else DevPilotColors.accent,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = if (path.isEmpty()) "Click to select project folder" else path,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (path.isEmpty()) DevPilotColors.text2 else DevPilotColors.text0,
                            textAlign = TextAlign.Center,
                            maxLines = 2
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Display Name", color = DevPilotColors.text3) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(DevPilotRadius.md),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = DevPilotColors.text0,
                        unfocusedTextColor = DevPilotColors.text1,
                        focusedBorderColor = DevPilotColors.accent,
                        unfocusedBorderColor = DevPilotColors.border2,
                        cursorColor = DevPilotColors.accent,
                        focusedContainerColor = DevPilotColors.bg1,
                        unfocusedContainerColor = DevPilotColors.bg1
                    )
                )

                Spacer(Modifier.height(20.dp))

                Text("Theme Color", style = MaterialTheme.typography.labelMedium, color = DevPilotColors.text1)
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    colors.forEach { color ->
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(Color(color))
                                .border(
                                    width = if (selectedColor == color) 2.dp else 0.dp,
                                    color = if (selectedColor == color) DevPilotColors.text0 else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable { selectedColor = color }
                        )
                    }
                }

                Spacer(Modifier.height(32.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(DevPilotRadius.md)
                    ) {
                        Text("Cancel", color = DevPilotColors.text2)
                    }
                    Spacer(Modifier.width(12.dp))
                    Button(
                        onClick = { onAdd(name, path, selectedColor) },
                        enabled = name.isNotEmpty() && path.isNotEmpty(),
                        modifier = Modifier.height(44.dp).padding(horizontal = 8.dp),
                        shape = RoundedCornerShape(DevPilotRadius.md),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DevPilotColors.accent,
                            contentColor = DevPilotColors.bg0,
                            disabledContainerColor = DevPilotColors.bg3,
                            disabledContentColor = DevPilotColors.text3
                        )
                    ) {
                        Text("Open Project", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
