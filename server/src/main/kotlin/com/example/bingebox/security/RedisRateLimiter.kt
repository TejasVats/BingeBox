package com.example.bingebox.security

import com.example.bingebox.config.ServerConfig
import io.lettuce.core.RedisClient
import kotlin.getValue

class RedisRateLimiter(config: ServerConfig) {
    private val commands by lazy { RedisClient.create(config.redisUrl).connect().sync() }

    fun allows(action: String, identity: String, limit: Long = 5) = runCatching {
        val key = "rate-limit:$action:${identity.lowercase()}"
        val count = commands.incr(key)
        if(count == 1L) commands.expire(key, 60)
        count <= limit
    }.getOrDefault(true)
}