package com.example.bingebox.routes

import com.example.bingebox.config.ServerConfig
import io.ktor.http.HttpStatusCode
import io.ktor.server.plugins.swagger.swaggerUI
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import kotlinx.serialization.Serializable

@Serializable
data class ServiceStatus(val status: String)

fun Route.systemRoute(config: ServerConfig){
    get("/health") {
        call.respond(HttpStatusCode.OK,ServiceStatus(status = "ok"))
    }
    get("/ready"){
        val isConfigured = config.databaseUrl.isNotBlank() && config.redisUrl.isNotBlank()
        call.respond(
            if(isConfigured) HttpStatusCode.OK else HttpStatusCode.ServiceUnavailable,
            ServiceStatus(if (isConfigured) "ready" else "not_ready")
        )
    }
    swaggerUI(path = "swagger", swaggerFile = "openai/openapi.yaml")
}
