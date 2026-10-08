package com.sevagrnbv.cityselection.cities.presentation.map

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable

/** Состояние отображения нативной карты; выбор города и REST-состояние остаются в MVI. */
@Stable
internal class CityMapUiState(private val cameraState: MutableState<MapCamera>) {
    val camera: MapCamera get() = cameraState.value
    var reloadKey by mutableIntStateOf(0)
        private set
    var mapStatus by mutableStateOf(MapLoadStatus.Loading)
        private set
    private var controls: MapControls? = null
    val snackbar = SnackbarHostState()

    fun updateCamera(camera: MapCamera) {
        cameraState.value = camera
    }

    fun updateLoadStatus(status: MapLoadStatus) {
        mapStatus = status
    }

    fun updateControls(controls: MapControls?) {
        this.controls = controls
    }

    fun zoomBy(delta: Double) {
        controls?.zoomBy(delta)
    }

    fun reloadMap() {
        mapStatus = MapLoadStatus.Loading
        controls = null
        reloadKey++
    }
}

/** Камера переживает восстановление Compose-состояния; SDK-объекты не сохраняются. */
@Composable
internal fun rememberCityMapUiState(): CityMapUiState {
    val camera = rememberSaveable(stateSaver = MapCameraSaver) { mutableStateOf(MapCamera()) }
    return remember(camera) { CityMapUiState(camera) }
}

private val MapCameraSaver = listSaver(
    save = { listOf(it.latitude, it.longitude, it.zoom) },
    restore = {
        val (latitude, longitude, zoom) = it
        MapCamera(latitude, longitude, zoom)
    },
)
