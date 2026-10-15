package com.example.bingebox.data.repository

import com.example.bingebox.core.content.ContentDetailsResponse
import com.example.bingebox.core.library.ActivityRequest
import com.example.bingebox.core.library.ViewingHistoryResponse
import com.example.bingebox.core.library.WatchListItemResponse
import com.example.bingebox.core.library.WatchListRequest
import com.example.bingebox.data.auth.remote.BingeBoxApiClient
import com.example.bingebox.data.auth.remote.BingeBoxApiException
import com.example.bingebox.data.library.LocalLibraryCache
import com.example.bingebox.domain.repository.CachedLibraryResult
import com.example.bingebox.domain.repository.LibraryRepository


class LibraryRepositoryImp(
    private val apiClient: BingeBoxApiClient,
    private val localCache: LocalLibraryCache,
) : LibraryRepository {
    override suspend fun watchList(limit: Int): CachedLibraryResult<WatchListItemResponse> {
        val cachedItems = localCache.cachedWatchlist(limit)
        return try {
            val serverItems = apiClient.watchList(limit)
            localCache.replaceWatchlist(serverItems)
            CachedLibraryResult( // list<WatchListItemResponse>
                items = serverItems,
                isFromCache = false,
                storageUnavailableMessage = localCache.storageUnavailableMessage,
            )
        } catch (exception: BingeBoxApiException) {
            CachedLibraryResult(
                items = cachedItems,
                isFromCache = cachedItems.isNotEmpty(),
                refreshErrorMessage = exception.message,
                storageUnavailableMessage = localCache.storageUnavailableMessage,
            )
        } catch (_: Exception) {
            CachedLibraryResult(
                items = cachedItems,
                isFromCache = cachedItems.isNotEmpty(),
                refreshErrorMessage = "Library refresh failed. Showing cached data where available.",
                storageUnavailableMessage = localCache.storageUnavailableMessage,
            )
        }
    }

    override suspend fun viewingHistory(limit: Int): CachedLibraryResult<ViewingHistoryResponse> {
        val cachedItems = localCache.cachedViewingHistory(limit)
        return try {
            val serverItems = apiClient.viewingHistory(limit)
            localCache.replaceViewingHistory(serverItems)
            CachedLibraryResult(
                items = serverItems,
                isFromCache = false,
                storageUnavailableMessage = localCache.storageUnavailableMessage,
            )
        } catch (exception: BingeBoxApiException) {
            CachedLibraryResult(
                items = cachedItems,
                isFromCache = cachedItems.isNotEmpty(),
                refreshErrorMessage = exception.message,
                storageUnavailableMessage = localCache.storageUnavailableMessage,
            )
        } catch (_: Exception) {
            CachedLibraryResult(
                items = cachedItems,
                isFromCache = cachedItems.isNotEmpty(),
                refreshErrorMessage = "Activity refresh failed. Showing cached data where available.",
                storageUnavailableMessage = localCache.storageUnavailableMessage,
            )
        }
    }

    override suspend fun addToWatchlist(
        titleId: Long,
        mediaType: String,
    ) = apiClient.addWatchListItem(WatchListRequest(titleId, mediaType)).also {
        localCache.replaceWatchlist(apiClient.watchList())
    }

    override suspend fun removeFromWatchlist(
        titleId: Long,
        mediaType: String,
    ) {
        apiClient.removeWatchListItem(titleId, mediaType)
        localCache.removeWatchlistItem(titleId, mediaType)
    }

    override suspend fun cacheContentMetadata(details: ContentDetailsResponse) {
        localCache.cacheContentMetadata(details)
    }

    override suspend fun recordActivity(
        titleId: Long,
        mediaType: String,
        activityType: String
    ): ViewingHistoryResponse = apiClient.recordViewingActivity(
        ActivityRequest(titleId, mediaType, activityType)
    )
}