package com.example.bingebox

import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.testApplication
import kotlin.test.Test
import kotlin.test.assertEquals

class LibraryRoutesTest {
    @Test
    fun libraryEndpointsRequireAnAccessToken() = testApplication {
        environment { config = testServerConfig() }
        application { module() }

        assertEquals(HttpStatusCode.Unauthorized, client.get("/api/v1/library/watchlist").status)
        assertEquals(HttpStatusCode.Unauthorized, client.get("/api/v1/library/history").status)
    }
}