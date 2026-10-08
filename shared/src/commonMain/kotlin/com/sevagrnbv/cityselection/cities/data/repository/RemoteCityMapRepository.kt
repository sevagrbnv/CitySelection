package com.sevagrnbv.cityselection.cities.data.repository

import com.sevagrnbv.cityselection.cities.data.mapper.toDomain
import com.sevagrnbv.cityselection.cities.data.remote.CitiesApi
import com.sevagrnbv.cityselection.cities.domain.model.City
import com.sevagrnbv.cityselection.cities.domain.repository.CityMapRepository
import com.sevagrnbv.cityselection.core.network.NetworkResult
import com.sevagrnbv.cityselection.core.network.networkRequest

internal class RemoteCityMapRepository(private val api: CitiesApi) : CityMapRepository {
    override suspend fun getCitiesAround(
        latitude: Double,
        longitude: Double,
        radiusMeters: Int,
    ): NetworkResult<List<City>> = networkRequest {
        api.getMapCities(latitude, longitude, radiusMeters).items.map { it.toDomain() }
    }
}
