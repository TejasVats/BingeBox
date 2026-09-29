package com.example.bingebox.routes

import com.example.bingebox.api.API_VERSION_PREFIX
import com.example.bingebox.api.ApiResponse
import com.example.bingebox.groq.AiService
import com.example.bingebox.groq.AiServiceResult
import com.example.bingebox.plugins.ApiError
import com.example.bingebox.security.RedisRateLimiter
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.plugins.callid.callId
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class AiTextResponse(
    val text: String,
)

fun Route.aiRoutes(
    service: AiService,
    rateLimiter: RedisRateLimiter
) {
    authenticate("auth-jwt") {
        get("$API_VERSION_PREFIX/ai/recommendations") {
            val accountId = call.accountIdFromAi()
            if (!call.allowsAi(rateLimiter, accountId)) return@get
            call.respondAi(service.recommendation(accountId))
        }
        get("$API_VERSION_PREFIX/ai/insights/{titleId}") {
            val titleId = call.aiTitleId() ?: return@get
            if (!call.allowsAi(rateLimiter, call.accountIdFromAi())) return@get
            call.respondAi(service.insight(titleId))
        }
        get("$API_VERSION_PREFIX/ai/discovery") {
            val query =
                call.request.queryParameters["query"]
                    ?.trim()
                    ?.takeIf { it.length >= 2 }
                    ?: return@get call.invalidRequest("query must contain at least two characters.")
            if (!call.allowsAi(rateLimiter, call.accountIdFromAi())) return@get
            call.respondAi(service.discovery(query))
        }
        get("$API_VERSION_PREFIX/ai/viewing-summary") {
            val accountId = call.accountIdFromAi()
            if (!call.allowsAi(rateLimiter, accountId)) return@get
            call.respondAi(service.viewingSummary(accountId))
        }
    }
}

private fun ApplicationCall.accountIdFromAi(): UUID = UUID.fromString(
    principal<JWTPrincipal>()!!.payload.subject
)

private suspend fun ApplicationCall.allowsAi(
    rateLimiter: RedisRateLimiter,
    accountId: UUID
): Boolean =
    rateLimiter.allows("ai", accountId.toString(), 20).also { allowed ->
        if (!allowed) {
            respond(
                HttpStatusCode.TooManyRequests,
                aiError("ai_rate_limited", "AI request limit reached, Try again later")
            )
        }
    }

private suspend fun ApplicationCall.aiTitleId(): Long? =
    parameters["titleId"]?.toLongOrNull()?.takeIf { it > 0 } ?: run {
        invalidRequest("titleId must be a positive Watchmode Title ID")
        null
    }

private suspend fun ApplicationCall.respondAi(result: AiServiceResult) =
    when (result) {
        is AiServiceResult.Success -> respond(ApiResponse(AiTextResponse(result.text), callId))
        AiServiceResult.ContentUnavailable ->
            respond(
                HttpStatusCode.ServiceUnavailable,
                aiError("ai_content_unavailable", "Content is temporarily unavailable."),
            )
        AiServiceResult.RateLimited ->
            respond(
                HttpStatusCode.TooManyRequests,
                aiError("ai_rate_limited", "AI is busy. Try again shortly."),
            )
        AiServiceResult.Unavailable ->
            respond(
                HttpStatusCode.ServiceUnavailable,
                aiError("ai_unavailable", "AI is temporarily unavailable. Try again shortly."),
            )
    }

private suspend fun ApplicationCall.invalidRequest(message: String) =
    respond(HttpStatusCode.BadRequest, aiError("invalid_ai_requests", message))

private fun ApplicationCall.aiError(
    code: String,
    message: String,
) = ApiError(code, message, callId)