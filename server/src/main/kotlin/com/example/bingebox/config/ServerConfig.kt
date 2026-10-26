package com.example.bingebox.config

import io.ktor.server.application.ApplicationEnvironment

data class ServerConfig(
    val databaseUrl: String,
    val databaseUser: String,
    val databasePassword: String,
    val redisUri: String,
    val jwtSecret: String,
    val smtpHost: String,
    val smtpPort: Int,
    val migrateOnStart: Boolean,
    val smtpFrom: String,
    val watchModeApiKey: String,
    val watchModeBaseUrl: String,
    val groqApiKey: String,
    val groqBaseUrl: String,
    val groqFastModel: String,
    val groqAdvanceModel: String,
    val licensedTrailersJson: String?
)

object ServerConfigLoader {
    fun load(environment: ApplicationEnvironment): ServerConfig = ServerConfig(
        databaseUrl = environment.requiredValue("bingebox.database.url", "BINGEBOX_DATABASE_URL"),
        databaseUser = environment.requiredValue(
            "bingebox.database.user",
            "BINGEBOX_DATABASE_USER"
        ),
        databasePassword = environment.requiredValue(
            "bingebox.database.password",
            "BINGEBOX_DATABASE_PASSWORD"
        ),
        redisUri = environment.requiredValue("bingebox.redis.uri", "BINGEBOX_REDIS_URI"),
        jwtSecret = environment.requiredValue("bingebox.jwt.secret", "BINGEBOX_JWT_SECRET"),
        smtpHost = environment.requiredValue("bingebox.smtp.host", "BINGEBOX_SMTP_HOST"),
        smtpPort = environment.requiredValue("bingebox.smtp.port", "BINGEBOX_SMTP_PORT")
            .toIntOrNull()
            ?: error("Server configuration value BINGEBOX_SMTP_PORT must be a number."),
        smtpFrom = environment.requiredValue("bingebox.smtp.from", "BINGEBOX_SMTP_FROM"),
        migrateOnStart = environment.optionalValue(
            "bingebox.database.migrate-on-start",
            "BINGEBOX_DATABASE_MIGRATE_ON_START"
        )
            ?.toBooleanStrictOrNull()
            ?: true,
        watchModeApiKey = environment.requiredValue(
            "bingebox.watchmode.api-key",
            "BINGEBOX_WATCHMODE_API_KEY"
        ),
        watchModeBaseUrl = environment.optionalValue(
            "bingebox.watchmode.base-url",
            "BINGEBOX_WATCHMODE_BASE_URL"
        ) ?: "https://api.watchmode.com/v1",
        groqApiKey = environment.requiredValue("bingebox.groq.api-key", "BINGEBOX_GROQ_API_KEY"),
        groqBaseUrl =
            environment.optionalValue("bingebox.groq.base-url", "BINGEBOX_GROQ_BASE_URL")
                ?: "https://api.groq.com/openai/v1",
        groqFastModel =
            environment.optionalValue("bingebox.groq.fast-model", "BINGEBOX_GROQ_FAST_MODEL")
                ?: "llama-3.1-8b-instant",
        groqAdvanceModel =
            environment.optionalValue(
                "bingebox.groq.advanced-model",
                "BINGEBOX_GROQ_ADVANCED_MODEL"
            )
                ?: "llama-3.3-70b-versatile",
        licensedTrailersJson = environment.optionalValue(
            "bingebox.trailers.licensed-json",
            "BINGEBOX_LICENSED_TRAILERS_JSON"
        ),
    )

    private fun ApplicationEnvironment.requiredValue(
        propertyName: String,
        environmentName: String
    ): String =
        config.propertyOrNull(propertyName)?.getString()
            ?: System.getenv(environmentName)
            ?: error("Missing required Server configuration $environmentName (or $propertyName)")

    private fun ApplicationEnvironment.optionalValue(
        propertyName: String,
        environmentName: String
    ): String? =
        config.propertyOrNull(propertyName)?.getString() ?: System.getenv(environmentName)
}