package com.sevagrnbv.cityselection.cities.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.tooling.preview.Preview
import cityselection.shared.generated.resources.*
import com.sevagrnbv.cityselection.theme.CitySelectionTheme
import com.sevagrnbv.cityselection.theme.CornerRadius
import com.sevagrnbv.cityselection.theme.IconSize
import com.sevagrnbv.cityselection.theme.Spacing
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun CityDetailsRoute(city: CityDetailsDestination, onBack: () -> Unit) {
    val uriHandler = LocalUriHandler.current
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val browserError = stringResource(Res.string.city_details_browser_error)
    CityDetailsScreen(
        city = city,
        onBack = onBack,
        onSearch = {
            try {
                uriHandler.openUri(citySearchUrl(city.name))
            } catch (_: IllegalArgumentException) {
                scope.launch { snackbar.showSnackbar(browserError) }
            }
        },
        snackbarHost = { SnackbarHost(snackbar) },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CityDetailsScreen(
    city: CityDetailsDestination,
    onBack: () -> Unit,
    onSearch: () -> Unit,
    modifier: Modifier = Modifier,
    snackbarHost: @Composable () -> Unit = {},
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(Res.string.city_details_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_arrow_back),
                            contentDescription = stringResource(Res.string.navigation_back),
                            modifier = Modifier.size(IconSize.standard),
                        )
                    }
                },
            )
        },
        bottomBar = {
            Button(
                onClick = onSearch,
                modifier = Modifier.fillMaxWidth().padding(Spacing.medium),
                shape = RoundedCornerShape(CornerRadius.medium),
            ) { Text(stringResource(Res.string.city_details_search)) }
        },
        snackbarHost = snackbarHost,
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding)
                .verticalScroll(rememberScrollState()).padding(Spacing.medium),
            verticalArrangement = Arrangement.spacedBy(Spacing.medium),
        ) {
            CityDetailField(stringResource(Res.string.city_details_name), city.name)
            CityDetailField(stringResource(Res.string.city_details_country), city.countryName)
            val population = city.population?.let {
                stringResource(Res.string.city_details_population_value, formatPopulation(it))
            } ?: stringResource(Res.string.city_details_unknown_population)
            CityDetailField(stringResource(Res.string.city_details_population), population)
        }
    }
}

@Composable
private fun CityDetailField(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.extraSmall)) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}

@Preview
@Composable
internal fun CityDetailsPreview() {
    CitySelectionTheme {
        CityDetailsScreen(
            city = CityDetailsDestination(524901, "Moscow", "Россия", 12_655_000),
            onBack = {},
            onSearch = {},
        )
    }
}
