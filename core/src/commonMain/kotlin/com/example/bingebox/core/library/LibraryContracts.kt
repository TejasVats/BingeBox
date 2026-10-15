package com.example.bingebox.core.library

import kotlinx.serialization.Serializable

@Serializable
data class WatchListRequest(
    val titleId: Long,
    val mediaType: String
)

@Serializable
data class ActivityRequest(
    val titleId: Long,
    val mediaType: String,
    val activityType: String
)

@Serializable
data class WatchListItemResponse(
    val titleId: Long,
    val mediaType: String,
    val addedAt: String
)

@Serializable
data class ViewingHistoryResponse(
    val titleId: Long,
    val mediaType: String,
    val activityType: String,
    val occurredAt: String
)

@Serializable
data class ApiDataResponse<T>(
    val data: T,
    val requestId: String? = null
)

@Serializable
data class PageMetadataResponse(
    val limit: Int,
    val offset: Long,
    val returned: Int,
    val nextOffset: Long? = null
)

@Serializable
data class ApiPageResponse<T>(
    val data: List<T>,
    val page: PageMetadataResponse,
    val requestedId: String? = null
)