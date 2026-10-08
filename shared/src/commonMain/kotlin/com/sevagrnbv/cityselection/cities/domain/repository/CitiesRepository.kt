package com.sevagrnbv.cityselection.cities.domain.repository

import com.sevagrnbv.cityselection.cities.domain.model.CitiesPage
import com.sevagrnbv.cityselection.core.network.NetworkResult

interface CitiesRepository {
    /** Страницы нумеруются с 1; размер страницы по умолчанию — 20. */
    suspend fun getCities(query: String = "", page: Int = 1, limit: Int = DEFAULT_PAGE_SIZE): NetworkResult<CitiesPage>

    companion object {
        const val DEFAULT_PAGE_SIZE = 20
    }
}
