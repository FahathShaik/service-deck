package com.fahad.microservices_manager.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fahad.microservices_manager.domain.ServiceStatus
import com.fahad.microservices_manager.ui.theme.DevPilotColors

@Composable
fun StatusBadge(status: ServiceStatus) {
    val (bgColor, textColor, borderColor) = when (status) {
        ServiceStatus.STOPPED -> Triple(DevPilotColors.redDim, DevPilotColors.red, DevPilotColors.redMid)
        ServiceStatus.STARTING -> Triple(DevPilotColors.amberDim, DevPilotColors.amber, DevPilotColors.amberMid)
        ServiceStatus.STOPPING -> Triple(DevPilotColors.amberDim, DevPilotColors.amber, DevPilotColors.amberMid)
        ServiceStatus.RUNNING -> Triple(DevPilotColors.greenDim, DevPilotColors.green, DevPilotColors.greenMid)
        ServiceStatus.ERROR -> Triple(DevPilotColors.redDim, DevPilotColors.red, DevPilotColors.redMid)
        ServiceStatus.BUILDING -> Triple(DevPilotColors.blueDim, DevPilotColors.blue, DevPilotColors.blueMid)
    }

    val alpha = if (status == ServiceStatus.STARTING || status == ServiceStatus.STOPPING) {
        val infiniteTransition = rememberInfiniteTransition()
        val pulseAlpha by infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 0.5f,
            animationSpec = infiniteRepeatable(
                animation = tween(1500, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            )
        )
        pulseAlpha
    } else 1f

    Box(
        modifier = Modifier
            .alpha(alpha)
            .clip(RoundedCornerShape(999.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(999.dp))
            .padding(horizontal = 7.dp, vertical = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = status.name,
            style = MaterialTheme.typography.labelSmall,
            color = textColor
        )
    }
}

@Composable
fun DependencyChip(name: String, isRunning: Boolean) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(DevPilotColors.blueDim)
            .padding(horizontal = 8.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(5.dp)
                .clip(CircleShape)
                .background(if (isRunning) DevPilotColors.green else DevPilotColors.red)
        )
        Text(
            text = name,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                fontSize = 11.5.sp,
                color = DevPilotColors.blue
            ),
            modifier = Modifier.padding(start = 6.dp)
        )
    }
}
