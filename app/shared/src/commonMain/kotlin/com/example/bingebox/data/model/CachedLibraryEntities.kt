package com.example.bingebox.data.model

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "watchlist_items")
data class CachedWatchListItemEntity(
    @PrimaryKey val titleId: Long,
    val mediaType: String,
    val addedAt: String
)

@Entity(tableName = "viewing_history_items")
data class CachedViewingHistoryEntity(
    @PrimaryKey(autoGenerate = true) val localId: Long = 0,
    val titleId: Long,
    val mediaType: String,
    val activityType: String,
    val occurredAt: String
)

@Entity(tableName = "content_metadata")
data class CachedContentMetadataEntity(
    @PrimaryKey val titleId: Long,
    val title: String,
    val mediaType: String,
    val year: Int?,
    val overview: String?,
    val trailerUrl: String?,
    val cachedAt: String,
)