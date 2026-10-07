package com.sevagrnbv.cityselection.cities.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sevagrnbv.cityselection.cities.domain.CitiesPage
import com.sevagrnbv.cityselection.cities.domain.CitiesRepository
import com.sevagrnbv.cityselection.core.network.NetworkResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.orbitmvi.orbit.OrbitContainerHost
import org.orbitmvi.orbit.orbitContainer
import org.orbitmvi.orbit.syntax.Syntax

internal class CitiesViewModel(
    private val repository: CitiesRepository,
    private val countryNames: CountryNames,
) : ViewModel(), OrbitContainerHost<CitiesState, CitiesState, Nothing> {
    private val requestMutex = Mutex()
    private var generation = 0L
    private var activeQuery = ""
    private var nextPage: Int? = null
    private var pageSize = PAGE_SIZE
    private var requestJob: Job? = null

    override val container = viewModelScope.orbitContainer<CitiesState, Nothing>(CitiesState()) {
        requestMutex.withLock {
            if (generation == 0L) startFirstPage(debounce = false)
        }
    }

    fun onQueryChanged(query: String) = intent {
        requestMutex.withLock {
            val normalized = query.trim()
            if (normalized == activeQuery) return@withLock
            activeQuery = normalized
            startFirstPage(debounce = true)
        }
    }

    fun loadNextPage() = intent {
        requestMutex.withLock {
            if (state.content is CitiesContent.Data && state.nextPage == NextPageState.Idle) {
                startNextPage()
            }
        }
    }

    fun retry() = intent {
        requestMutex.withLock {
            when {
                state.content is CitiesContent.Error -> startFirstPage(debounce = false)
                state.nextPage is NextPageState.Error -> startNextPage()
            }
        }
    }

    /** Изменение запроса сразу отменяет загрузку, включая запрос следующей страницы. */
    private suspend fun Syntax<CitiesState, Nothing>.startFirstPage(debounce: Boolean) {
        generation++
        requestJob?.cancel()
        nextPage = null
        pageSize = PAGE_SIZE
        reduce { CitiesState(query = activeQuery) }
        launchRequest(page = 1, append = false, debounce = debounce)
    }

    private suspend fun Syntax<CitiesState, Nothing>.startNextPage() {
        val page = nextPage ?: return
        reduce { state.copy(nextPage = NextPageState.Loading) }
        launchRequest(page = page, append = true, debounce = false)
    }

    private fun launchRequest(page: Int, append: Boolean, debounce: Boolean) {
        val requestGeneration = generation
        val query = activeQuery
        val limit = pageSize
        requestJob = viewModelScope.launch {
            if (debounce) delay(SEARCH_DEBOUNCE_MILLIS)
            val result = repository.getCities(query = query, page = page, limit = limit)
            applyResult(requestGeneration, append, result).join()
        }
    }

    private fun applyResult(
        requestGeneration: Long,
        append: Boolean,
        result: NetworkResult<CitiesPage>,
    ) = intent {
        requestMutex.withLock {
            // Проверка нужна даже при отмене: источник данных может завершить старый запрос.
            if (requestGeneration != generation) return@withLock
            when (result) {
                is NetworkResult.Success -> {
                    val page = result.value
                    val existing = if (append) (state.content as? CitiesContent.Data)?.items.orEmpty() else emptyList()
                    val received = page.items.map { city ->
                        CityListItem(city.id, city.name, countryNames.displayName(city.countryCode))
                    }
                    val items = (existing + received).distinctBy { it.id }
                    nextPage = if (page.hasNextPage) page.page + 1 else null
                    pageSize = page.limit
                    reduce {
                        state.copy(
                            content = if (items.isEmpty()) CitiesContent.Empty else CitiesContent.Data(items, page.hasNextPage),
                            nextPage = NextPageState.Idle,
                        )
                    }
                }
                is NetworkResult.Failure -> reduce {
                    if (append) state.copy(nextPage = NextPageState.Error(result.error))
                    else state.copy(content = CitiesContent.Error(result.error))
                }
            }
        }
    }

    private companion object {
        const val PAGE_SIZE = 30
        const val SEARCH_DEBOUNCE_MILLIS = 300L
    }
}
