package com.example.bingebox.di

import com.example.bingebox.config.ServerConfig
import org.koin.core.module.Module
import org.koin.dsl.module

fun serverModule(config: ServerConfig): Module = module {
    single { config }
}