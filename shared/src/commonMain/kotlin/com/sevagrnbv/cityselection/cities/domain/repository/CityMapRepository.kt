package com.sevagrnbv.cityselection.cities.domain.repository

import com.sevagrnbv.cityselection.cities.domain.model.City
import com.sevagrnbv.cityselection.core.network.NetworkResult

interface CityMapRepository {
    /** Радиус в метрах вокруг центра видимой области; ответ без пагинации. */
    suspend fun getCitiesAround(latitude: Double, longitude: Double, radiusMeters: Int): NetworkResult<List<City>>
}
