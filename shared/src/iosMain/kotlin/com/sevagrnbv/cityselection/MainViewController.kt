package com.sevagrnbv.cityselection

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.window.ComposeUIViewController
import com.sevagrnbv.cityselection.cities.presentation.map.IosCityMapFactory
import com.sevagrnbv.cityselection.cities.presentation.map.LocalIosCityMapFactory

// Общий граф для контроллеров: пересоздание SwiftUI-представления не создаёт новый HTTP-клиент.
internal val appKoin by lazy { com.sevagrnbv.cityselection.di.createAppKoin() }

fun MainViewController(mapFactory: IosCityMapFactory): platform.UIKit.UIViewController {
    return ComposeUIViewController {
        CompositionLocalProvider(LocalIosCityMapFactory provides mapFactory) { App(appKoin) }
    }
}