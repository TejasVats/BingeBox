package com.example.bingebox.routes

import com.example.bingebox.api.API_VERSION_PREFIX
import com.example.bingebox.plugins.ApiError
import com.example.bingebox.watchmode.ContentCatalogService
import com.example.bingebox.watchmode.WatchModeResults
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.auth.authenticate
import io.ktor.server.plugins.callid.callId
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get

fun Route.contentRoutes(contentCatalogService: ContentCatalogService) {
    authenticate("auth-jwt") {
        get("$API_VERSION_PREFIX/content/titles") {
            val types = call.request.queryParameters["types"] ?: "movie, tv_series"
            val page = call.page() ?: return@get
            call.respondWatchMode(contentCatalogService.listTitles(types, call.region(), page))
        }
        get("$API_VERSION_PREFIX/content/search") {
            val query =
                call.request.queryParameters["query"]?.trim()?.takeIf { it.length >= 2 } ?: run {
                    call.invalidContentRequest("query must contain least two characters")
                    return@get
                }
            call.respondWatchMode(
                contentCatalogService.search(
                    query,
                    call.request.queryParameters["types"]
                )
            )
        }
        get("$API_VERSION_PREFIX/content/{titleId}") {
            val id = call.titleId() ?: return@get
            call.respondWatchMode(contentCatalogService.details(id))
        }
        get("$API_VERSION_PREFIX/content/{titleId}/sources") {
            val id = call.titleId() ?: return@get
            call.respondWatchMode(contentCatalogService.sources(id, call.region()))
        }
    }
}

private fun ApplicationCall.page(): Int? = request.queryParameters["page"]?.toIntOrNull()?.takeIf {
    it in 1..250
} ?: when {
    request.queryParameters["page"] == null -> 1
    else -> null
}

private fun ApplicationCall.region(): String? =
    request.queryParameters["region"]?.uppercase()?.takeIf { it.matches(Regex("[A-Z]{2}")) }

private suspend fun ApplicationCall.titleId(): Long? =
    parameters["titleId"]?.toLongOrNull()?.takeIf { it > 0 }
        ?: run { invalidContentRequest("titleId must be a positive WatchMode title ID."); null }

private suspend fun ApplicationCall.invalidContentRequest(message: String) {
    respond(HttpStatusCode.BadRequest, ApiError("invalid_content_request", message, callId))
}

private suspend fun <T> ApplicationCall.respondWatchMode(result: WatchModeResults<T>) =
    when (result) {
        is WatchModeResults.Success -> respond(result.value as Any)
        WatchModeResults.NotFound -> respond(
            HttpStatusCode.NotFound,
            ApiError("content_not_found", "This title is unavailable.", callId)
        )

        WatchModeResults.RateLimited -> respond(
            HttpStatusCode.ServiceUnavailable,
            ApiError(
                "content_provider_rate_limited",
                "Content is temporarily unavailable. Try again shortly.",
                callId
            )
        )

        WatchModeResults.Unavailable -> respond(
            HttpStatusCode.ServiceUnavailable,
            ApiError(
                "content_provider_unavailable",
                "Content is temporarily unavailable. Try again shortly.",
                callId
            )
        )
    }