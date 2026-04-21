package com.fahad.microservices_manager.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fahad.microservices_manager.ui.state.ToastData
import com.fahad.microservices_manager.ui.state.ToastType
import com.fahad.microservices_manager.ui.theme.DevPilotColors
import com.fahad.microservices_manager.ui.theme.DevPilotRadius

@Composable
fun ToastStack(
    toasts: List<ToastData>,
    onDismiss: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.End
    ) {
        toasts.forEach { toast ->
            key(toast.id) {
                AnimatedVisibility(
                    visible = true,
                    enter = slideInVertically(
                        initialOffsetY = { it },
                        animationSpec = tween(350)
                    ) + fadeIn(animationSpec = tween(350)) + scaleIn(initialScale = 0.95f, animationSpec = tween(350)),
                    exit = slideOutVertically(
                        targetOffsetY = { -it },
                        animationSpec = tween(300)
                    ) + fadeOut(animationSpec = tween(300)) + scaleOut(targetScale = 0.95f, animationSpec = tween(300))
                ) {
                    ToastItem(toast = toast, onDismiss = { onDismiss(toast.id) })
                }
            }
        }
    }
}

@Composable
fun ToastItem(toast: ToastData, onDismiss: () -> Unit) {
    val (iconVector, iconBg, iconTint) = when (toast.type) {
        ToastType.SUCCESS -> Triple(Icons.Default.CheckCircle, DevPilotColors.greenDim, DevPilotColors.green)
        ToastType.ERROR -> Triple(Icons.Default.Error, DevPilotColors.redDim, DevPilotColors.red)
        ToastType.WARNING -> Triple(Icons.Default.Warning, DevPilotColors.amberDim, DevPilotColors.amber)
        ToastType.INFO -> Triple(Icons.Default.Info, DevPilotColors.blueDim, DevPilotColors.blue)
    }

    Row(
        modifier = Modifier
            .widthIn(min = 260.dp, max = 400.dp)
            .clip(RoundedCornerShape(DevPilotRadius.lg))
            .background(DevPilotColors.bg3)
            .border(1.dp, DevPilotColors.border2, RoundedCornerShape(DevPilotRadius.lg))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = iconVector,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(12.dp)
            )
        }

        Text(
            text = toast.message,
            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 14.sp),
            color = DevPilotColors.text0,
            modifier = Modifier.weight(1f)
        )

        IconButton(
            onClick = onDismiss,
            modifier = Modifier.size(20.dp)
        ) {
            Icon(
                Icons.Default.Close,
                contentDescription = "Dismiss",
                tint = DevPilotColors.text3,
                modifier = Modifier.size(12.dp)
            )
        }
    }
}
