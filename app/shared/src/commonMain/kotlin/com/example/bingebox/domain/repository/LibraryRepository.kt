package com.example.bingebox.domain.repository

import com.example.bingebox.core.content.ContentDetailsResponse
import com.example.bingebox.core.library.ViewingHistoryResponse
import com.example.bingebox.core.library.WatchListItemResponse

const val DETAIL_OPENED_ACTIVITY = "detail_opened"
const val TRAILER_STARTED_ACTIVITY = "trailer_started"

data class CachedLibraryResult<T>(
    val items: List<T>,
    val isFromCache: Boolean,
    val refreshErrorMessage: String? = null,
    val storageUnavailableMessage: String? = null
)

interface LibraryRepository {

    suspend fun watchList(limit: Int = 20): CachedLibraryResult<WatchListItemResponse>
    suspend fun viewingHistory(limit: Int = 20): CachedLibraryResult<ViewingHistoryResponse>

    suspend fun addToWatchlist(
        titleId: Long,
        mediaType: String,
    ): WatchListItemResponse

    suspend fun removeFromWatchlist(
        titleId: Long,
        mediaType: String,
    )

    suspend fun cacheContentMetadata(details: ContentDetailsResponse)
    suspend fun recordActivity(
        titleId: Long,
        mediaType: String,
        activityType: String,
    ): ViewingHistoryResponse
}