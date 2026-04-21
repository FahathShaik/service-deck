package com.fahad.microservices_manager

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class WindowLayoutMode {
    COMPACT,
    MEDIUM,
    EXPANDED
}

fun windowLayoutModeFor(width: Dp): WindowLayoutMode = when {
    width < 1120.dp -> WindowLayoutMode.COMPACT
    width < 1440.dp -> WindowLayoutMode.MEDIUM
    else -> WindowLayoutMode.EXPANDED
}
