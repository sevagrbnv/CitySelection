package com.sevagrnbv.cityselection.di

import com.sevagrnbv.cityselection.cities.data.remote.CitiesApi
import com.sevagrnbv.cityselection.cities.data.repository.RemoteCitiesRepository
import com.sevagrnbv.cityselection.cities.data.repository.RemoteCityMapRepository
import com.sevagrnbv.cityselection.cities.domain.repository.CitiesRepository
import com.sevagrnbv.cityselection.cities.domain.repository.CityMapRepository
import com.sevagrnbv.cityselection.cities.presentation.common.CountryNames
import com.sevagrnbv.cityselection.cities.presentation.common.platformCountryNames
import com.sevagrnbv.cityselection.cities.presentation.list.CitiesViewModel
import com.sevagrnbv.cityselection.cities.presentation.map.CityMapViewModel
import com.sevagrnbv.cityselection.core.network.ApiConfig
import com.sevagrnbv.cityselection.core.network.createHttpClient
import com.sevagrnbv.cityselection.core.network.platformEngineFactory
import io.ktor.client.HttpClient
import org.koin.core.KoinApplication
import org.koin.core.module.dsl.viewModel
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
    viewModel { CityMapViewModel(get(), get()) }
    single<CityMapRepository> { RemoteCityMapRepository(get()) }
    single<CitiesRepository> { RemoteCitiesRepository(get()) }
}
