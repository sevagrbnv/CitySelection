package com.sevagrnbv.cityselection.core.designsystem.theme

import androidx.compose.ui.unit.dp

/** Общая шкала отступов. Значения не зависят от ширины окна. */
internal object Spacing {
    val tiny = 2.dp
    val extraSmall = 4.dp
    val fieldVertical = 10.dp
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
    val status = 88.dp
}

/** Радиусы скруглений компонентов. */
internal object CornerRadius {
    val small = 8.dp
    val medium = 12.dp
    val large = 16.dp
}

/** Размеры основных кнопок. */
internal object ButtonSize {
    val primaryHeight = 56.dp
}

/** Минимальные размеры поля; высота может расти вместе с системным шрифтом. */
internal object InputSize {
    val minimumHeight = 56.dp
    val borderWidth = 1.5.dp
}

/** Параметры тени фокуса из макета. */
internal object FocusShadow {
    val radius = 8.dp
    val offsetY = 2.dp
}

/** Геометрия компонентов из макета; размеры текста и контента могут расти. */
internal object ComponentSize {
    val headerTitleHorizontalPadding = 64.dp
    val navigationHeight = 64.dp
    val rowMinimumHeight = 54.dp
    val sheetRadius = 24.dp
    val sheetHandleWidth = 48.dp
    val sheetHandleHeight = 4.dp
    val zoomWidth = 52.dp
    val zoomHeight = 96.dp
    val zoomRadius = 20.dp
}
