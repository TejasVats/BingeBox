package com.example.bingebox.data.library

import com.example.bingebox.core.content.ContentDetailsResponse
import com.example.bingebox.core.library.ViewingHistoryResponse
import com.example.bingebox.core.library.WatchListItemResponse

class UnavailableLocalLibraryCache(
    override val storageUnavailableMessage: String,
) : LocalLibraryCache {
    override suspend fun cachedWatchlist(limit: Int): List<WatchListItemResponse> = emptyList()

    override suspend fun replaceWatchlist(items: List<WatchListItemResponse>) = Unit

    override suspend fun removeWatchlistItem(
        titleId: Long,
        mediaType: String,
    ) = Unit

    override suspend fun cachedViewingHistory(limit: Int): List<ViewingHistoryResponse> = emptyList()

    override suspend fun replaceViewingHistory(items: List<ViewingHistoryResponse>) = Unit

    override suspend fun cacheContentMetadata(details: ContentDetailsResponse) = Unit
}