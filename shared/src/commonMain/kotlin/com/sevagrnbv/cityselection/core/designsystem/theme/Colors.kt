package com.sevagrnbv.cityselection.core.designsystem.theme

import androidx.compose.ui.graphics.Color

internal const val BRAND_COLOR_ARGB: Int = 0xFF804AFF.toInt()

/** Цвета текста из макета. */
internal object TextColors {
    val Primary = Color(0xFF25222B)
    val Secondary = Color(0xFF777381)
    val Tertiary = Color(0xFFA5A2AB)
}

/** Цвета основной кнопки в обычном и нажатом состояниях. */
internal object ButtonColors {
    val PrimaryBackground = Color(BRAND_COLOR_ARGB)
    val PrimaryBackgroundPressed = Color(0xFF683BD3)
    val PrimaryForeground = Color.White
}

/** Цвета иконок из макета. */
internal object IconColors {
    val Primary = Color(0xFF25222B)
}

/** Фоны интерфейса из макета. */
internal object BackgroundColors {
    val Primary = Color.White
    val Tertiary = Color(0xFFF6F6F7)
}

/** Тень фокуса поля из Figma: чёрный с непрозрачностью 12%. */
internal object EffectColors {
    val FocusShadow = Color(0x1F000000)
}

internal object ComponentColors {
    val Divider = Color(0xFFEFEFF0)
    val NavigationIndicator = Color(0xFFF5EDFF)
    val Skeleton = Color(0x14000000)
    val Handle = Color(0x1A000000)
}
