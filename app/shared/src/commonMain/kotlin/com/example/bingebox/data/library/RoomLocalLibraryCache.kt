package com.example.bingebox.data.library

import com.example.bingebox.core.content.ContentDetailsResponse
import com.example.bingebox.core.library.ViewingHistoryResponse
import com.example.bingebox.core.library.WatchListItemResponse
import com.example.bingebox.data.library.dao.LocalLibraryDao
import com.example.bingebox.data.library.database.LocalLibraryDatabase
import com.example.bingebox.data.model.CachedContentMetadataEntity
import com.example.bingebox.data.model.CachedViewingHistoryEntity
import com.example.bingebox.data.model.CachedWatchListItemEntity

class RoomLocalLibraryCache(
    database: LocalLibraryDatabase,
) : LocalLibraryCache {
    private val dao: LocalLibraryDao = database.libraryDao()

    override val storageUnavailableMessage: String? = null

    override suspend fun cachedWatchlist(limit: Int): List<WatchListItemResponse> =
        dao.watchlist(limit).map { WatchListItemResponse(it.titleId, it.mediaType, it.addedAt) }

    override suspend fun replaceWatchlist(items: List<WatchListItemResponse>) {
        dao.clearWatchlist()
        dao.replaceWatchlistItems(items.map {
            CachedWatchListItemEntity(
                it.titleId,
                it.mediaType,
                it.addedAt
            )
        })
    }

    override suspend fun removeWatchlistItem(
        titleId: Long,
        mediaType: String,
    ) {
        dao.removeWatchlistItem(titleId, mediaType)
    }

    override suspend fun cachedViewingHistory(limit: Int): List<ViewingHistoryResponse> =
        dao.viewingHistory(limit).map {
            ViewingHistoryResponse(it.titleId, it.mediaType, it.activityType, it.occurredAt)
        }

    override suspend fun replaceViewingHistory(items: List<ViewingHistoryResponse>) {
        dao.clearViewingHistory()
        dao.replaceViewingHistory(
            items.map {
                CachedViewingHistoryEntity(
                    titleId = it.titleId,
                    mediaType = it.mediaType,
                    activityType = it.activityType,
                    occurredAt = it.occurredAt,
                )
            },
        )
    }

    override suspend fun cacheContentMetadata(details: ContentDetailsResponse) {
        dao.upsertContentMetadata(
            CachedContentMetadataEntity(
                titleId = details.id,
                title = details.title,
                mediaType = details.type,
                year = details.year,
                overview = details.overview,
                trailerUrl = details.trailerUrl,
                cachedAt = "refreshed",
            ),
        )
    }
}