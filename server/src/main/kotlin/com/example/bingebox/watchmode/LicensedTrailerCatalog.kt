package com.example.bingebox.watchmode

import com.example.bingebox.config.ServerConfig
import com.example.bingebox.core.content.TrailerSourceResponse
import com.example.bingebox.core.content.TrailerSourceType
import com.example.bingebox.core.content.TrailerStreamFormat
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.net.URI

class LicensedTrailerCatalog(
    private val trailerByTitleId: Map<Long, TrailerSourceResponse>
) {

    fun directSourceFor(titleId: Long): TrailerSourceResponse? = trailerByTitleId[titleId]

    companion object {
        private val json = Json { ignoreUnknownKeys = true }

        // BINGEBOX_LICENSED_TRAILERS_JSON=[{"titleId":1396,"url":"https://cdn.pixabay.com/video/2026/07/23/365737_large.mp4","format":"MP4"}]

        fun fromConfig(config: ServerConfig): LicensedTrailerCatalog {
            val rawJson = config.licensedTrailersJson?.trim().orEmpty()
            if (rawJson.isBlank()) {
                return LicensedTrailerCatalog(emptyMap())
            }
            val entries = json.decodeFromString<List<LicensedTrailerEntry>>(rawJson)
            val duplicateTitleIds = entries.groupBy { it.titleId }.filterValues { it.size > 1 }.keys
            require(duplicateTitleIds.isEmpty()) {
                "BINGEBOX_LICENSED_TRAILERS_JSON contains duplicate title IDs: ${
                    duplicateTitleIds.sorted().joinToString()
                }."
            }
            entries.forEach { entry ->
                require(entry.titleId > 0) {
                    "BINGEBOX_LICENSED_TRAILERS_JSON contains an invalid titleId: ${entry.titleId}."
                }
                require(entry.url.isNotBlank()) {
                    "BINGEBOX_LICENSED_TRAILERS_JSON contains an empty trailer URL for titleId ${entry.titleId}."
                }
                require(isAllowedTrailerUrl(entry.url)) {
                    "BINGEBOX_LICENSED_TRAILERS_JSON must use HTTPS URLs, or local HTTP URLs for development, for titleId ${entry.titleId}."
                }
            }
            return LicensedTrailerCatalog(
                trailerByTitleId =
                    entries.associate { entry ->
                        entry.titleId to
                                TrailerSourceResponse(
                                    url = entry.url,
                                    sourceType = TrailerSourceType.DIRECT,
                                    format = entry.format,
                                )
                    },
            )
        }

        private fun isAllowedTrailerUrl(url: String): Boolean {
            val url = runCatching { URI(url) }.getOrNull() ?: return false
            val scheme = url.scheme?.lowercase() ?: return false
            if (scheme == "https") {
                return true
            }
            if (scheme != "http") {
                return false
            }
            val host = url.host?.lowercase() ?: return false
            return host == "localhost" || host == "127.0.0.1"
        }
    }
}

@Serializable
private data class LicensedTrailerEntry(
    val titleId: Long,
    val url: String,
    val format: TrailerStreamFormat = TrailerStreamFormat.UNKNOWN,
)