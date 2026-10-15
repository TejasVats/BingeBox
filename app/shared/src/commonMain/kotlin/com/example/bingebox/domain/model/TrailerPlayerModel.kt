package com.example.bingebox.domain.model

data class TrailerPlayerRequest(
    val titleId: Long,
    val title: String,
    val trailerUrl: String
)

data class TrailerPlaybackResult(
    val message: String,
    val isSupported: Boolean
)