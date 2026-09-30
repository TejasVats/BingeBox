package com.example.bingebox

import io.ktor.server.config.MapApplicationConfig

fun testServerConfig() =
    MapApplicationConfig(
        "bingebox.database.url" to "jdbc:postgresql://localhost:5432/bingebox_test",
        "bingebox.database.user" to "bingebox",
        "bingebox.database.password" to "test-password",
        "bingebox.redis.uri" to "redis://localhost:6379",
        "bingebox.jwt.secret" to "test-jwt-secret-that-is-long-enough",
        "bingebox.smtp.host" to "localhost",
        "bingebox.smtp.port" to "1025",
        "bingebox.smtp.from" to "noreply@bingebox.test",
        "bingebox.watchmode.api-key" to "test-watchmode-key",
        "bingebox.watchmode.base-url" to "http://localhost:9/v1",
        "bingebox.groq.api-key" to "test-groq-key",
        "bingebox.groq.base-url" to "http://localhost:9/openai/v1",
        "bingebox.groq.fast-model" to "openai/gpt-oss-20b",
        "bingebox.groq.advanced-model" to "openai/gpt-oss-120b",
        "bingebox.database.migrate-on-start" to "false",
    )