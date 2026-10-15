package com.example.bingebox

import com.example.bingebox.config.ServerConfig
import com.example.bingebox.database.DatabaseMigrations
import com.example.bingebox.repository.ExposedAccountRepository
import com.example.bingebox.repository.ExposedViewingHistoryRepository
import com.example.bingebox.repository.ExposedWatchlistRepository
import com.example.bingebox.security.RedisRateLimiter
import com.example.bingebox.watchmode.WatchModeResponseCache
import kotlinx.coroutines.runBlocking
import org.junit.Assume.assumeTrue
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import org.testcontainers.DockerClientFactory
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.PostgreSQLContainer


class LibraryRepositoryIntegrationTest {
    @Test
    fun libraryRepositoriesPersistNewestFirstAndKeepAccountsIsolated() =
        runBlocking {
            assumeTrue(
                "Docker is required for PostgreSQL integration tests.",
                DockerClientFactory.instance().isDockerAvailable,
            )
            val postgres = PostgreSQLContainer<Nothing>("postgres:17-alpine")
            val redis = GenericContainer<Nothing>("redis:7-alpine")
            redis.withExposedPorts(6379)
            postgres.start()
            redis.start()
            try {
                val config =
                    ServerConfig(
                        databaseUrl = postgres.jdbcUrl,
                        databaseUser = postgres.username,
                        databasePassword = postgres.password,
                        redisUri = "redis://${redis.host}:${redis.getMappedPort(6379)}",
                        jwtSecret = "test-jwt-secret-that-is-long-enough",
                        smtpHost = "localhost",
                        smtpPort = 1025,
                        smtpFrom = "noreply@bingebox.test",
                        watchModeApiKey = "test-key",
                        watchModeBaseUrl = "http://localhost",
                        groqApiKey = "test-groq-key",
                        groqBaseUrl = "http://localhost",
                        groqFastModel = "openai/gpt-oss-20b",
                        groqAdvanceModel = "openai/gpt-oss-120b",
                        migrateOnStart = true,
                    )
                DatabaseMigrations.connect(config)
                DatabaseMigrations.migrate(config)

                val accounts = ExposedAccountRepository()
                val firstAccount = accounts.create("first-${UUID.randomUUID()}@bingebox.test", "hash")
                val secondAccount = accounts.create("second-${UUID.randomUUID()}@bingebox.test", "hash")
                val watchlist = ExposedWatchlistRepository()
                val history = ExposedViewingHistoryRepository()

                assertTrue(watchlist.list(firstAccount.id, 20, 0).isEmpty())
                assertTrue(history.list(firstAccount.id, 20, 0).isEmpty())
                watchlist.add(firstAccount.id, 101, "movie")
                watchlist.add(firstAccount.id, 202, "tv_series")
                watchlist.add(secondAccount.id, 303, "movie")
                assertEquals(
                    listOf(202L, 101L),
                    watchlist.list(firstAccount.id, 20, 0).map { it.watchModeTitleId },
                )
                assertEquals(
                    listOf(303L),
                    watchlist.list(secondAccount.id, 20, 0).map { it.watchModeTitleId },
                )
                assertFalse(watchlist.remove(secondAccount.id, 101, "movie"))
                assertTrue(watchlist.remove(firstAccount.id, 101, "movie"))

                history.record(firstAccount.id, 101, "movie", "detail_opened")
                history.record(firstAccount.id, 202, "tv_series", "trailer_started")
                history.record(secondAccount.id, 303, "movie", "detail_opened")
                assertEquals(2, history.list(firstAccount.id, 20, 0).size)
                assertEquals(1, history.list(secondAccount.id, 20, 0).size)

                val cache = WatchModeResponseCache(config)
                cache.write("title:101", "cached title")
                assertEquals("cached title", cache.read("title:101"))

                val rateLimiter = RedisRateLimiter(config)
                assertTrue(rateLimiter.allows("integration", "first-account", limit = 1))
                assertFalse(rateLimiter.allows("integration", "first-account", limit = 1))
            } finally {
                redis.stop()
                postgres.stop()
            }
        }
}