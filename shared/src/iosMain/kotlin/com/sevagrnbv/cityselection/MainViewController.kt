package com.sevagrnbv.cityselection

import androidx.compose.ui.window.ComposeUIViewController

// Общий граф для контроллеров: пересоздание SwiftUI-представления не создаёт новый HTTP-клиент.
internal val appKoin by lazy { com.sevagrnbv.cityselection.di.createAppKoin() }

fun MainViewController(): platform.UIKit.UIViewController {
    appKoin
    return ComposeUIViewController { App() }
}