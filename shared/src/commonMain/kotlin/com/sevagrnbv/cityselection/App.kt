package com.sevagrnbv.cityselection

import androidx.compose.runtime.Composable
import com.sevagrnbv.cityselection.cities.presentation.navigation.CitySelectionRoute
import com.sevagrnbv.cityselection.core.designsystem.theme.CitySelectionTheme
import org.koin.compose.KoinIsolatedContext
import org.koin.core.KoinApplication

@Composable
fun App(appKoin: KoinApplication) {
    KoinIsolatedContext(context = appKoin) {
        CitySelectionTheme { CitySelectionRoute() }
    }
}
