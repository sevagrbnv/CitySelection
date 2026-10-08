package com.sevagrnbv.cityselection.cities.presentation.map

import androidx.lifecycle.ViewModelStore
import com.sevagrnbv.cityselection.cities.domain.model.City
import com.sevagrnbv.cityselection.cities.domain.repository.CityMapRepository
import com.sevagrnbv.cityselection.cities.presentation.common.CountryNames
import com.sevagrnbv.cityselection.core.network.NetworkError
import com.sevagrnbv.cityselection.core.network.NetworkResult
import kotlin.test.*
import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import org.orbitmvi.orbit.test.testWithInternalState

@OptIn(ExperimentalCoroutinesApi::class)
class CityMapViewModelTest {
    @Test
    fun successiveCameraStopsAreDebouncedAndDuplicateViewportIsIgnored() {
        val repository = FakeRepository { NetworkResult.Success(listOf(city(1))) }
        withViewModel(repository) { vm ->
            vm.onViewportChanged(viewport(1.0)); runCurrent()
            advanceTimeBy(300)
            vm.onViewportChanged(viewport(2.0)); runCurrent()
            advanceTimeBy(349); runCurrent()
            assertTrue(repository.requests.isEmpty())
            advanceTimeBy(1); runCurrent()
            assertEquals(listOf(2.0), repository.requests.map { it.camera.latitude })
            assertEquals("Россия", vm.current.cities.single().countryName)
            vm.onViewportChanged(viewport(2.0)); advanceTimeBy(1000); runCurrent()
            assertEquals(1, repository.requests.size)
        }
    }

    @Test
    fun newViewportCancelsOldRequestAndLateResultIsIgnored() {
        val oldGate = CompletableDeferred<Unit>()
        var cancelled = false
        val repository = FakeRepository {
            if (it.camera.latitude == 1.0) {
                try { oldGate.await() }
                catch (_: CancellationException) { cancelled = true; withContext(NonCancellable) { oldGate.await() } }
            }
            NetworkResult.Success(listOf(city(it.camera.latitude.toLong())))
        }
        withViewModel(repository) { vm ->
            vm.onViewportChanged(viewport(1.0)); runCurrent(); advanceTimeBy(350); runCurrent()
            vm.onViewportChanged(viewport(2.0)); runCurrent()
            assertTrue(cancelled)
            advanceTimeBy(350); runCurrent()
            assertEquals(listOf(2L), vm.current.cities.map { it.id })
            oldGate.complete(Unit); runCurrent()
            assertEquals(listOf(2L), vm.current.cities.map { it.id })
        }
    }

    @Test
    fun failureKeepsMarkersAndRetryUsesCurrentViewportWithoutDebounce() {
        var attempts = 0
        val repository = FakeRepository {
            if (++attempts == 2) NetworkResult.Failure(NetworkError.Network, IllegalStateException("test"))
            else NetworkResult.Success(listOf(city(attempts.toLong())))
        }
        withViewModel(repository) { vm ->
            vm.onViewportChanged(viewport(1.0)); runCurrent(); advanceTimeBy(350); runCurrent()
            vm.selectCity(1); runCurrent()
            vm.onViewportChanged(viewport(2.0)); runCurrent(); advanceTimeBy(350); runCurrent()
            assertEquals(NetworkError.Network, vm.current.error)
            assertEquals(1L, vm.current.cities.single().id)
            assertEquals(1L, vm.current.selected?.id)
            vm.retry(); runCurrent()
            assertEquals(listOf(1.0, 2.0, 2.0), repository.requests.map { it.camera.latitude })
            assertNull(vm.current.error)
            assertEquals(3L, vm.current.cities.single().id)
            assertEquals(1L, vm.current.selected?.id)
            vm.dismissCity(); runCurrent(); assertNull(vm.current.selected)
        }
    }

    @Test
    fun cannotPlotMissingCoordinatesAndEmptyResultIsSuccessful() {
        val repository = FakeRepository { NetworkResult.Success(listOf(city(1).copy(latitude = null), city(2).copy(longitude = 200.0))) }
        withViewModel(repository) { vm ->
            vm.onViewportChanged(viewport(1.0)); runCurrent(); advanceTimeBy(350); runCurrent()
            assertTrue(vm.current.loaded)
            assertFalse(vm.current.loading)
            assertTrue(vm.current.cities.isEmpty())
            assertNull(vm.current.error)
        }
    }

    private fun withViewModel(repository: FakeRepository, block: suspend TestScope.(CityMapViewModel) -> Unit) = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val vm = CityMapViewModel(repository, CountryNames { "Россия" })
        val store = ViewModelStore().apply { put("map", vm) }
        try {
            vm.testWithInternalState(this) {
                try { block(vm) } finally { store.clear(); cancelAndIgnoreRemainingItems() }
            }
        } finally { store.clear(); Dispatchers.resetMain() }
    }
    private val CityMapViewModel.current get() = container.stateFlow.value
    private fun city(id: Long) = City(id, "City $id", "RU", 55.0, 37.0, null)
    private fun viewport(lat: Double) = MapViewport(MapCamera(lat, 37.0, 10.0), 12000)
    private class FakeRepository(val handler: suspend (MapViewport) -> NetworkResult<List<City>>) : CityMapRepository {
        val requests = mutableListOf<MapViewport>()
        override suspend fun getCitiesAround(latitude: Double, longitude: Double, radiusMeters: Int): NetworkResult<List<City>> {
            val viewport = MapViewport(MapCamera(latitude, longitude), radiusMeters)
            requests += viewport
            return handler(viewport)
        }
    }
}
