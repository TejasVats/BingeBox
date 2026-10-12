package com.example.bingebox.data.repository

import com.example.bingebox.core.content.ContentDetailsResponse
import com.example.bingebox.core.content.ContentPageResponse
import com.example.bingebox.core.content.ContentSummaryResponse
import com.example.bingebox.core.content.StreamingSourceResponse
import com.example.bingebox.data.auth.remote.BingeBoxApiClient
import com.example.bingebox.domain.repository.ContentRepository

class ContentRepositoryImp(
    private val apiClient: BingeBoxApiClient
) : ContentRepository {
    override suspend fun listTitles(
        types: String,
        region: String?,
        page: Int
    ): ContentPageResponse = apiClient.listContentTitles(types, region, page)

    override suspend fun search(
        query: String,
        types: String?
    ): List<ContentSummaryResponse> = apiClient.searchContent(query, types)

    override suspend fun details(titleId: Long): ContentDetailsResponse = apiClient.contentDetails(titleId)

    override suspend fun sources(
        titleId: Long,
        region: String?
    ): List<StreamingSourceResponse> = apiClient.contentSources(titleId, region)
}