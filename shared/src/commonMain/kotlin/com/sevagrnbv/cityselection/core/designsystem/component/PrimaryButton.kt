package com.sevagrnbv.cityselection.core.designsystem.component

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.sevagrnbv.cityselection.core.designsystem.theme.ButtonColors
import com.sevagrnbv.cityselection.core.designsystem.theme.ButtonSize
import com.sevagrnbv.cityselection.core.designsystem.theme.CornerRadius
import com.sevagrnbv.cityselection.core.designsystem.theme.Spacing

/** Основная кнопка; минимальная высота растёт при увеличенном системном шрифте. */
@Composable
internal fun PrimaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    Button(
        onClick = onClick,
        interactionSource = interactionSource,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (pressed) ButtonColors.PrimaryBackgroundPressed else ButtonColors.PrimaryBackground,
            contentColor = ButtonColors.PrimaryForeground,
        ),
        modifier = modifier.heightIn(min = ButtonSize.primaryHeight),
        shape = RoundedCornerShape(CornerRadius.large),
        contentPadding = PaddingValues(horizontal = Spacing.large, vertical = Spacing.compact),
    ) {
        Text(text, style = MaterialTheme.typography.labelLarge, textAlign = TextAlign.Center)
    }
}
