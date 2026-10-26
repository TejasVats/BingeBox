package com.example.bingebox.presentation.screens

import com.example.bingebox.domain.model.TrailerPlaybackResult
import com.example.bingebox.domain.model.TrailerPlayerRequest

interface TrailerPlayer {
    val modeName: String
    val supportsInLinePlayback: Boolean
    suspend fun openTrailer(request: TrailerPlayerRequest): TrailerPlaybackResult
}

class FallBackTrailerPlayer(
    override val modeName: String,
    override val supportsInLinePlayback: Boolean,
    private val fallbackMessage: String
) : TrailerPlayer {
    override suspend fun openTrailer(request: TrailerPlayerRequest): TrailerPlaybackResult {
        return TrailerPlaybackResult(fallbackMessage, isSupported = supportsInLinePlayback)
    }

}
