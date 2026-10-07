package com.sevagrnbv.cityselection.cities.domain

import com.sevagrnbv.cityselection.core.network.NetworkResult

interface CitiesRepository {
    /** Страницы нумеруются с 1. Некорректные аргументы вызывают IllegalArgumentException. */
    suspend fun getCities(query: String = "", page: Int = 1, limit: Int = 30): NetworkResult<CitiesPage>
}
