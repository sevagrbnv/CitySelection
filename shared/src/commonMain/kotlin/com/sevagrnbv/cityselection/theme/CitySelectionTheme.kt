package com.sevagrnbv.cityselection.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val colors = lightColorScheme(
    primary = Color(0xFF8247FF),
    onPrimary = Color.White,
    background = Color.White,
    surface = Color.White,
    surfaceContainerLow = Color(0xFFF6F5F8),
    outlineVariant = Color(0xFFEBEAEE),
    onSurface = Color(0xFF24212B),
    onSurfaceVariant = Color(0xFF827C8D),
)

@Composable
internal fun CitySelectionTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = colors, content = content)
}
