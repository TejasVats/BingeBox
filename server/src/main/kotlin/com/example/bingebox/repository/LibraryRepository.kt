package com.example.bingebox.repository

import java.time.OffsetDateTime
import java.util.UUID

data class WatchListItemRecord(
    val watchModeTitleId: Long,
    val mediaType: String,
    val addedAt: OffsetDateTime
)

data class ViewingHistoryRecord(
    val watchModeTitleId: Long,
    val mediaType: String,
    val activityType: String,
    val occurredAt: OffsetDateTime
)

interface WatchListRepository{
    suspend fun add(accountId: UUID, watchModeTitleId: Long, media: String): WatchListItemRecord

    suspend fun remove(accountId: UUID, watchModeTitleId: Long, media: String): Boolean

    suspend fun list(accountId: UUID, limit: Int, offset: Long): List<WatchListItemRecord>
}

interface ViewingHistoryRepository{
    suspend fun record(accountId: UUID, watchModeTitleId: Long, mediaType: String, activityType: String): ViewingHistoryRecord
    suspend fun list(accountId: UUID, limit: Int, offset: Long): List<ViewingHistoryRecord>
}