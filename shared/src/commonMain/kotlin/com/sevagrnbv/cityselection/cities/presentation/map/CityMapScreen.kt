package com.sevagrnbv.cityselection.cities.presentation.map

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import cityselection.shared.generated.resources.*
import com.sevagrnbv.cityselection.cities.presentation.common.citySearchUrl
import com.sevagrnbv.cityselection.core.designsystem.theme.ComponentColors
import com.sevagrnbv.cityselection.core.designsystem.theme.ComponentSize
import com.sevagrnbv.cityselection.core.designsystem.theme.IconColors
import com.sevagrnbv.cityselection.core.designsystem.theme.Spacing
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

/** Отображает MVI-состояние; нативные объекты и состояние анимации принадлежат UI. */
@Composable
internal fun CityMapScreen(
    state: CityMapState,
    onViewportChanged: (MapViewport) -> Unit,
    onCitySelected: (Long) -> Unit,
    onDismissCity: () -> Unit,
    onRetryCities: () -> Unit,
    modifier: Modifier = Modifier,
    uiState: CityMapUiState = rememberCityMapUiState(),
) {
    val scope = rememberCoroutineScope()
    val uriHandler = LocalUriHandler.current
    val zoomInLabel = stringResource(Res.string.map_zoom_in)
    val zoomOutLabel = stringResource(Res.string.map_zoom_out)
    val browserError = stringResource(Res.string.city_details_browser_error)

    CityMapSheet(
        selectedCity = state.selected,
        snackbar = uiState.snackbar,
        onDismiss = onDismissCity,
        onSearch = { city ->
            try {
                uriHandler.openUri(citySearchUrl(city.name))
            } catch (_: IllegalArgumentException) {
                scope.launch { uiState.snackbar.showSnackbar(browserError) }
            }
        },
        modifier = modifier.fillMaxSize(),
    ) { coveredHeight ->
        BoxWithConstraints(Modifier.fillMaxSize()) {
            key(uiState.reloadKey) {
                NativeCityMap(
                    initialCamera = uiState.camera,
                    cities = state.cities,
                    selectedId = state.selected?.id,
                    onViewportChanged = { uiState.updateCamera(it.camera); onViewportChanged(it) },
                    onCitySelected = onCitySelected,
                    onMapTapped = onDismissCity,
                    onLoadStatus = uiState::updateLoadStatus,
                    onControlsChanged = uiState::updateControls,
                    modifier = Modifier.fillMaxSize(),
                )
            }
            Column(
                Modifier
                    .align(Alignment.TopCenter)
                    .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal))
                    .padding(Spacing.medium),
                verticalArrangement = Arrangement.spacedBy(Spacing.small),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                MapLoadStatusContent(status = uiState.mapStatus, onRetry = uiState::reloadMap)
                CitiesLoadStatusContent(state = state, onRetry = onRetryCities)
            }

            val hiddenBySheet by animateDpAsState(
                coveredHeight,
                label = "zoom-visible-map"
            )

            Box(
                Modifier.fillMaxWidth().height((maxHeight - hiddenBySheet).coerceAtLeast(0.dp)),
                contentAlignment = Alignment.CenterEnd
            ) {
                Surface(
                    Modifier.windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal))
                        .padding(end = Spacing.compact).width(ComponentSize.zoomWidth),
                    shape = RoundedCornerShape(ComponentSize.zoomRadius),
                    shadowElevation = Spacing.extraSmall
                ) {
                    Column {
                        MapZoomButton(
                            action = ZoomAction.IN,
                            label = zoomInLabel,
                            enabled = uiState.mapStatus == MapLoadStatus.Ready,
                            onZoom = uiState::zoomBy,
                        )
                        HorizontalDivider(thickness = Spacing.tiny, color = ComponentColors.Divider)
                        MapZoomButton(
                            action = ZoomAction.OUT,
                            label = zoomOutLabel,
                            enabled = uiState.mapStatus == MapLoadStatus.Ready,
                            onZoom = uiState::zoomBy,
                        )
                    }
                }
            }
        }
    }
}

private enum class ZoomAction(val delta: Double) {
    IN(1.0),
    OUT(-1.0),
}

@Composable
private fun MapZoomButton(
    action: ZoomAction,
    label: String,
    enabled: Boolean,
    onZoom: (Double) -> Unit,
) {
    IconButton(
        onClick = { onZoom(action.delta) },
        enabled = enabled,
        modifier = Modifier.fillMaxWidth()
            .height((ComponentSize.zoomHeight - Spacing.tiny) / 2)
            .semantics { contentDescription = label },
    ) {
        Canvas(Modifier.size(24.dp)) {
            val color = if (enabled) IconColors.Primary else Color.Gray
            drawLine(color, Offset(size.width * .25f, size.height / 2),
                Offset(size.width * .75f, size.height / 2), strokeWidth = 2.dp.toPx())
            if (action == ZoomAction.IN) {
                drawLine(color, Offset(size.width / 2, size.height * .25f),
                    Offset(size.width / 2, size.height * .75f), strokeWidth = 2.dp.toPx())
            }
        }
    }
}
