package com.sevagrnbv.cityselection.cities.data.remote

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

    suspend fun getMapCities(latitude: Double, longitude: Double, radiusMeters: Int): CitiesMapDto =
        client.get("$citiesUrl/map") {
            parameter("centerLat", latitude)
            parameter("centerLng", longitude)
            parameter("radius", radiusMeters)
        }.body()
}
