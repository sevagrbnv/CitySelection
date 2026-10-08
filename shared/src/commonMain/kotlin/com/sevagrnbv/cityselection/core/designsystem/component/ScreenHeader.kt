package com.sevagrnbv.cityselection.core.designsystem.component

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.sevagrnbv.cityselection.core.designsystem.theme.ComponentSize
import com.sevagrnbv.cityselection.core.designsystem.theme.IconSize
import com.sevagrnbv.cityselection.core.designsystem.theme.Spacing
import com.sevagrnbv.cityselection.core.designsystem.theme.TextColors

/** Общий заголовок с системной safe area и растущей высотой текста. */
@Composable
internal fun ScreenHeader(title: String, navigationIcon: @Composable () -> Unit = {}) {
    Box(
        Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal))
            .padding(top = Spacing.compact, bottom = Spacing.medium)
    ) {
        Text(
            title, style = MaterialTheme.typography.titleLarge, color = TextColors.Primary,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = ComponentSize.headerTitleHorizontalPadding)
                .heightIn(min = IconSize.standard)
        )
        Box(
            Modifier
                .align(Alignment.CenterStart)
                .padding(start = Spacing.medium)
                .size(IconSize.standard)
        ) {
            navigationIcon()
        }
    }
}
