package com.example.bingebox.di

import com.example.bingebox.data.auth.local.AuthTokenStorage
import com.example.bingebox.data.auth.remote.BingeBoxApiClient
import com.example.bingebox.data.auth.remote.BingeBoxApiConfiguration
import com.example.bingebox.data.auth.local.InMemoryAuthTokenStorage
import com.example.bingebox.data.auth.repository.AuthRepositoryImp
import com.example.bingebox.data.repository.ContentRepositoryImp
import com.example.bingebox.data.repository.LibraryRepositoryImp
import com.example.bingebox.domain.auth.AuthRepository
import com.example.bingebox.domain.repository.ContentRepository
import com.example.bingebox.domain.repository.LibraryRepository
import com.example.bingebox.presentation.auth.AuthViewModel
import com.example.bingebox.presentation.viewmodel.ContentDetailViewModel
import com.example.bingebox.presentation.viewmodel.HomeViewModel
import com.example.bingebox.presentation.viewmodel.NavigationViewModel
import com.example.bingebox.presentation.viewmodel.SearchViewModel
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.core.module.Module
import org.koin.dsl.module

fun appModule(): Module = module {
    single {
        HttpClient {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    explicitNulls = false
                })
            }
        }
    }
    single { BingeBoxApiConfiguration() }
    single<AuthTokenStorage> { InMemoryAuthTokenStorage() }
    single { BingeBoxApiClient(get(), get(), get()) }
    single<AuthRepository> { AuthRepositoryImp(get()) }
    factory { AuthViewModel(get()) }
    single<ContentRepository> { ContentRepositoryImp(get()) }
    single<LibraryRepository> { LibraryRepositoryImp(get()) }
    factory { NavigationViewModel() }
    factory { HomeViewModel(get()) }
    factory { SearchViewModel(get()) }
    factory { ContentDetailViewModel(get(), get()) }
}

val appModule = listOf(
    appModule(),
    platformModule()
)