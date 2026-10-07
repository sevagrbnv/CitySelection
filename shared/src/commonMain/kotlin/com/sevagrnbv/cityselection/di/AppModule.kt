package com.sevagrnbv.cityselection.di

import com.sevagrnbv.cityselection.cities.data.CitiesApi
import com.sevagrnbv.cityselection.cities.data.RemoteCitiesRepository
import com.sevagrnbv.cityselection.cities.domain.CitiesRepository
import com.sevagrnbv.cityselection.core.network.ApiConfig
import com.sevagrnbv.cityselection.core.network.createHttpClient
import com.sevagrnbv.cityselection.core.network.platformEngineFactory
import com.sevagrnbv.cityselection.cities.presentation.CitiesViewModel
import com.sevagrnbv.cityselection.cities.presentation.CountryNames
import com.sevagrnbv.cityselection.cities.presentation.platformCountryNames
import org.koin.core.module.dsl.viewModel
import io.ktor.client.HttpClient
import org.koin.core.KoinApplication
import org.koin.dsl.koinApplication
import org.koin.dsl.module
import org.koin.dsl.onClose

/** Изолированный граф DI; платформенный код управляет его жизненным циклом. */
fun createAppKoin(): KoinApplication = koinApplication {
    modules(appModule)
}

private val appModule = module {
    single<HttpClient> { createHttpClient(platformEngineFactory()) } onClose { it?.close() }
    single { CitiesApi(get(), ApiConfig.BASE_URL) }
    single<CountryNames> { platformCountryNames() }
    viewModel { CitiesViewModel(get(), get()) }
    single<CitiesRepository> { RemoteCitiesRepository(get()) }
}
