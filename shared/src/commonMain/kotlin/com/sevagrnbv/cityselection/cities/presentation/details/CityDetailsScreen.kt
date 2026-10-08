package com.sevagrnbv.cityselection.cities.presentation.details

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.tooling.preview.Preview
import cityselection.shared.generated.resources.*
import com.sevagrnbv.cityselection.cities.presentation.common.CityDetailFields
import com.sevagrnbv.cityselection.cities.presentation.common.citySearchUrl
import com.sevagrnbv.cityselection.cities.presentation.navigation.CityDetailsDestination
import com.sevagrnbv.cityselection.core.designsystem.component.PrimaryButton
import com.sevagrnbv.cityselection.core.designsystem.component.ScreenHeader
import com.sevagrnbv.cityselection.core.designsystem.theme.CitySelectionTheme
import com.sevagrnbv.cityselection.core.designsystem.theme.IconSize
import com.sevagrnbv.cityselection.core.designsystem.theme.Spacing
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
            ScreenHeader(
                title = stringResource(Res.string.city_details_title),
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
            PrimaryButton(
                text = stringResource(Res.string.city_details_search),
                onClick = onSearch,
                modifier = Modifier.fillMaxWidth().padding(Spacing.medium),
            )
        },
        snackbarHost = snackbarHost,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.medium, vertical = Spacing.small),
        ) {
            CityDetailFields(city.name, city.countryName, city.population)
        }
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
