package com.example.bingebox.data.library

import com.example.bingebox.core.content.ContentDetailsResponse
import com.example.bingebox.core.library.ViewingHistoryResponse
import com.example.bingebox.core.library.WatchListItemResponse

interface LocalLibraryCache {
    val storageUnavailableMessage: String?

    suspend fun cachedWatchlist(limit: Int): List<WatchListItemResponse>

    suspend fun replaceWatchlist(items: List<WatchListItemResponse>)

    suspend fun removeWatchlistItem(
        titleId: Long,
        mediaType: String,
    )

    suspend fun cachedViewingHistory(limit: Int): List<ViewingHistoryResponse>

    suspend fun replaceViewingHistory(items: List<ViewingHistoryResponse>)

    suspend fun cacheContentMetadata(details: ContentDetailsResponse)
}

expect fun createRoomLocalLibraryCache(databaseName: String): LocalLibraryCache