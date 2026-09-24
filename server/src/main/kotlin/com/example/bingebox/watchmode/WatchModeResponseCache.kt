package com.example.bingebox.watchmode

import com.example.bingebox.config.ServerConfig
import io.lettuce.core.RedisClient

interface ContentResponseCache{
    fun read(key: String): String?;

    fun write(key: String, body: String, seconds: Long= 300)
}

class WatchModeResponseCache(config: ServerConfig): ContentResponseCache{

    private val commands by lazy { RedisClient.create(config.redisUri).connect().sync()}

    override fun read(key: String): String? = runCatching {
        commands.get("watchmode:$key")
    }.getOrNull()

    override fun write(key: String, body: String, seconds: Long) {
        runCatching {
            commands.setex("watchmode:$key", seconds, body)
        }
    }


}