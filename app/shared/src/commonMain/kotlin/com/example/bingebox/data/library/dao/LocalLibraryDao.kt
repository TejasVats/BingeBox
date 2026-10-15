package com.example.bingebox.data.library.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import com.example.bingebox.data.model.CachedContentMetadataEntity
import com.example.bingebox.data.model.CachedViewingHistoryEntity
import com.example.bingebox.data.model.CachedWatchListItemEntity

@Dao
interface LocalLibraryDao {
    @Query("SELECT * FROM watchlist_items ORDER BY addedAt DESC LIMIT :limit")
    suspend fun watchlist(limit: Int): List<CachedWatchListItemEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun replaceWatchlistItems(items: List<CachedWatchListItemEntity>)

    @Query("DELETE FROM watchlist_items")
    suspend fun clearWatchlist()

    @Query("DELETE FROM watchlist_items WHERE titleId = :titleId AND mediaType = :mediaType")
    suspend fun removeWatchlistItem(
        titleId: Long,
        mediaType: String,
    )

    @Query("SELECT * FROM viewing_history_items ORDER BY occurredAt DESC LIMIT :limit")
    suspend fun viewingHistory(limit: Int): List<CachedViewingHistoryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun replaceViewingHistory(items: List<CachedViewingHistoryEntity>)

    @Query("DELETE FROM viewing_history_items")
    suspend fun clearViewingHistory()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertContentMetadata(item: CachedContentMetadataEntity)
}