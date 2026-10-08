package com.sevagrnbv.cityselection.core.designsystem.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import cityselection.shared.generated.resources.Res
import cityselection.shared.generated.resources.roboto_medium
import cityselection.shared.generated.resources.roboto_regular
import cityselection.shared.generated.resources.roboto_semibold
import org.jetbrains.compose.resources.Font

/** Заголовок экрана: Roboto SemiBold, 19/26, без межбуквенного интервала. */
@Composable
internal fun citySelectionTypography(): Typography = Typography(
    headlineSmall = TextStyle(
        fontFamily = FontFamily(Font(Res.font.roboto_semibold, weight = FontWeight.SemiBold)),
        fontWeight = FontWeight.SemiBold,
        fontSize = 23.sp,
        lineHeight = 26.sp,
        letterSpacing = 0.sp,
        textAlign = TextAlign.Center,
    ),
    bodyLarge = mediumTextStyle(),
    bodyMedium = TextStyle(
        fontFamily = FontFamily(Font(Res.font.roboto_regular, weight = FontWeight.Normal)),
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.sp,
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily(Font(Res.font.roboto_semibold, weight = FontWeight.SemiBold)),
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 26.sp,
        letterSpacing = 0.sp,
        textAlign = TextAlign.Center,
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily(Font(Res.font.roboto_semibold, weight = FontWeight.SemiBold)),
        fontWeight = FontWeight.SemiBold,
        fontSize = 19.sp,
        lineHeight = 26.sp,
        letterSpacing = 0.sp,
        textAlign = TextAlign.Center,
    ),
)

/** Основной текст: поле поиска, его подсказка и элементы списка. */
@Composable
private fun mediumTextStyle(): TextStyle = TextStyle(
    fontFamily = FontFamily(Font(Res.font.roboto_medium, weight = FontWeight.Medium)),
    fontWeight = FontWeight.Medium,
    fontSize = 16.sp,
    lineHeight = 22.sp,
    letterSpacing = 0.sp,
)
