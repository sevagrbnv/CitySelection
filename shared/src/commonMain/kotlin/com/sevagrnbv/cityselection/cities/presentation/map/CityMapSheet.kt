package com.sevagrnbv.cityselection.cities.presentation.map

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import cityselection.shared.generated.resources.Res
import cityselection.shared.generated.resources.city_details_search
import com.sevagrnbv.cityselection.cities.presentation.common.CityDetailFields
import com.sevagrnbv.cityselection.core.designsystem.component.PrimaryButton
import com.sevagrnbv.cityselection.core.designsystem.theme.ComponentColors
import com.sevagrnbv.cityselection.core.designsystem.theme.ComponentSize
import com.sevagrnbv.cityselection.core.designsystem.theme.Spacing
import kotlinx.coroutines.flow.drop
import org.jetbrains.compose.resources.stringResource

/** Карточка над нижним меню. Выбранный город поступает из MVI, снимок нужен только для закрытия. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CityMapSheet(
    selectedCity: CityMapMarker?,
    snackbar: SnackbarHostState,
    onDismiss: () -> Unit,
    onSearch: (CityMapMarker) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable (coveredHeight: Dp) -> Unit,
) {
    val sheetState = rememberBottomSheetState(
        initialValue = SheetValue.Hidden,
        enabledValues = setOf(SheetValue.Hidden, SheetValue.Expanded),
    )
    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = sheetState,
        snackbarHostState = snackbar,
    )
    var sheetCity by remember { mutableStateOf(selectedCity) }
    val dismissCallback by rememberUpdatedState(onDismiss)

    NavigationBackHandler(
        state = rememberNavigationEventState(NavigationEventInfo.None),
        isBackEnabled = selectedCity != null,
        onBackCompleted = onDismiss,
    )

    LaunchedEffect(selectedCity?.id) {
        if (selectedCity != null) {
            sheetCity = selectedCity
            sheetState.expand()
        } else {
            sheetState.hide()
            // Снимок остаётся до конца анимации, чтобы содержимое не схлопывалось.
            sheetCity = null
        }
    }

    LaunchedEffect(sheetState) {
        snapshotFlow { sheetState.currentValue }.drop(1).collect { value ->
            if (value == SheetValue.Hidden) dismissCallback()
        }
    }

    var contentHeight by remember { mutableIntStateOf(0) }
    val density = LocalDensity.current
    val coveredHeight = if (selectedCity != null) {
        with(density) { contentHeight.toDp() } + Spacing.small * 2 + ComponentSize.sheetHandleHeight
    } else 0.dp

    BottomSheetScaffold(
        modifier = modifier,
        scaffoldState = scaffoldState,
        sheetPeekHeight = 0.dp,
        sheetContainerColor = MaterialTheme.colorScheme.surface,
        sheetTonalElevation = 0.dp,
        sheetShape = RoundedCornerShape(
            topStart = ComponentSize.sheetRadius,
            topEnd = ComponentSize.sheetRadius
        ),
        sheetShadowElevation = 3.dp,
        sheetDragHandle = { if (sheetCity != null) CityMapSheetHandle() },
        sheetContent = {
            sheetCity?.let { city ->
                CityMapSheetContent(
                    city, onSearch,
                    Modifier.onSizeChanged { contentHeight = it.height })
            }
        },
    ) { content(coveredHeight) }
}

@Composable
private fun CityMapSheetHandle() {
    Box(
        Modifier.fillMaxWidth().padding(vertical = Spacing.small),
        contentAlignment = Alignment.Center
    ) {
        Box(
            Modifier.size(ComponentSize.sheetHandleWidth, ComponentSize.sheetHandleHeight)
                .background(ComponentColors.Handle, RoundedCornerShape(50))
        )
    }
}

@Composable
private fun CityMapSheetContent(
    city: CityMapMarker,
    onSearch: (CityMapMarker) -> Unit,
    modifier: Modifier
) {
    Column(
        modifier.fillMaxWidth().verticalScroll(rememberScrollState())
            .padding(horizontal = Spacing.medium)
    ) {
        CityDetailFields(city.name, city.countryName, city.population)
        Spacer(Modifier.height(Spacing.medium))
        PrimaryButton(
            text = stringResource(Res.string.city_details_search),
            onClick = { onSearch(city) },
            modifier = Modifier.fillMaxWidth().padding(bottom = Spacing.medium),
        )
    }
}
