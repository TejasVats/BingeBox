package com.example.bingebox.domain.repository

import com.example.bingebox.core.library.ViewingHistoryResponse

const val DETAIL_OPENED_ACTIVITY = "detail_opened"
const val TRAILER_STARTED_ACTIVITY = "trailer_started"

interface LibraryRepository {
    suspend fun recordActivity(
        titleId: Long,
        mediaType: String,
        activityType: String,
    ): ViewingHistoryResponse
}