package com.sevagrnbv.cityselection.cities.data.repository

import com.sevagrnbv.cityselection.cities.data.mapper.toDomain
import com.sevagrnbv.cityselection.cities.data.remote.CitiesApi
import com.sevagrnbv.cityselection.cities.domain.model.CitiesPage
import com.sevagrnbv.cityselection.cities.domain.repository.CitiesRepository
import com.sevagrnbv.cityselection.core.network.NetworkResult
import com.sevagrnbv.cityselection.core.network.networkRequest

internal class RemoteCitiesRepository(private val api: CitiesApi) : CitiesRepository {
    override suspend fun getCities(query: String, page: Int, limit: Int): NetworkResult<CitiesPage> =
        networkRequest {
            api.getCities(
                query = query.trim(),
                page = page,
                limit = limit
            ).toDomain()
        }
}
