package com.fahad.microservices_manager.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CallSplit
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fahad.microservices_manager.domain.Project
import com.fahad.microservices_manager.ui.state.ThemeMode
import com.fahad.microservices_manager.ui.theme.DevPilotColors

@Composable
fun Topbar(
    selectedProject: Project?,
    themeMode: ThemeMode,
    onToggleThemeMode: () -> Unit,
    sleepIdleEnabled: Boolean,
    onToggleSleepIdle: () -> Unit,
    onShowShortcuts: () -> Unit,
    onShowBranchInfo: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp)
            .background(DevPilotColors.bg0)
            .border(width = (0.5).dp, color = DevPilotColors.border1, shape = androidx.compose.foundation.shape.RoundedCornerShape(0.dp))
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = selectedProject?.name ?: "No Project",
                style = MaterialTheme.typography.bodyLarge,
                color = DevPilotColors.text0
            )
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = DevPilotColors.text3,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = "main",
                style = MaterialTheme.typography.bodyLarge,
                color = DevPilotColors.text2
            )
        }

        Spacer(Modifier.width(12.dp))

        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(999.dp))
                .background(DevPilotColors.blueDim)
                .border(1.dp, DevPilotColors.blueMid, RoundedCornerShape(999.dp))
                .clickable(onClick = onShowBranchInfo)
                .padding(horizontal = 11.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                Icons.Default.CallSplit,
                contentDescription = null,
                tint = DevPilotColors.blue,
                modifier = Modifier.size(14.dp)
            )
            Text(
                "main",
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 11.5.sp),
                color = DevPilotColors.blue
            )
        }

        Spacer(Modifier.weight(1f))

        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(18.dp)) {
            IconButton(onClick = onToggleThemeMode, modifier = Modifier.size(32.dp)) {
                Icon(
                    imageVector = if (themeMode == ThemeMode.DARK) Icons.Default.LightMode else Icons.Default.DarkMode,
                    contentDescription = "Toggle theme",
                    tint = DevPilotColors.text2,
                    modifier = Modifier.size(18.dp)
                )
            }

            Text(
                "Sleep idle",
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.5.sp),
                color = DevPilotColors.text2
            )

            Row(
                modifier = Modifier
                    .width(40.dp)
                    .height(22.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(if (sleepIdleEnabled) DevPilotColors.accentDim else DevPilotColors.bg3)
                    .border(
                        1.dp,
                        if (sleepIdleEnabled) DevPilotColors.accentMid else DevPilotColors.border2,
                        RoundedCornerShape(999.dp)
                    )
                    .clickable(onClick = onToggleSleepIdle)
                    .padding(horizontal = 3.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = if (sleepIdleEnabled) Arrangement.End else Arrangement.Start
            ) {
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .background(
                            if (sleepIdleEnabled) DevPilotColors.accent else DevPilotColors.text2,
                            CircleShape
                        )
                )
            }

            IconButton(onClick = onShowShortcuts, modifier = Modifier.size(32.dp)) {
                Icon(
                    Icons.Default.Keyboard,
                    contentDescription = "Keyboard shortcuts",
                    tint = DevPilotColors.text2,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
