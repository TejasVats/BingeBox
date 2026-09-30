package com.example.bingebox

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.config.MapApplicationConfig
import io.ktor.server.testing.testApplication
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class SystemRoutesTest {

    @Test
    fun healthRouteReportsOkAndReturnsTheRequestID() = testApplication {
        environment {
            config = testServerConfig()
        }
        application {
            module()
        }

        val response = client.get("/health") {
            header(HttpHeaders.XRequestId, "test-request-id")
        }

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(
            "test-request-id", response.headers[HttpHeaders.XRequestId]
        )
    }

    @Test
    fun deployedBackendHealthIsReachable() = runBlocking {
        val baseUrl = System.getenv("BACKEND_BASE_URL")
            ?: error("Set BACKEND_BASE_URL, e.g. http://localhost:8080")

        val client = HttpClient(CIO)

        try {
            val response = client.get("$baseUrl/health") {
                header(HttpHeaders.XRequestId, "test-request-id")
            }

            assertEquals(HttpStatusCode.OK, response.status)
            assertEquals("test-request-id", response.headers[HttpHeaders.XRequestId])
        } finally {
            client.close()
        }
    }

    @Test
    fun startupConfigurationHasAllRequiredValues() {
        val config: MapApplicationConfig = testServerConfig()
        assertNotNull(config.propertyOrNull("nstream.database.url"))
    }

    @Test
    fun swaggerUiIsAvailableLocally() = testApplication {
        environment {
            config = testServerConfig()
        }
        application {
            module()
        }

        val response = client.get("/swagger")

        assertEquals(HttpStatusCode.OK, response.status)
    }
}