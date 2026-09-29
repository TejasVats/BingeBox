package com.example.bingebox.groq

import com.example.bingebox.config.ServerConfig
import io.lettuce.core.RedisClient

interface AiResponseCache {
    fun read(key: String): String?
    fun write(
        key: String,
        text: String,
        seconds: Long = 300
    )
}

class RedisAiResponseCache(
    config: ServerConfig
) : AiResponseCache {
    private val commands by lazy { RedisClient.create(config.redisUri).connect().sync() }
    override fun read(key: String): String? = runCatching {
        commands.get("ai:$key")
    }.getOrNull()

    override fun write(key: String, text: String, seconds: Long) {
        runCatching { commands.setex("ai:$key", seconds, text) }
    }
}