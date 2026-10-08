package com.sevagrnbv.cityselection.cities.data.mapper

import com.sevagrnbv.cityselection.cities.data.remote.CitiesPageDto
import com.sevagrnbv.cityselection.cities.data.remote.CityDto
import com.sevagrnbv.cityselection.cities.domain.model.CitiesPage
import com.sevagrnbv.cityselection.cities.domain.model.City

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
