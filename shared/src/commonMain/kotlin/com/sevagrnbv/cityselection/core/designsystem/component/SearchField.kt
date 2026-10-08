package com.sevagrnbv.cityselection.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldDecorator
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import cityselection.shared.generated.resources.Res
import cityselection.shared.generated.resources.ic_search
import com.sevagrnbv.cityselection.core.designsystem.theme.BackgroundColors
import com.sevagrnbv.cityselection.core.designsystem.theme.CornerRadius
import com.sevagrnbv.cityselection.core.designsystem.theme.EffectColors
import com.sevagrnbv.cityselection.core.designsystem.theme.FocusShadow
import com.sevagrnbv.cityselection.core.designsystem.theme.IconColors
import com.sevagrnbv.cityselection.core.designsystem.theme.IconSize
import com.sevagrnbv.cityselection.core.designsystem.theme.InputSize
import com.sevagrnbv.cityselection.core.designsystem.theme.Spacing
import com.sevagrnbv.cityselection.core.designsystem.theme.TextColors
import org.jetbrains.compose.resources.painterResource

/** Адаптивное поле поиска: минимальная высота, без фиксированной ширины и высоты текста. */
@Composable
internal fun SearchField(
    state: TextFieldState,
    placeholder: String,
    onSearch: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val focused by interactionSource.collectIsFocusedAsState()
    val shape = RoundedCornerShape(CornerRadius.large)

    BasicTextField(
        state = state,
        modifier = modifier.fillMaxWidth(),
        interactionSource = interactionSource,
        textStyle = MaterialTheme.typography.bodyLarge.copy(color = TextColors.Primary),
        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
        lineLimits = TextFieldLineLimits.SingleLine,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        onKeyboardAction = { onSearch() },
        decorator = TextFieldDecorator { innerTextField ->
            Row(
                modifier = Modifier.fillMaxWidth().heightIn(min = InputSize.minimumHeight)
                    .then(if (focused) Modifier.dropShadow(shape, Shadow(
                        radius = FocusShadow.radius,
                        color = EffectColors.FocusShadow,
                        offset = DpOffset(0.dp, FocusShadow.offsetY),
                    )) else Modifier)
                    .background(if (focused) BackgroundColors.Primary else BackgroundColors.Tertiary, shape)
                    .border(InputSize.borderWidth, if (focused) MaterialTheme.colorScheme.primary else Color.Transparent, shape)
                    .padding(Spacing.medium),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.compact),
            ) {
                Box(Modifier.weight(1f)) {
                    if (state.text.isEmpty()) {
                        Text(
                            text = placeholder,
                            style = MaterialTheme.typography.bodyLarge,
                            color = TextColors.Tertiary, maxLines = 1
                        )
                    }
                    innerTextField()
                }
                Icon(painterResource(Res.drawable.ic_search), contentDescription = null,
                    tint = IconColors.Primary, modifier = Modifier.size(IconSize.standard))
            }
        },
    )
}
