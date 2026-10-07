package com.sevagrnbv.cityselection.cities.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Оболочка ответа `GET /api/cities?query=...&page=...&limit=...`.
 *
 * Все поля оболочки обязательны. Пагинация использует возвращённые сервером значения,
 * поскольку сервер может нормализовать параметры запроса.
 *
 * @property items Города текущей страницы; пустой список допустим.
 * @property page Номер страницы, возвращённый сервером; нумерация начинается с 1.
 * @property limit Размер страницы, возвращённый сервером; не равен числу элементов в общем случае.
 * @property total Общее число городов, соответствующих запросу, по всем страницам.
 */
@Serializable
internal data class CitiesPageDto(
    @SerialName("items") val items: List<CityDto>,
    @SerialName("page") val page: Int,
    @SerialName("limit") val limit: Int,
    @SerialName("total") val total: Long,
)
