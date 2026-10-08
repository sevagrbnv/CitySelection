package com.sevagrnbv.cityselection.cities.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Данные города в списке items ответа API.
 *
 * Идентификатор, название и страна обязательны. Дополнительные поля остаются null,
 * если отсутствуют или явно равны null; значения по умолчанию не подставляются.
 * Преобразование в модели приложения выполняется в CitiesMappers.
 *
 * @property id Идентификатор города на сервере.
 * @property name Название города, полученное от сервера.
 * @property country Двухсимвольный код страны, полученный от сервера.
 * @property latitude Широта в градусах или null, если неизвестна.
 * @property longitude Долгота в градусах или null, если неизвестна.
 * @property population Население из JSON-поля `pop` или null, если неизвестно.
 */
@Serializable
internal data class CityDto(
    @SerialName("id") val id: Long,
    @SerialName("name") val name: String,
    @SerialName("country") val country: String,
    @SerialName("lat") val latitude: Double? = null,
    @SerialName("lon") val longitude: Double? = null,
    @SerialName("pop") val population: Long? = null,
)
