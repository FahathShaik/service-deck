package com.fahad.microservices_manager

import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertEquals

class ComposeAppDesktopTest {

    @Test
    fun `window layout mode uses compact breakpoint below 1120dp`() {
        assertEquals(WindowLayoutMode.COMPACT, windowLayoutModeFor(1119.dp))
    }

    @Test
    fun `window layout mode uses medium breakpoint below 1440dp`() {
        assertEquals(WindowLayoutMode.MEDIUM, windowLayoutModeFor(1280.dp))
    }

    @Test
    fun `window layout mode uses expanded breakpoint at and above 1440dp`() {
        assertEquals(WindowLayoutMode.EXPANDED, windowLayoutModeFor(1440.dp))
    }
}
