package com.example.bingebox.watchmode

import com.example.bingebox.core.content.ContentDetailsResponse
import com.example.bingebox.core.content.ContentPageResponse
import com.example.bingebox.core.content.ContentSummaryResponse
import com.example.bingebox.core.content.StreamingSourceResponse

class ContentCatalogService(
    private val watchmodeClient: WatchModeClient,
    private val licensedTrailerCatalog: LicensedTrailerCatalog,
) {
    suspend fun listTitles(
        types: String,
        region: String?,
        page: Int,
    ): WatchModeResults<ContentPageResponse> = watchmodeClient.listTitles(types, region, page)

    suspend fun search(
        query: String,
        types: String?,
    ): WatchModeResults<List<ContentSummaryResponse>> = watchmodeClient.search(query, types)

    suspend fun details(id: Long): WatchModeResults<ContentDetailsResponse> =
        when (val result = watchmodeClient.details(id)) {
            is WatchModeResults.Success ->
                WatchModeResults.Success(
                    result.value.copy(
                        trailerSource = licensedTrailerCatalog.directSourceFor(id) ?: result.value.trailerSource,
                    ),
                )

            WatchModeResults.NotFound -> WatchModeResults.NotFound
            WatchModeResults.RateLimited -> WatchModeResults.RateLimited
            WatchModeResults.Unavailable -> WatchModeResults.Unavailable
        }

    suspend fun sources(
        id: Long,
        region: String?,
    ): WatchModeResults<List<StreamingSourceResponse>> = watchmodeClient.sources(id, region)
}