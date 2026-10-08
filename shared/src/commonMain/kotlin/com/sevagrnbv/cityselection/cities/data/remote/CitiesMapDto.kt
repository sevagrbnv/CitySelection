package com.sevagrnbv.cityselection.cities.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Ответ GET /api/cities/map. Пагинации нет; count описывает полученную выборку.
 * Формат города общий с запросом списка, поэтому используется CityDto.
 */
@Serializable
internal data class CitiesMapDto(
    @SerialName("count") val count: Int,
    @SerialName("items") val items: List<CityDto>,
)
