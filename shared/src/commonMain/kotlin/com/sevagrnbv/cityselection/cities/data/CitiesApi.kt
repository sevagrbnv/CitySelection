package com.sevagrnbv.cityselection.cities.data

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter

internal class CitiesApi(private val client: HttpClient, private val baseUrl: String) {
    private val citiesUrl = "${baseUrl.trimEnd('/')}/api/cities"

    suspend fun getCities(query: String, page: Int, limit: Int): CitiesPageDto =
        client.get(citiesUrl) {
            parameter("query", query)
            parameter("page", page)
            parameter("limit", limit)
        }.body()
}
