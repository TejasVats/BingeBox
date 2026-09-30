package com.example.bingebox

import com.example.bingebox.config.ServerConfig
import com.example.bingebox.groq.GroqClient
import com.example.bingebox.groq.GroqModel
import com.example.bingebox.groq.GroqResult
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.HttpRequestData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals

class GroqClientTest {
    @Test fun `client sends the configured model and keeps the API key in the authorization header`() =
        runBlocking {
            var request: HttpRequestData? = null
            val http =
                HttpClient(
                    MockEngine {
                        request = it
                        respond(
                            "{\"choices\":[{\"message\":{\"role\":\"assistant\",\"content\":\"Try Arrival.\"}}]}",
                            HttpStatusCode.OK,
                        )
                    },
                )

            val result = GroqClient(config(), http).complete(GroqModel.FAST, "Be concise.", "Recommend a film.")

            assertEquals(GroqResult.Success("Try Arrival."), result)
            assertEquals("Bearer test-groq-key", request!!.headers[HttpHeaders.Authorization])
            assertContains(request.body.toString(), "openai/gpt-oss-20b")
        }

    private fun config() =
        ServerConfig(
            databaseUrl = "jdbc:postgresql://localhost:5432/nstream_test",
            databaseUser = "bingebox",
            databasePassword = "test",
            redisUri = "redis://localhost:6379",
            jwtSecret = "test-secret",
            smtpHost = "localhost",
            smtpPort = 1025,
            smtpFrom = "noreply@nstream.test",
            watchModeApiKey = "test-watchmode-key",
            watchModeBaseUrl = "https://watchmode.test/v1",
            groqApiKey = "test-groq-key",
            groqBaseUrl = "https://groq.test/openai/v1",
            groqFastModel = "openai/gpt-oss-20b",
            groqAdvanceModel = "openai/gpt-oss-120b",
            migrateOnStart = false,
        )
}