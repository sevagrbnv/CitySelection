package com.sevagrnbv.cityselection.cities.presentation.list

import androidx.lifecycle.ViewModelStore
import com.sevagrnbv.cityselection.cities.domain.model.CitiesPage
import com.sevagrnbv.cityselection.cities.domain.model.City
import com.sevagrnbv.cityselection.cities.domain.repository.CitiesRepository
import com.sevagrnbv.cityselection.cities.presentation.common.CountryNames
import com.sevagrnbv.cityselection.core.network.NetworkError
import com.sevagrnbv.cityselection.core.network.NetworkResult
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.withContext
import org.orbitmvi.orbit.test.testWithInternalState

@OptIn(ExperimentalCoroutinesApi::class)
class CitiesViewModelTest {
    @Test
    fun loadsFirstPageAndMapsCountryInPresentation() {
        val gate = CompletableDeferred<Unit>()
        val repository = FakeRepository { gate.await(); pageResult(1, listOf(city(1).copy(population = 12_655_000)), total = 2) }
        withViewModel(repository) { vm ->
            runCurrent()
            assertEquals(CitiesContent.Loading, vm.current.content)
            assertEquals(listOf(Request("", 1, 20)), repository.requests)
            gate.complete(Unit)
            runCurrent()
            val content = assertIs<CitiesContent.Data>(vm.current.content)
            assertEquals(listOf(CityListItem(1, "City 1", "Россия", 12_655_000)), content.items)
            assertTrue(content.hasNextPage)
        }
    }

    @Test
    fun debounceOnlyRequestsLatestNormalizedQuery() {
        val repository = FakeRepository { pageResult(it.page, emptyList(), total = 0) }
        withViewModel(repository) { vm ->
            runCurrent()
            vm.onQueryChanged("M")
            runCurrent()
            advanceTimeBy(200)
            vm.onQueryChanged("  Moscow  ")
            runCurrent()
            advanceTimeBy(299)
            runCurrent()
            assertEquals(listOf(""), repository.requests.map { it.query })
            advanceTimeBy(1)
            runCurrent()
            assertEquals(listOf("", "Moscow"), repository.requests.map { it.query })
            assertEquals("Moscow", vm.current.query)
            assertEquals(CitiesContent.Empty, vm.current.content)
            vm.onQueryChanged("Moscow ")
            advanceTimeBy(500)
            runCurrent()
            assertEquals(2, repository.requests.size)
        }
    }

    @Test
    fun changingQueryCancelsPreviousRequestBeforeDebounceEnds() {
        val gate = CompletableDeferred<Unit>()
        var cancelled = false
        val repository = FakeRepository {
            if (it.query == "") {
                try { gate.await() } catch (error: CancellationException) { cancelled = true; throw error }
            }
            pageResult(1, emptyList(), 0)
        }
        withViewModel(repository) { vm ->
            runCurrent()
            vm.onQueryChanged("new")
            runCurrent()
            assertTrue(cancelled)
            assertEquals(1, repository.requests.size)
            advanceTimeBy(300)
            runCurrent()
            assertEquals(listOf("", "new"), repository.requests.map { it.query })
        }
    }

    @Test
    fun lateResponseCannotReplaceNewSearchResults() {
        val gate = CompletableDeferred<Unit>()
        val repository = FakeRepository {
            if (it.query.isEmpty()) withContext(NonCancellable) { gate.await() }
            pageResult(1, listOf(city(if (it.query.isEmpty()) 1 else 2)), 1)
        }
        withViewModel(repository) { vm ->
            runCurrent()
            vm.onQueryChanged("new")
            runCurrent()
            advanceTimeBy(300)
            runCurrent()
            assertEquals(listOf(2L), vm.current.items.map { it.id })
            gate.complete(Unit)
            runCurrent()
            assertEquals(listOf(2L), vm.current.items.map { it.id })
        }
    }

    @Test
    fun nextPageIsRequestedOnceAndDuplicatesAreRemoved() {
        val gate = CompletableDeferred<Unit>()
        val repository = FakeRepository {
            if (it.page == 1) pageResult(1, listOf(city(1)), 2)
            else { gate.await(); pageResult(2, listOf(city(1), city(2)), 2) }
        }
        withViewModel(repository) { vm ->
            runCurrent()
            repeat(3) { vm.loadNextPage() }
            runCurrent()
            assertEquals(listOf(1, 2), repository.requests.map { it.page })
            assertEquals(NextPageState.Loading, vm.current.nextPage)
            assertEquals(listOf(1L), vm.current.items.map { it.id })
            gate.complete(Unit)
            runCurrent()
            assertEquals(listOf(1L, 2L), vm.current.items.map { it.id })
            assertFalse(assertIs<CitiesContent.Data>(vm.current.content).hasNextPage)
            vm.loadNextPage()
            runCurrent()
            assertEquals(2, repository.requests.size)
        }
    }

    @Test
    fun nextPageFailurePreservesListAndRetryUsesSamePage() {
        var attempts = 0
        val repository = FakeRepository {
            if (it.page == 1) pageResult(1, listOf(city(1)), 2)
            else if (++attempts == 1) failure() else pageResult(2, listOf(city(2)), 2)
        }
        withViewModel(repository) { vm ->
            runCurrent()
            vm.loadNextPage()
            runCurrent()
            assertEquals(listOf(1L), vm.current.items.map { it.id })
            assertIs<NextPageState.Error>(vm.current.nextPage)
            vm.loadNextPage()
            runCurrent()
            assertEquals(2, repository.requests.size)
            repeat(2) { vm.retry() }
            runCurrent()
            assertEquals(listOf(1, 2, 2), repository.requests.map { it.page })
            assertEquals(listOf(1L, 2L), vm.current.items.map { it.id })
        }
    }

    @Test
    fun retryFirstPageDoesNotWaitForSearchDebounce() {
        val repository = FakeRepository { if (it.query.isEmpty()) failure() else pageResult(1, listOf(city(1)), 1) }
        withViewModel(repository) { vm ->
            runCurrent()
            assertIs<CitiesContent.Error>(vm.current.content)
            vm.retry()
            runCurrent()
            assertEquals(2, repository.requests.size)
            vm.onQueryChanged("Moscow")
            runCurrent()
            advanceTimeBy(300)
            runCurrent()
            assertIs<CitiesContent.Data>(vm.current.content)
        }
    }

    @Test
    fun queryChangeResetsPaginationAndIgnoresOldPageResponse() {
        val gate = CompletableDeferred<Unit>()
        val repository = FakeRepository {
            if (it.page == 2) withContext(NonCancellable) { gate.await() }
            pageResult(it.page, listOf(city(if (it.query.isEmpty()) it.page.toLong() else 10)), if (it.query.isEmpty()) 2 else 1)
        }
        withViewModel(repository) { vm ->
            runCurrent()
            vm.loadNextPage()
            runCurrent()
            vm.onQueryChanged("new")
            runCurrent()
            advanceTimeBy(300)
            runCurrent()
            assertEquals(Request("new", 1, 20), repository.requests.last())
            assertEquals(listOf(10L), vm.current.items.map { it.id })
            gate.complete(Unit)
            runCurrent()
            assertEquals(listOf(10L), vm.current.items.map { it.id })
        }
    }

    @Test
    fun emptyPageTerminatesPaginationWithoutRemovingExistingItems() {
        val repository = FakeRepository { pageResult(it.page, if (it.page == 1) listOf(city(1)) else emptyList(), 100) }
        withViewModel(repository) { vm ->
            runCurrent()
            vm.loadNextPage()
            runCurrent()
            assertEquals(listOf(1L), vm.current.items.map { it.id })
            assertFalse(assertIs<CitiesContent.Data>(vm.current.content).hasNextPage)
            vm.loadNextPage()
            runCurrent()
            assertEquals(2, repository.requests.size)
        }
    }

    private fun withViewModel(repository: FakeRepository, block: suspend TestScope.(CitiesViewModel) -> Unit) = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val vm = CitiesViewModel(repository, CountryNames { "Россия" })
        val store = ViewModelStore().apply { put("cities", vm) }
        try {
            vm.testWithInternalState(this) {
                runOnCreate().join()
                try { block(vm) } finally { store.clear(); cancelAndIgnoreRemainingItems() }
            }
        } finally {
            store.clear()
            Dispatchers.resetMain()
        }
    }

    private val CitiesViewModel.current get() = container.stateFlow.value
    private val CitiesState.items get() = assertIs<CitiesContent.Data>(content).items
    private fun city(id: Long) = City(id, "City $id", "RU", null, null, null)
    private fun pageResult(page: Int, items: List<City>, total: Long) = NetworkResult.Success(CitiesPage(items, page, 1, total))
    private fun failure() = NetworkResult.Failure(NetworkError.Network, IllegalStateException("test failure"))
    private data class Request(val query: String, val page: Int, val limit: Int)
    private class FakeRepository(val handler: suspend (Request) -> NetworkResult<CitiesPage>) : CitiesRepository {
        val requests = mutableListOf<Request>()
        override suspend fun getCities(query: String, page: Int, limit: Int): NetworkResult<CitiesPage> {
            val request = Request(query, page, limit)
            requests += request
            return handler(request)
        }
    }
}
