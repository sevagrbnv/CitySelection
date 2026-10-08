package com.sevagrnbv.cityselection.cities.presentation.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import cityselection.shared.generated.resources.Res
import cityselection.shared.generated.resources.cities_tab_list
import cityselection.shared.generated.resources.cities_tab_map
import cityselection.shared.generated.resources.ic_list
import cityselection.shared.generated.resources.ic_map
import com.sevagrnbv.cityselection.cities.presentation.details.CityDetailsRoute
import com.sevagrnbv.cityselection.cities.presentation.list.CitiesRoute
import com.sevagrnbv.cityselection.cities.presentation.map.CityMapRoute
import com.sevagrnbv.cityselection.core.designsystem.theme.ComponentColors
import com.sevagrnbv.cityselection.core.designsystem.theme.ComponentSize
import com.sevagrnbv.cityselection.core.designsystem.theme.IconSize
import com.sevagrnbv.cityselection.core.designsystem.theme.TextColors
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/** Контейнер фичи выбора города; нижняя панель принадлежит фиче, а не экрану списка. */
@Composable
internal fun CitySelectionRoute() {
    val navController = rememberNavController()
    val entry by navController.currentBackStackEntryAsState()
    val keyboard = LocalSoftwareKeyboardController.current
    val selectedTab = when {
        entry?.destination?.hasRoute<CityListDestination>() == true -> CitySelectionTab.LIST
        entry?.destination?.hasRoute<CityMapDestination>() == true -> CitySelectionTab.MAP
        else -> null
    }
    var listError by remember { mutableStateOf(false) }
    val showBottomBar = selectedTab != null && (selectedTab != CitySelectionTab.LIST || !listError)
    val selectTab: (CitySelectionTab) -> Unit = { tab ->
        keyboard?.hide()
        when (tab) {
            CitySelectionTab.MAP -> navController.navigate(CityMapDestination) {
                launchSingleTop = true
                restoreState = true
            }
            CitySelectionTab.LIST -> {
                navController.popBackStack<CityListDestination>(inclusive = false, saveState = true)
            }
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        bottomBar = {
            if (showBottomBar) CitySelectionBottomBar(selectedTab, selectTab)
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = CityListDestination,
            modifier = Modifier.fillMaxSize().padding(padding).consumeWindowInsets(padding),
        ) {
            composable<CityListDestination> {
                CitiesRoute(
                    onFullScreenErrorChanged = { listError = it },
                    onCitySelected = { city ->
                        keyboard?.hide()
                        navController.navigate(
                            CityDetailsDestination(
                                id = city.id,
                                name = city.name,
                                countryName = city.countryName,
                                population = city.population,
                            )
                        ) { launchSingleTop = true }
                    })
            }
            composable<CityMapDestination> { CityMapRoute() }
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
private fun CitySelectionBottomBar(
    selectedTab: CitySelectionTab,
    onTabSelected: (CitySelectionTab) -> Unit,
) {
    Row(
        Modifier.fillMaxWidth()
            .dropShadow(
                RectangleShape,
                Shadow(radius = 3.dp, color = Color.Black.copy(alpha = 0.15f))
            )
            .background(MaterialTheme.colorScheme.surface)
            .windowInsetsPadding(citySelectionBottomBarInsets())
            .heightIn(min = ComponentSize.navigationHeight).padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CitySelectionTab.entries.forEach { tab ->
            val selected = selectedTab == tab
            Box(
                Modifier.weight(1f).heightIn(min = ComponentSize.navigationHeight)
                    .clickable(role = Role.Tab) {
                        if (!selected) onTabSelected(tab)
                    }.semantics { this.selected = selected },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painterResource(tab.icon),
                    stringResource(tab.label),
                    tint = if (selected) MaterialTheme.colorScheme.primary else TextColors.Secondary,
                    modifier = Modifier
                        .background(
                            if (selected) ComponentColors.NavigationIndicator else Color.Transparent,
                            RoundedCornerShape(32.dp)
                        )
                        .padding(horizontal = 20.dp, vertical = 4.dp)
                        .size(IconSize.standard)
                )
            }
        }
    }
}

private enum class CitySelectionTab(val icon: DrawableResource, val label: StringResource) {
    LIST(Res.drawable.ic_list, Res.string.cities_tab_list),
    MAP(Res.drawable.ic_map, Res.string.cities_tab_map),
}

/** На iOS панель доходит до края экрана; Android сохраняет отступ системной навигации. */
@Composable
internal expect fun citySelectionBottomBarInsets(): WindowInsets
