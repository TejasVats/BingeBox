package com.example.bingebox.domain.repository

import com.example.bingebox.core.content.ContentDetailsResponse
import com.example.bingebox.core.content.ContentPageResponse
import com.example.bingebox.core.content.ContentSummaryResponse
import com.example.bingebox.core.content.StreamingSourceResponse

interface ContentRepository{
    suspend fun listTitles(
        types: String = "movies, tv_series",
        region: String? = null,
        page: Int = 1,
    ): ContentPageResponse

    suspend fun search(
        query: String,
        types: String? = null
    ): List<ContentSummaryResponse>

    suspend fun details(titleId: Long): ContentDetailsResponse

    suspend fun sources(
        titleId: Long,
        region: String? = null,
    ): List<StreamingSourceResponse>
}