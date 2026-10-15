package com.example.bingebox

import com.example.bingebox.config.ServerConfig
import com.example.bingebox.core.content.ContentPageResponse
import com.example.bingebox.watchmode.ContentResponseCache
import com.example.bingebox.watchmode.WatchModeClient
import com.example.bingebox.watchmode.WatchModeResults
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class WatchmodeClientTest {
    @Test
    fun cachedListIsUsedWithoutCallingWatchmode() =
        runBlocking {
            val cache =
                FakeCache().apply {
                    entries["list-movie,tv_series-null-1"] =
                        "{\"titles\":[{\"id\":11,\"title\":\"Cached title\",\"type\":\"movie\"}],\"page\":1,\"total_pages\":1,\"total_results\":1}"
                }
            val result =
                WatchModeClient(
                    config(),
                    cache,
                    HttpClient(
                        MockEngine {
                            error("Watchmode must not be called")
                        },
                    ),
                ).listTitles("movie,tv_series", null, 1)
            val page = assertIs<WatchModeResults.Success<*>>(result).value as ContentPageResponse
            assertEquals("Cached title", page.items.single().title)
        }

    @Test fun rateLimitedAndUnavailableProvidersReturnSafeResults() =
        runBlocking {
            val limited =
                WatchModeClient(
                    config(),
                    FakeCache(),
                    HttpClient(MockEngine { respond("{}", HttpStatusCode.TooManyRequests) }),
                )
            val unavailable =
                WatchModeClient(
                    config(),
                    FakeCache(),
                    HttpClient(MockEngine { respond("{}", HttpStatusCode.BadGateway) }),
                )
            assertEquals(WatchModeResults.RateLimited, limited.listTitles("movie", null, 1))
            assertEquals(WatchModeResults.Unavailable, unavailable.listTitles("movie", null, 1))
        }

    private fun config() =
        ServerConfig(
            "jdbc:postgresql://localhost:5432/bingebox_test",
            "bingebox",
            "test",
            "redis://localhost:6379",
            "test-secret",
            "localhost",
            1025,
            false, // migrateOnStart
            "noreply@bingebox.test",
            "test-key",
            "https://watchmode.test/v1",
            "test-groq-key",
            "https://groq.test/openai/v1",
            "openai/gpt-oss-20b",
            "openai/gpt-oss-120b",
        )

    private class FakeCache : ContentResponseCache {
        val entries = mutableMapOf<String, String>()

        override fun read(key: String) = entries[key]

        override fun write(
            key: String,
            body: String,
            seconds: Long,
        ) {
            entries[key] =
                body
        }
    }
}