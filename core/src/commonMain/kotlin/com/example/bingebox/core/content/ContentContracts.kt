package com.example.bingebox.core.content

import kotlinx.serialization.Serializable

@Serializable
enum class TrailerSourceType {
    DIRECT,
    EXTERNAL,
}

@Serializable
enum class TrailerStreamFormat {
    MP4,
    HLS,
    UNKNOWN,
}

@Serializable
data class TrailerSourceResponse(
    val url: String,
    val sourceType: TrailerSourceType,
    val format: TrailerStreamFormat = TrailerStreamFormat.UNKNOWN,
)

@Serializable
data class ContentPageResponse(
    val items: List<ContentSummaryResponse>,
    val page: Int,
    val totalPages: Int,
    val totalResults: Int
)

@Serializable
data class ContentSummaryResponse(
    val id: Long,
    val title: String,
    val type: String,
    val year: Int? = null,
    val imdbId: String? = null
)

@Serializable
data class ContentDetailsResponse(
    val id: Long,
    val title: String,
    val type: String,
    val overview: String? = null,
    val runtimeMinutes: Int? = null,
    val year: Int? = null,
    val releaseDate: String? = null,
    val trailerSource: TrailerSourceResponse? = null,
    val similarTitleIds: List<Long> = emptyList()
)

@Serializable
data class StreamingSourceResponse(
    val id: Int,
    val name: String,
    val type: String,
    val webUrl: String? = null
)