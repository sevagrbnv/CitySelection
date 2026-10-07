package com.sevagrnbv.cityselection.cities.data

import com.sevagrnbv.cityselection.cities.domain.CitiesPage
import com.sevagrnbv.cityselection.cities.domain.City

internal fun CityDto.toDomain(): City = City(
    id = id,
    name = name,
    countryCode = country,
    latitude = latitude,
    longitude = longitude,
    population = population,
)

internal fun CitiesPageDto.toDomain(): CitiesPage = CitiesPage(
    items = items.map { it.toDomain() },
    page = page,
    limit = limit,
    total = total,
)
