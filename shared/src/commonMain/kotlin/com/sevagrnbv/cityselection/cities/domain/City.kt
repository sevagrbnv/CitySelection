package com.sevagrnbv.cityselection.cities.domain

/** Названия и коды стран сохраняются как на сервере; форматирование относится к презентации. */
data class City(
    val id: Long,
    val name: String,
    val countryCode: String,
    val latitude: Double?,
    val longitude: Double?,
    val population: Long?,
)

data class CitiesPage(
    val items: List<City>,
    val page: Int,
    val limit: Int,
    val total: Long,
) {
    val hasNextPage: Boolean
        get() = items.isNotEmpty() && page.toLong() * limit < total
}
