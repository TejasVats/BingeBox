package com.example.bingebox

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.example.bingebox.di.appModule
import org.koin.core.context.startKoin

fun main() = application {
    startKoin {
        modules(appModule + desktopAppModule)
    }
    Window(
        onCloseRequest = ::exitApplication,
        title = "BingeBox",
    ) {
        App()
    }
}