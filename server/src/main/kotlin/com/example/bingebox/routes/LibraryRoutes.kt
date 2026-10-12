package com.example.bingebox.routes

import com.example.bingebox.api.API_VERSION_PREFIX
import com.example.bingebox.api.ApiResponse
import com.example.bingebox.api.PaginatedResponse
import com.example.bingebox.api.pageMetadata
import com.example.bingebox.core.library.ActivityRequest
import com.example.bingebox.core.library.WatchListItemResponse
import com.example.bingebox.core.library.WatchListRequest
import com.example.bingebox.plugins.ApiError
import com.example.bingebox.repository.ViewingHistoryRecord
import com.example.bingebox.repository.ViewingHistoryRepository
import com.example.bingebox.repository.WatchListItemRecord
import com.example.bingebox.repository.WatchListRepository
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.plugins.callid.callId
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class ViewingHistoryResponse(
    val titleId: Long,
    val mediaType: String,
    val activityType: String,
    val occurredAt: String
)

fun Route.libraryRoutes(
    watchListRepository: WatchListRepository,
    viewingHistoryRepository: ViewingHistoryRepository
) {
    authenticate("auth-jwt") {
        post("$API_VERSION_PREFIX/library/watchlist") {
            val request = call.receive<WatchListRequest>()
            if (!request.isValidContent()) {
                call.respond(HttpStatusCode.UnprocessableEntity, invalidLibraryRequest(call.callId))
                return@post
            }
            val item = watchListRepository.add(call.accountId(), request.titleId, request.mediaType)
            call.respond(HttpStatusCode.Created, ApiResponse(item.toResponse(), call.callId))
        }
        delete("$API_VERSION_PREFIX/library/watchlist/{titleId}") {
            val titleId = call.parameters["titleId"]?.toLongOrNull()
            val mediaType = call.request.queryParameters["mediaType"] ?: ""
            if (titleId == null || titleId < 0 || mediaType !in mediaTypes) {
                call.respond(
                    HttpStatusCode.UnprocessableEntity,
                    invalidLibraryRequest(call.callId)
                )
                return@delete
            }
            if (!watchListRepository.remove(call.accountId(), titleId, mediaType)) {
                call.respond(
                    HttpStatusCode.NotFound,
                    ApiError("not_found", "The watchlist item was not found", call.callId)
                )
                return@delete
            }
            call.respond(HttpStatusCode.NoContent)
        }

        get("$API_VERSION_PREFIX/library/watchlist") {
            val page = call.pageRequest() ?: return@get
            val items = watchListRepository.list(call.accountId(), page.limit, page.offset)
            call.respond(
                PaginatedResponse(
                    items.map { it.toResponse() },
                    pageMetadata(page.limit, page.offset, items.size),
                    call.callId
                )
            )
        }
        post("$API_VERSION_PREFIX/library/history") {
            val request = call.receive<ActivityRequest>()
            if (!request.isValidContent() || request.activityType !in activityTypes) {
                call.respond(
                    HttpStatusCode.UnprocessableEntity,
                    invalidLibraryRequest(call.callId)
                )
                return@post
            }
            val activity = viewingHistoryRepository.record(
                call.accountId(),
                request.titleId,
                request.mediaType,
                request.activityType
            )
            call.respond(
                HttpStatusCode.Created,
                ApiResponse(
                    activity.toResponse(),
                    call.callId
                )
            )
        }
        get("$API_VERSION_PREFIX/library/history"){
            val page = call.pageRequest() ?: return@get
            val activities = viewingHistoryRepository.list(call.accountId(), page.limit,page.offset)
            call.respond(
                PaginatedResponse(
                    activities.map { it.toResponse() },
                    pageMetadata(page.limit, page.offset, activities.size),
                    call.callId
                )
            )
        }
    }
}

private val mediaTypes = setOf("movie", "tv")
private val activityTypes = setOf("detail_opened", "trailer_started")

private fun WatchListRequest.isValidContent() = titleId > 0 && mediaType in mediaTypes

private fun ActivityRequest.isValidContent() = titleId > 0 && mediaType in mediaTypes

private fun WatchListItemRecord.toResponse() =
    WatchListItemResponse(watchModeTitleId, mediaType, addedAt.toString())

private fun ViewingHistoryRecord.toResponse() =
    ViewingHistoryResponse(watchModeTitleId, mediaType, activityType, occurredAt.toString())

private fun ApplicationCall.accountId(): UUID =
    UUID.fromString(principal<JWTPrincipal>()!!.payload.subject)

private fun invalidLibraryRequest(requestId: String?) =
    ApiError(
        "validation-error",
        "Provide a valid title Id, media Type and activity Type",
        requestId
    )

private data class PageRequest(val limit: Int, val offset: Long)

private suspend fun ApplicationCall.pageRequest(): PageRequest? {
    val limit = request.queryParameters["limits"]?.toIntOrNull() ?: 20
    val offset = request.queryParameters["offset"]?.toLongOrNull() ?: 0
    return if (limit in 1..100 && offset >= 0) PageRequest(limit, offset) else {
        respond(
            HttpStatusCode.UnprocessableEntity,
            invalidLibraryRequest(callId)
        )
        null
    }
}