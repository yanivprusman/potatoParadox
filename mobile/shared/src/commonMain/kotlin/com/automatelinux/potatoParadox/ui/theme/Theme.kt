package com.automatelinux.potatoParadox.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val Water = Color(0xFF2F80C9)
val WaterDeep = Color(0xFF1D4F80)
val Potato = Color(0xFFEBC07E)
val Dry = Color(0xFFC98D45)
val DryDark = Color(0xFF8A5A22)
val Alarm = Color(0xFFD1492E)
val Paper = Color(0xFFFBF6EE)
val Card = Color(0xFFFFFFFF)
val Track = Color(0xFFEDE6DA)
val Ink = Color(0xFF2A2420)
val Muted = Color(0xFF7D7268)

private val Colors = lightColorScheme(
    primary = Water,
    onPrimary = Color.White,
    secondary = Dry,
    background = Paper,
    surface = Card,
    surfaceVariant = Track,
    onBackground = Ink,
    onSurface = Ink,
    onSurfaceVariant = Muted,
)

private val Type = Typography().let { t ->
    t.copy(
        displayMedium = t.displayMedium.copy(fontWeight = FontWeight.Black, letterSpacing = (-1).sp),
        headlineMedium = t.headlineMedium.copy(fontWeight = FontWeight.Black),
    )
}

// Multiplatform theme (no Android-only dynamic color, so it compiles for iOS too).
// Always light: the app is a sheet of paper with a potato on it.
@Composable
fun AppTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = Colors, typography = Type, content = content)
}
