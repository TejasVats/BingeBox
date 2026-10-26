package com.example.bingebox.domain.model

import com.example.bingebox.core.content.TrailerSourceResponse

data class TrailerPlayerRequest(
    val titleId: Long,
    val title: String,
    val source: TrailerSourceResponse
)

data class TrailerPlaybackResult(
    val message: String,
    val isSupported: Boolean
)