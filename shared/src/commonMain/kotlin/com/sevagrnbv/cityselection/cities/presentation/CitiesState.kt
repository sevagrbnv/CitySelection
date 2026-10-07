package com.sevagrnbv.cityselection.cities.presentation

import com.sevagrnbv.cityselection.core.network.NetworkError

internal data class CityListItem(val id: Long, val name: String, val countryName: String)

internal data class CitiesState(
    val query: String = "",
    val content: CitiesContent = CitiesContent.Loading,
    val nextPage: NextPageState = NextPageState.Idle,
)

internal sealed interface CitiesContent {
    data object Loading : CitiesContent
    data object Empty : CitiesContent
    data class Error(val error: NetworkError) : CitiesContent
    data class Data(val items: List<CityListItem>, val hasNextPage: Boolean) : CitiesContent
}

internal sealed interface NextPageState {
    data object Idle : NextPageState
    data object Loading : NextPageState
    data class Error(val error: NetworkError) : NextPageState
}
