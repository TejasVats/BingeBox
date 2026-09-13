package com.example.bingebox.config

import io.ktor.server.application.ApplicationEnvironment

data class ServerConfig (
    val databaseUrl: String,
    val databaseUser: String,
    val databasePassword: String,
    val redisUrl: String,
    val jwtSecret: String,
    val smtpHost: String,
    val smtpPort: Int,
    val migrateOnStart: Boolean
)

object ServerConfigLoader {
    fun load(environment: ApplicationEnvironment): ServerConfig = ServerConfig(
        databaseUrl = environment.requiredValue("bingebox.database.url", "BINGEBOX_DATABASE_URL"),
        databaseUser = environment.requiredValue("bingebox.database.user", "BINGEBOX_DATABASE_USER"),
        databasePassword = environment.requiredValue("bingebox.database.password", "BINGEBOX_DATABASE_PASSWORD"),
        redisUrl = environment.requiredValue("bingebox.redis.uri", "BINGEBOX_REDIS_URI"),
        jwtSecret = environment.requiredValue("bingebox.jwt.secret", "BINGEBOX_JWT_SECRET"),
        smtpHost = environment.requiredValue("bingebox.smtp.host", "BINGEBOX_SMTP_HOST"),
        smtpPort = environment.requiredValue("bingebox.smtp.port", "BINGEBOX_SMTP_PORT").toIntOrNull()
            ?: error("Server configuration value BINGEBOX_SMTP_PORT must be a number."),
        migrateOnStart = environment.optionalValue("bingebox.database.migrate-on-start", "BINGEBOX_DATABASE_MIGRATE_ON_START")
            ?.toBooleanStrictOrNull()
            ?: true,
    )

    private fun ApplicationEnvironment.requiredValue(propertyName: String, environmentName: String) : String =
        config.propertyOrNull(propertyName)?.getString()
            ?: System.getenv(environmentName)
            ?: error("Missing required Server configuration $environmentName (or $propertyName)")

    private fun ApplicationEnvironment.optionalValue(propertyName: String, environmentName: String): String? =
        config.propertyOrNull(propertyName)?.getString() ?: System.getenv(environmentName)
}