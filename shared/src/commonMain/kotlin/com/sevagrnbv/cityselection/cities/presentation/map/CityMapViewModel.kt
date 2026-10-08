package com.sevagrnbv.cityselection.cities.presentation.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sevagrnbv.cityselection.cities.domain.model.City
import com.sevagrnbv.cityselection.cities.domain.repository.CityMapRepository
import com.sevagrnbv.cityselection.cities.presentation.common.CountryNames
import com.sevagrnbv.cityselection.core.network.NetworkResult
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.orbitmvi.orbit.OrbitContainerHost
import org.orbitmvi.orbit.orbitContainer
import org.orbitmvi.orbit.syntax.Syntax

internal class CityMapViewModel(
    private val repository: CityMapRepository,
    private val countryNames: CountryNames,
) : ViewModel(), OrbitContainerHost<CityMapState, CityMapState, Nothing> {
    override val container = viewModelScope.orbitContainer<CityMapState, Nothing>(CityMapState())
    private val mutex = Mutex()
    private var viewport: MapViewport? = null
    private var generation = 0L
    private var request: Job? = null

    /** Нативная карта сообщает область после остановки камеры; короткий debounce объединяет жесты. */
    fun onViewportChanged(value: MapViewport) = intent {
        mutex.withLock {
            if (viewport == value) return@withLock
            viewport = value
            load(value, debounce = true)
        }
    }

    fun retry() = intent {
        mutex.withLock {
            if (state.error != null && !state.loading) viewport?.let { load(it, debounce = false) }
        }
    }

    fun selectCity(id: Long) = intent {
        reduce { state.copy(selected = state.cities.firstOrNull { it.id == id }) }
    }

    fun dismissCity() = intent { reduce { state.copy(selected = null) } }

    private suspend fun Syntax<CityMapState, Nothing>.load(value: MapViewport, debounce: Boolean) {
        val token = ++generation
        request?.cancel()
        // Метки остаются доступными при обновлении и сетевой ошибке.
        reduce { state.copy(loading = true, error = null) }
        request = viewModelScope.launch {
            if (debounce) delay(VIEWPORT_DEBOUNCE)
            val result = repository.getCitiesAround(value.camera.latitude, value.camera.longitude, value.radiusMeters)
            applyResult(token, result).join()
        }
    }

    private fun applyResult(token: Long, result: NetworkResult<List<City>>) = intent {
        mutex.withLock {
            if (token != generation) return@withLock
            when (result) {
                is NetworkResult.Success -> {
                    val markers = result.value.mapNotNull { city ->
                        val lat = city.latitude ?: return@mapNotNull null
                        val lon = city.longitude ?: return@mapNotNull null
                        // Неизвестную или непригодную для SDK координату нельзя нанести на карту.
                        if (!lat.isFinite() || !lon.isFinite() || lat !in -90.0..90.0 || lon !in -180.0..180.0) return@mapNotNull null
                        CityMapMarker(city.id, city.name, countryNames.displayName(city.countryCode), lat, lon, city.population)
                    }.distinctBy { it.id }
                    // Открытая карточка сохраняется даже при перемещении за пределы новой выборки.
                    reduce { state.copy(cities = markers, loading = false, loaded = true, error = null) }
                }
                is NetworkResult.Failure -> reduce { state.copy(loading = false, error = result.error) }
            }
        }
    }

    private companion object {
        /** Объединяет частые изменения области карты перед запросом городов. */
        val VIEWPORT_DEBOUNCE = 350.milliseconds
    }
}
