package com.example.bingebox.watchmode

import com.example.bingebox.core.content.ContentDetailsResponse
import com.example.bingebox.core.content.ContentPageResponse
import com.example.bingebox.core.content.ContentSummaryResponse
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class WatchModeListDto(
    val titles: List<WatchModeTitleDTO> = emptyList(),
    val page: Int = 1,
    @SerialName("total_pages")
    val totalPages: Int = 1,
    @SerialName("total_results")
    val totalResults: Int = 0
)

@Serializable
internal data class WatchModeSearchDTO(
    @SerialName("title_results")
    val titleResults: List<WatchModeTitleDTO> = emptyList()
)

@Serializable
internal data class WatchModeTitleDTO(
    val id: Long,
    val title: String? = null,
    val name: String? = null,
    val type: String,
    val year: Int? = null,
    @SerialName("imdb_id")
    val imdbId: String? = null
)

@Serializable
internal data class WatchModeDetailsDTO(
    val id: Long,
    val title: String,
    val type: String,
    @SerialName("plot_overview")
    val overview: String? = null,
    @SerialName("runtime_minutes")
    val runtimeMinutes: Int? = null,
    val year: Int? = null,
    @SerialName("release_date")
    val releaseDate: String? = null,
    val trailer: String? = null,
    @SerialName("similar_titles")
    val similarTitles: List<Long>? = null
)

@Serializable
internal data class WatchModeSourceDTO(
    @SerialName("source_id")
    val id: Int,
    val name: String,
    val type: String,
    @SerialName("web_url")
    val webUrl: String? = null
)

internal fun WatchModeTitleDTO.toSummary() = ContentSummaryResponse(
    id, title ?: name ?: "Untitled", type, year, imdbId
)

internal fun WatchModeListDto.toPage() = ContentPageResponse(
    items = titles.map { it.toSummary() }, page, totalPages, totalResults
)

internal fun WatchModeDetailsDTO.toDetails() = ContentDetailsResponse(
    id,
    title,
    type,
    overview,
    runtimeMinutes,
    year,
    releaseDate,
    trailer,
    similarTitles ?: emptyList()
)