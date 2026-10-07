package com.sevagrnbv.cityselection.cities.presentation

import kotlinx.serialization.Serializable

@Serializable
internal data object CityListDestination

/**
 * Снимок выбранного города для деталей. API не предоставляет запрос по id;
 * значения сохраняются в аргументах навигации и доступны после восстановления экрана.
 */
@Serializable
internal data class CityDetailsDestination(
    val id: Long,
    val name: String,
    val countryName: String,
    val population: Long?,
)
