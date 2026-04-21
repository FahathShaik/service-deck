package com.fahad.microservices_manager.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import service_deck.composeapp.generated.resources.Res
import service_deck.composeapp.generated.resources.UbuntuSans_Variable
import org.jetbrains.compose.resources.Font as ResourceFont
import com.fahad.microservices_manager.ui.state.ThemeMode

data class DevPilotPalette(
    val bg0: Color,
    val bg1: Color,
    val bg2: Color,
    val bg3: Color,
    val bg4: Color,
    val bgHover: Color,
    val bgOverlay: Color,
    val border1: Color,
    val border2: Color,
    val border3: Color,
    val text0: Color,
    val text1: Color,
    val text2: Color,
    val text3: Color,
    val accent: Color,
    val accentDim: Color,
    val accentMid: Color,
    val blue: Color,
    val blueDim: Color,
    val blueMid: Color,
    val red: Color,
    val redDim: Color,
    val redMid: Color,
    val amber: Color,
    val amberDim: Color,
    val amberMid: Color,
    val green: Color,
    val greenDim: Color,
    val greenMid: Color,
    val purple: Color,
    val purpleDim: Color
)

private val LightPalette = DevPilotPalette(
    bg0 = Color(0xFFF5F8FC),
    bg1 = Color(0xFFEFF4FA),
    bg2 = Color(0xFFFFFFFF),
    bg3 = Color(0xFFDDEBFB),
    bg4 = Color(0xFFD1E3F8),
    bgHover = Color(0xFFE6F0FA),
    bgOverlay = Color(0xA8E7EEF7),
    border1 = Color(0x140E1A2B),
    border2 = Color(0x26101F35),
    border3 = Color(0x3B18304D),
    text0 = Color(0xFF132238),
    text1 = Color(0xFF3D536D),
    text2 = Color(0xFF6F839D),
    text3 = Color(0xFF98A7BA),
    accent = Color(0xFF00A886),
    accentDim = Color(0x1F00A886),
    accentMid = Color(0x4000A886),
    blue = Color(0xFF2F7EEA),
    blueDim = Color(0x1F2F7EEA),
    blueMid = Color(0x403F86F0),
    red = Color(0xFFD84263),
    redDim = Color(0x1AD84263),
    redMid = Color(0x33D84263),
    amber = Color(0xFFE1971E),
    amberDim = Color(0x1AE1971E),
    amberMid = Color(0x33E1971E),
    green = Color(0xFF1EAF67),
    greenDim = Color(0x1A1EAF67),
    greenMid = Color(0x331EAF67),
    purple = Color(0xFF7C63E6),
    purpleDim = Color(0x1A7C63E6)
)

private val DarkPalette = DevPilotPalette(
    bg0 = Color(0xFF06090F),
    bg1 = Color(0xFF0B1019),
    bg2 = Color(0xFF101723),
    bg3 = Color(0xFF16203A),
    bg4 = Color(0xFF1C2844),
    bgHover = Color(0xFF1A2540),
    bgOverlay = Color(0xB8040E0E),
    border1 = Color(0x0FFFFFFF),
    border2 = Color(0x1AFFFFFF),
    border3 = Color(0x29FFFFFF),
    text0 = Color(0xFFEAF0FF),
    text1 = Color(0xFFB0BCDA),
    text2 = Color(0xFF6B7A9E),
    text3 = Color(0xFF3D4E6F),
    accent = Color(0xFF00D4AA),
    accentDim = Color(0x1F00D4AA),
    accentMid = Color(0x4000D4AA),
    blue = Color(0xFF4B9CFF),
    blueDim = Color(0x1F4B9CFF),
    blueMid = Color(0x404B9CFF),
    red = Color(0xFFFF5574),
    redDim = Color(0x1AFF5574),
    redMid = Color(0x38FF5574),
    amber = Color(0xFFFFAD33),
    amberDim = Color(0x1AFFAD33),
    amberMid = Color(0x38FFAD33),
    green = Color(0xFF2DD881),
    greenDim = Color(0x1A2DD881),
    greenMid = Color(0x382DD881),
    purple = Color(0xFFA78BFA),
    purpleDim = Color(0x1AA78BFA)
)

private val LocalDevPilotPalette = staticCompositionLocalOf { LightPalette }

object DevPilotColors {
    val bg0: Color @Composable get() = LocalDevPilotPalette.current.bg0
    val bg1: Color @Composable get() = LocalDevPilotPalette.current.bg1
    val bg2: Color @Composable get() = LocalDevPilotPalette.current.bg2
    val bg3: Color @Composable get() = LocalDevPilotPalette.current.bg3
    val bg4: Color @Composable get() = LocalDevPilotPalette.current.bg4
    val bgHover: Color @Composable get() = LocalDevPilotPalette.current.bgHover
    val bgOverlay: Color @Composable get() = LocalDevPilotPalette.current.bgOverlay
    val border1: Color @Composable get() = LocalDevPilotPalette.current.border1
    val border2: Color @Composable get() = LocalDevPilotPalette.current.border2
    val border3: Color @Composable get() = LocalDevPilotPalette.current.border3
    val text0: Color @Composable get() = LocalDevPilotPalette.current.text0
    val text1: Color @Composable get() = LocalDevPilotPalette.current.text1
    val text2: Color @Composable get() = LocalDevPilotPalette.current.text2
    val text3: Color @Composable get() = LocalDevPilotPalette.current.text3
    val accent: Color @Composable get() = LocalDevPilotPalette.current.accent
    val accentDim: Color @Composable get() = LocalDevPilotPalette.current.accentDim
    val accentMid: Color @Composable get() = LocalDevPilotPalette.current.accentMid
    val blue: Color @Composable get() = LocalDevPilotPalette.current.blue
    val blueDim: Color @Composable get() = LocalDevPilotPalette.current.blueDim
    val blueMid: Color @Composable get() = LocalDevPilotPalette.current.blueMid
    val red: Color @Composable get() = LocalDevPilotPalette.current.red
    val redDim: Color @Composable get() = LocalDevPilotPalette.current.redDim
    val redMid: Color @Composable get() = LocalDevPilotPalette.current.redMid
    val amber: Color @Composable get() = LocalDevPilotPalette.current.amber
    val amberDim: Color @Composable get() = LocalDevPilotPalette.current.amberDim
    val amberMid: Color @Composable get() = LocalDevPilotPalette.current.amberMid
    val green: Color @Composable get() = LocalDevPilotPalette.current.green
    val greenDim: Color @Composable get() = LocalDevPilotPalette.current.greenDim
    val greenMid: Color @Composable get() = LocalDevPilotPalette.current.greenMid
    val purple: Color @Composable get() = LocalDevPilotPalette.current.purple
    val purpleDim: Color @Composable get() = LocalDevPilotPalette.current.purpleDim
}

object DevPilotSpacing {
    val xs = 6.dp
    val sm = 8.dp
    val md = 10.dp
    val lg = 14.dp
    val xl = 20.dp
    val xxl = 24.dp
}

object DevPilotRadius {
    val xs = 4.dp
    val sm = 6.dp
    val md = 8.dp
    val lg = 12.dp
    val xl = 16.dp
}

@Composable
fun DevPilotTheme(
    themeMode: ThemeMode = ThemeMode.LIGHT,
    content: @Composable () -> Unit
) {
    val uiFontFamily = FontFamily(
        ResourceFont(Res.font.UbuntuSans_Variable, weight = FontWeight.Normal),
        ResourceFont(Res.font.UbuntuSans_Variable, weight = FontWeight.Medium),
        ResourceFont(Res.font.UbuntuSans_Variable, weight = FontWeight.SemiBold),
        ResourceFont(Res.font.UbuntuSans_Variable, weight = FontWeight.Bold)
    )

    val palette = if (themeMode == ThemeMode.DARK) DarkPalette else LightPalette

    val colorScheme = if (themeMode == ThemeMode.DARK) {
        darkColorScheme(
            primary = palette.accent,
            background = palette.bg0,
            surface = palette.bg2,
            onBackground = palette.text0,
            onSurface = palette.text0,
            outline = palette.border2
        )
    } else {
        lightColorScheme(
            primary = palette.accent,
            background = palette.bg0,
            surface = palette.bg2,
            onBackground = palette.text0,
            onSurface = palette.text0,
            outline = palette.border2
        )
    }

    val typography = Typography(
        headlineMedium = TextStyle(
            fontFamily = uiFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 34.sp
        ),
        titleLarge = TextStyle(
            fontFamily = uiFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 18.sp
        ),
        bodyLarge = TextStyle(
            fontFamily = uiFontFamily,
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal,
            color = DevPilotColors.text0
        ),
        bodyMedium = TextStyle(
            fontFamily = uiFontFamily,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = DevPilotColors.text1
        ),
        labelLarge = TextStyle(
            fontFamily = uiFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            letterSpacing = 0.05.sp
        ),
        labelMedium = TextStyle(
            fontFamily = uiFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 11.5.sp,
            letterSpacing = 0.08.sp
        ),
        labelSmall = TextStyle(
            fontFamily = uiFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 11.sp,
            letterSpacing = 0.05.sp
        )
    )

    CompositionLocalProvider(LocalDevPilotPalette provides palette) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = typography,
            content = content
        )
    }
}
