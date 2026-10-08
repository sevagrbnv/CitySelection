package com.sevagrnbv.cityselection.core.designsystem.theme

/**
 * Оформление нативных пинов Android и iOS. Размеры заданы в dp/points, цвета — в ARGB.
 * Платформенный рендерер учитывает плотность экрана; ширина плашки зависит от текста.
 * Публичный доступ нужен Swift-адаптеру карты.
 */
object CityPinStyle {
    val backgroundArgb: Int = 0xFF4A4557.toInt()
    val selectedBackgroundArgb: Int = BRAND_COLOR_ARGB
    val foregroundArgb: Int = 0xFFFFFFFF.toInt()
    val shadowArgb: Int = 0x26000000

    val fontSize = 12f
    val bodyHeight = 30f
    val horizontalPadding = 8f
    val minimumWidth = 44f
    val tailHeight = 7f
    val tailHalfWidth = 7f
    val tailControlOffset = 3f
    /** Смещение управляющих точек кривой боковых скруглений исходного контура. */
    val capsuleControlOffset = 4.33f
    val strokeWidth = 1f
    val imagePadding = 4f
    val shadowRadius = 1.5f
    val shadowOffsetY = 2f
    val iconOffsetY = imagePadding
    val hitTestRadius = 12f
}
