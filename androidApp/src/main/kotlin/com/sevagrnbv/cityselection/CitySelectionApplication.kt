package com.sevagrnbv.cityselection

import android.app.Application
import org.maplibre.android.MapLibre
import com.sevagrnbv.cityselection.di.createAppKoin
import org.koin.core.Koin
import org.koin.core.KoinApplication

class CitySelectionApplication : Application() {
    lateinit var appKoin: KoinApplication
        private set

    val koin: Koin get() = appKoin.koin

    override fun onCreate() {
        super.onCreate()
        MapLibre.getInstance(applicationContext)
        appKoin = createAppKoin()
    }
}
