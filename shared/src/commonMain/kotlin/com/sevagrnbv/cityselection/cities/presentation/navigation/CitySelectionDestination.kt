package com.sevagrnbv.cityselection.cities.presentation.navigation

import kotlinx.serialization.Serializable

@Serializable
internal data object CityListDestination

@Serializable
internal data object CityMapDestination

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
