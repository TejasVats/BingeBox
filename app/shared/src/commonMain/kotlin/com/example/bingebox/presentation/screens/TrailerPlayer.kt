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
    override val supportsInLinePlayback: Boolean
): TrailerPlayer{
    override suspend fun openTrailer(request: TrailerPlayerRequest): TrailerPlaybackResult {
        val message =
            if (supportsInLinePlayback) {
                "Opening trailer for ${request.title}."
            } else {
                "Trailer playback uses the platform browser on this target."
            }
        return TrailerPlaybackResult(message, isSupported = true)
    }

}
