package com.example.bingebox

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.testApplication
import kotlin.test.Test
import kotlin.test.assertEquals

class ContentRoutesTest {
    @Test
    fun invalidWatchmodeTitleIdsAreRejectedBeforeCallingTheProvider() = testApplication {
        environment { config = testServerConfig() }
        application { module() }

        val response = client.get("/api/v1/content/not-a-number") {
            header(HttpHeaders.Authorization, "Bearer ${testAccessToken()}")
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun contentRoutesRequireAnAccessToken() = testApplication {
        environment { config = testServerConfig() }
        application { module() }

        assertEquals(HttpStatusCode.Unauthorized, client.get("/api/v1/content/titles").status)
    }

    private fun testAccessToken(): String = JWT.create()
        .withSubject("00000000-0000-0000-0000-000000000001")
        .sign(Algorithm.HMAC256("test-jwt-secret-that-is-long-enough"))
}