package com.sevagrnbv.cityselection.cities.presentation.map

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import org.koin.compose.viewmodel.koinViewModel
import org.orbitmvi.orbit.compose.collectAsState

/** Связывает состояние и события экрана с MVI-контейнером. */
@Composable
internal fun CityMapRoute(viewModel: CityMapViewModel = koinViewModel()) {
    val state by viewModel.collectAsState()
    CityMapScreen(
        state = state,
        onViewportChanged = viewModel::onViewportChanged,
        onCitySelected = { viewModel.selectCity(it) },
        onDismissCity = { viewModel.dismissCity() },
        onRetryCities = { viewModel.retry() },
    )
}
