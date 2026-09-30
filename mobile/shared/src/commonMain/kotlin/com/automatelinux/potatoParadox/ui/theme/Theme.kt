package com.automatelinux.potatoParadox.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Water = Color(0xFF2F80C9)
val Dry = Color(0xFFA0703A)

private val LightColors = lightColorScheme(
    primary = Water,
    secondary = Dry,
    background = Color(0xFFFBF8F3),
    surfaceVariant = Color(0xFFF1EBE1),
)

// Multiplatform theme (no Android-only dynamic color, so it compiles for iOS too).
@Composable
fun AppTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = LightColors, content = content)
}
