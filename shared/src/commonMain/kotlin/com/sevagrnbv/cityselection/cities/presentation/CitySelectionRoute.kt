package com.sevagrnbv.cityselection.cities.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.toRoute
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cityselection.shared.generated.resources.Res
import cityselection.shared.generated.resources.cities_tab_list
import cityselection.shared.generated.resources.cities_tab_map
import cityselection.shared.generated.resources.ic_list
import cityselection.shared.generated.resources.ic_map
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/** Контейнер фичи выбора города; нижняя панель принадлежит фиче, а не экрану списка. */
@Composable
internal fun CitySelectionRoute() {
    val navController = rememberNavController()
    val entry by navController.currentBackStackEntryAsState()
    val showBottomBar = entry?.destination?.hasRoute<CityListDestination>() == true
    val keyboard = LocalSoftwareKeyboardController.current
    Scaffold(bottomBar = { if (showBottomBar) CitySelectionBottomBar() }) { padding ->
        NavHost(
            navController = navController,
            startDestination = CityListDestination,
            modifier = Modifier.fillMaxSize().padding(padding).consumeWindowInsets(padding),
        ) {
            composable<CityListDestination> {
                CitiesRoute(onCitySelected = { city ->
                    keyboard?.hide()
                    navController.navigate(CityDetailsDestination(
                        id = city.id,
                        name = city.name,
                        countryName = city.countryName,
                        population = city.population,
                    )) { launchSingleTop = true }
                })
            }
            composable<CityDetailsDestination> { backStackEntry ->
                CityDetailsRoute(
                    city = backStackEntry.toRoute<CityDetailsDestination>(),
                    onBack = { navController.popBackStack() },
                )
            }
        }
    }
}

@Composable
private fun CitySelectionBottomBar() {
    Column {
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        NavigationBar(
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 0.dp,
        ) {
            val colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary,
                indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                disabledIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            NavigationBarItem(
                selected = true,
                onClick = {},
                icon = {
                    Icon(
                        painterResource(Res.drawable.ic_list),
                        stringResource(Res.string.cities_tab_list)
                    )
                },
                colors = colors,
            )
            NavigationBarItem(
                selected = false,
                onClick = {},
                enabled = false,
                icon = {
                    Icon(
                        painterResource(Res.drawable.ic_map),
                        stringResource(Res.string.cities_tab_map)
                    )
                },
                colors = colors,
            )
        }
    }
}
