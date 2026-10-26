package com.example.bingebox

import androidx.compose.ui.window.ComposeUIViewController
import com.example.bingebox.di.appModule
import com.example.bingebox.presentation.IosTrailerPlayerHost
import org.koin.core.context.startKoin

fun initKoin() {
    startKoin {
        modules(appModule)
    }
}

fun MainViewController() = ComposeUIViewController(
    configure = { initKoin() },
) { App() }
    .also { viewController ->
        IosTrailerPlayerHost.rootViewController = viewController
    }