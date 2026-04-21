package com.fahad.microservices_manager.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.fahad.microservices_manager.domain.Service
import com.fahad.microservices_manager.ui.theme.DevPilotColors
import com.fahad.microservices_manager.ui.theme.DevPilotRadius

@Composable
fun PortConflictModal(
    service: Service,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.width(440.dp),
            colors = CardDefaults.cardColors(containerColor = DevPilotColors.bg2),
            shape = RoundedCornerShape(DevPilotRadius.xl),
            border = BorderStroke(1.dp, DevPilotColors.border2)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(DevPilotColors.redDim, RoundedCornerShape(DevPilotRadius.lg)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = null,
                        tint = DevPilotColors.red,
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                Spacer(Modifier.height(16.dp))
                
                Text(
                    "Port Conflict",
                    style = MaterialTheme.typography.titleLarge,
                    color = DevPilotColors.text0
                )
                
                Spacer(Modifier.height(8.dp))
                
                Text(
                    "Port ${service.port} is already in use by another process. Would you like to kill it and start ${service.name}?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = DevPilotColors.text2,
                    modifier = Modifier.padding(horizontal = 8.dp),
                    textAlign = TextAlign.Center
                )
                
                Spacer(Modifier.height(24.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(DevPilotRadius.md)
                    ) {
                        Text("Cancel", color = DevPilotColors.text2)
                    }
                    
                    Button(
                        onClick = onConfirm,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DevPilotColors.accent,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(DevPilotRadius.md)
                    ) {
                        Text("Kill & Start")
                    }
                }
            }
        }
    }
}
