package com.example.bingebox.di

import com.example.bingebox.auth.AuthTokenStorage
import com.example.bingebox.auth.BingeBoxApiClient
import com.example.bingebox.auth.BingeBoxApiConfiguration
import com.example.bingebox.auth.InMemoryAuthTokenStorage
import com.example.bingebox.data.repository.AuthRepositoryImp
import com.example.bingebox.domain.repository.AuthRepository
import com.example.bingebox.presentation.AuthViewModel
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
}

val appModule = listOf(
    appModule(),
    platformModule()
)