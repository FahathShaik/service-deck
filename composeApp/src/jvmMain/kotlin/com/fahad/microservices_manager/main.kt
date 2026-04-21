package com.fahad.microservices_manager

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import com.fahad.microservices_manager.data.ServiceStorage
import com.fahad.microservices_manager.ui.App
import com.fahad.microservices_manager.ui.state.ServicesViewModel
import com.fahad.microservices_manager.ui.theme.DevPilotTheme
import javax.swing.UIManager

fun main() {
    try {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
    } catch (_: Exception) {}

    application {
        val manager = remember { DesktopServiceManager() }
        val storage = remember { ServiceStorage() }
        val scope = rememberCoroutineScope()
        val viewModel = remember { ServicesViewModel(manager, storage, scope) }

        Window(
            onCloseRequest = {
                if (viewModel.hasRunningServices()) {
                    viewModel.showExitConfirmation(true)
                } else {
                    exitApplication()
                }
            },
            title = "ServiceDeck — Professional Microservices Manager",
            state = WindowState(
                width = 1600.dp,
                height = 980.dp,
                position = WindowPosition.Aligned(androidx.compose.ui.Alignment.Center)
            )
        ) {
            val state = viewModel.state.collectAsState()
            
            val density = LocalDensity.current
            CompositionLocalProvider(
                LocalDensity provides Density(density.density, fontScale = 1.22f)
            ) {
                DevPilotTheme(themeMode = state.value.themeMode) {
                    App(viewModel, onExit = { exitApplication() })
                }
            }
        }
    }
}
