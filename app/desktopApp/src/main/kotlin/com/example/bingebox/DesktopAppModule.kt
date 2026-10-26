package com.example.bingebox

import com.example.bingebox.presentation.screens.TrailerPlayer
import org.koin.dsl.module

val desktopAppModule = module {
    single<TrailerPlayer> { DesktopJavaFxTrailerPlayer() }
}