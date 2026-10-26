package com.example.bingebox.di

import com.example.bingebox.EmailSender
import com.example.bingebox.SmtpEmailSender
import com.example.bingebox.auth.AuthService
import com.example.bingebox.auth.PasswordHasher
import com.example.bingebox.auth.TokenService
import com.example.bingebox.config.ServerConfig
import com.example.bingebox.groq.AiOutputValidator
import com.example.bingebox.groq.AiPromptBuilder
import com.example.bingebox.groq.AiResponseCache
import com.example.bingebox.groq.AiService
import com.example.bingebox.groq.GroqAiService
import com.example.bingebox.groq.GroqClient
import com.example.bingebox.groq.RedisAiResponseCache
import com.example.bingebox.repository.AccountRepository
import com.example.bingebox.repository.AccountTokenRepository
import com.example.bingebox.repository.ExposedAccountRepository
import com.example.bingebox.repository.ExposedAccountTokenRepository
import com.example.bingebox.repository.ExposedRefreshSessionRepository
import com.example.bingebox.repository.ExposedViewingHistoryRepository
import com.example.bingebox.repository.ExposedWatchlistRepository
import com.example.bingebox.repository.RefreshSessionRepository
import com.example.bingebox.repository.ViewingHistoryRepository
import com.example.bingebox.repository.WatchListRepository
import com.example.bingebox.security.RedisRateLimiter
import com.example.bingebox.watchmode.ContentCatalogService
import com.example.bingebox.watchmode.ContentResponseCache
import com.example.bingebox.watchmode.LicensedTrailerCatalog
import com.example.bingebox.watchmode.WatchModeClient
import com.example.bingebox.watchmode.WatchModeResponseCache
import org.koin.core.module.Module
import org.koin.dsl.module

fun serverModule(config: ServerConfig): Module = module {
    single { config }
    single { PasswordHasher() }
    single { TokenService(get()) }
    single<AccountRepository> { ExposedAccountRepository() }
    single<RefreshSessionRepository> { ExposedRefreshSessionRepository() }
    single<AccountTokenRepository> { ExposedAccountTokenRepository() }
    single<EmailSender> { SmtpEmailSender(get()) }
    single { RedisRateLimiter(get()) }
    single<ContentResponseCache> { WatchModeResponseCache(get()) }
    single { WatchModeClient(get(), get()) }
    single {
        AuthService(
            get(), get(), get(),
            get(), get(), get()
        )
    }
    single<WatchListRepository> { ExposedWatchlistRepository() }
    single<ViewingHistoryRepository> { ExposedViewingHistoryRepository() }
    single { GroqClient(get()) }
    single { AiPromptBuilder() }
    single<AiResponseCache> { RedisAiResponseCache(get()) }
    single { AiOutputValidator() }
    single<AiService> {
        GroqAiService(
            get(), get(), get(), get(),
            get(), get(), get()
        )
    }
    single { LicensedTrailerCatalog.fromConfig(get()) }
    single { ContentCatalogService(get(), get()) }
}