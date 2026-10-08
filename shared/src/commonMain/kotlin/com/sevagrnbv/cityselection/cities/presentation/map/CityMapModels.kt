package com.sevagrnbv.cityselection.cities.presentation.map

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.sevagrnbv.cityselection.core.network.NetworkError
import kotlin.math.*

/** Данные метки для нативных SDK; DTO и платформенные типы в контракт не попадают. */
data class CityMapMarker(
    val id: Long,
    val name: String,
    val countryName: String,
    val latitude: Double,
    val longitude: Double,
    val population: Long?,
)

internal data class MapCamera(
    val latitude: Double = 55.75204,
    val longitude: Double = 37.61781,
    val zoom: Double = 10.0
)

internal data class MapViewport(val camera: MapCamera, val radiusMeters: Int)
internal enum class MapLoadStatus { Loading, Ready, Error }
internal data class CityMapState(
    val cities: List<CityMapMarker> = emptyList(),
    val selected: CityMapMarker? = null,
    val loading: Boolean = false,
    val loaded: Boolean = false,
    val error: NetworkError? = null,
)

private const val EARTH_MEAN_RADIUS_METERS = 6_371_000.0
private const val DEGREES_TO_RADIANS = PI / 180.0

/**
 * Расстояние в метрах между географическими координатами в градусах по формуле гаверсинуса.
 *
 * Используется для определения радиуса запроса городов: API принимает радиус в метрах,
 * поэтому [viewportRadius] выбирает максимальное расстояние от центра карты до её углов.
 * Средний радиус Земли [EARTH_MEAN_RADIUS_METERS] переводит центральный угол в радианах
 * в расстояние по поверхности сферы. Расчёт учитывает пересечение антимеридиана.
 *
 * Общий алгоритм заменяет отдельные расчёты через географические API Android и iOS.
 */
internal fun distanceMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val dLat = (lat2 - lat1) * DEGREES_TO_RADIANS
    val dLon = (lon2 - lon1) * DEGREES_TO_RADIANS
    val a = sin(dLat / 2).pow(2) +
        cos(lat1 * DEGREES_TO_RADIANS) * cos(lat2 * DEGREES_TO_RADIANS) * sin(dLon / 2).pow(2)
    // Ограничиваем погрешность округления, чтобы asin не получил значение вне [-1, 1].
    val centralAngle = 2 * asin(sqrt(a.coerceIn(0.0, 1.0)))
    return EARTH_MEAN_RADIUS_METERS * centralAngle
}

/** Окружность покрывает углы видимой области; округление вверх не обрезает край карты. */
internal fun viewportRadius(camera: MapCamera, corners: List<Pair<Double, Double>>): Int =
    ceil(corners.maxOf { (lat, lon) ->
        distanceMeters(camera.latitude, camera.longitude, lat, lon)
    }).toInt().coerceAtLeast(1)

internal fun interface MapControls {
    fun zoomBy(delta: Double)
}

@Composable
internal expect fun NativeCityMap(
    initialCamera: MapCamera,
    cities: List<CityMapMarker>,
    selectedId: Long?,
    onViewportChanged: (MapViewport) -> Unit,
    onCitySelected: (Long) -> Unit,
    onMapTapped: () -> Unit,
    onLoadStatus: (MapLoadStatus) -> Unit,
    onControlsChanged: (MapControls?) -> Unit,
    modifier: Modifier = Modifier,
)
