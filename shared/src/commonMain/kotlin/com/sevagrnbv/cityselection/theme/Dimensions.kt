package com.sevagrnbv.cityselection.theme

import androidx.compose.ui.unit.dp

/** Общая шкала отступов. Значения не зависят от ширины окна. */
internal object Spacing {
    val extraSmall = 4.dp
    val small = 8.dp
    val compact = 12.dp
    val medium = 16.dp
    val large = 24.dp
    val extraLarge = 32.dp
}

/** Размеры иконок; область нажатия задаётся компонентом отдельно. */
internal object IconSize {
    val small = 20.dp
    val standard = 24.dp
}

/** Радиусы скруглений компонентов. */
internal object CornerRadius {
    val small = 8.dp
    val medium = 12.dp
}
