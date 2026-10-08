package com.sevagrnbv.cityselection.core.designsystem.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val colors = lightColorScheme(
    primary = ButtonColors.PrimaryBackground,
    onPrimary = Color.White,
    background = Color.White,
    surface = Color.White,
    surfaceContainerLow = Color(0xFFF6F5F8),
    outlineVariant = Color(0xFFEBEAEE),
    onSurface = TextColors.Primary,
    onSurfaceVariant = Color(0xFF827C8D),
)

@Composable
internal fun CitySelectionTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = colors, typography = citySelectionTypography(), content = content)
}
