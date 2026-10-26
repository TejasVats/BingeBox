package com.example.bingebox.di

import com.example.bingebox.data.auth.remote.BingeBoxApiConfiguration
import com.example.bingebox.data.library.LocalLibraryCache
import com.example.bingebox.data.library.createRoomLocalLibraryCache
import com.example.bingebox.presentation.BrowserTrailerPlayer
import com.example.bingebox.presentation.screens.TrailerPlayer
import org.koin.core.module.Module
import org.koin.dsl.module

actual fun platformModule(): Module = module {
    single { BingeBoxApiConfiguration(baseUrl = "http://localhost:8080") }
    single<LocalLibraryCache> {  createRoomLocalLibraryCache("bingebox-library.db") }
    single<TrailerPlayer> { BrowserTrailerPlayer("Wasm Browser trailer tab") }
}