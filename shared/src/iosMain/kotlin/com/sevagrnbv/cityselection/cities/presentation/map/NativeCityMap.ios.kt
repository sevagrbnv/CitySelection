@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package com.sevagrnbv.cityselection.cities.presentation.map

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.viewinterop.UIKitInteropInteractionMode
import androidx.compose.ui.viewinterop.UIKitInteropProperties
import androidx.compose.ui.viewinterop.UIKitView
import cityselection.shared.generated.resources.Res
import org.jetbrains.compose.resources.ExperimentalResourceApi
import platform.UIKit.UIView

/** Реализуется iOS-приложением: MapLibre подключён через SPM, без CocoaPods в общем модуле. */
interface IosCityMapFactory {
    fun create(styleUrl: String, latitude: Double, longitude: Double, zoom: Double, events: IosMapEvents): IosCityMap
}

interface IosCityMap {
    val view: UIView
    fun update(cities: List<CityMapMarker>, selectedId: Long?)
    fun setPinFont(bytes: ByteArray)
    fun zoomBy(delta: Double)
    fun dispose()
}

/** Все вызовы выполняются в главном потоке. Границы передаются при остановке камеры. */
interface IosMapEvents {
    fun viewportChanged(latitude: Double, longitude: Double, zoom: Double,
                        northLatitude: Double, eastLongitude: Double, southLatitude: Double, westLongitude: Double)
    fun citySelected(id: Long)
    fun mapTapped()
    fun didFinishLoading()
    fun didFailLoading()
}

internal val LocalIosCityMapFactory = staticCompositionLocalOf<IosCityMapFactory> {
    error("IosCityMapFactory must be supplied by the iOS application")
}

@OptIn(ExperimentalResourceApi::class)
@Composable
internal actual fun NativeCityMap(
    initialCamera: MapCamera,
    cities: List<CityMapMarker>,
    selectedId: Long?,
    onViewportChanged: (MapViewport) -> Unit,
    onCitySelected: (Long) -> Unit,
    onMapTapped: () -> Unit,
    onLoadStatus: (MapLoadStatus) -> Unit,
    onControlsChanged: (MapControls?) -> Unit,
    modifier: Modifier,
) {
    val factory = LocalIosCityMapFactory.current
    val viewportCallback by rememberUpdatedState(onViewportChanged)
    val selectionCallback by rememberUpdatedState(onCitySelected)
    val mapTapCallback by rememberUpdatedState(onMapTapped)
    val controlsCallback by rememberUpdatedState(onControlsChanged)
    val statusCallback by rememberUpdatedState(onLoadStatus)
    val native = remember(factory) {
        factory.create(CityMapConfig.styleUrl, initialCamera.latitude, initialCamera.longitude, initialCamera.zoom,
            object : IosMapEvents {
                override fun viewportChanged(latitude: Double, longitude: Double, zoom: Double,
                                             northLatitude: Double, eastLongitude: Double, southLatitude: Double, westLongitude: Double) {
                    val camera = MapCamera(latitude, longitude, zoom)
                    val corners = listOf(northLatitude to eastLongitude, northLatitude to westLongitude,
                        southLatitude to eastLongitude, southLatitude to westLongitude)
                    viewportCallback(MapViewport(camera, viewportRadius(camera, corners)))
                }
                override fun citySelected(id: Long) = selectionCallback(id)
                override fun mapTapped() = mapTapCallback()
                override fun didFinishLoading() = statusCallback(MapLoadStatus.Ready)
                override fun didFailLoading() = statusCallback(MapLoadStatus.Error)
            })
    }
    LaunchedEffect(native) { native.setPinFont(Res.readBytes("font/roboto_semibold.ttf")) }
    DisposableEffect(native) {
        controlsCallback(MapControls { native.zoomBy(it) })
        onDispose { controlsCallback(null); native.dispose() }
    }
    UIKitView(
        factory = { native.view },
        modifier = modifier.clipToBounds(),
        update = { native.update(cities, selectedId) },
        properties = UIKitInteropProperties(interactionMode = UIKitInteropInteractionMode.NonCooperative,
            isNativeAccessibilityEnabled = true),
    )
}
