package com.example.bingebox.data.repository

import com.example.bingebox.core.library.ActivityRequest
import com.example.bingebox.core.library.ViewingHistoryResponse
import com.example.bingebox.data.auth.remote.BingeBoxApiClient
import com.example.bingebox.domain.repository.LibraryRepository


class LibraryRepositoryImp(
    private val apiClient: BingeBoxApiClient
) : LibraryRepository {
    override suspend fun recordActivity(
        titleId: Long,
        mediaType: String,
        activityType: String
    ): ViewingHistoryResponse = apiClient.recordViewingActivity(
        ActivityRequest(titleId, mediaType, activityType)
    )
}