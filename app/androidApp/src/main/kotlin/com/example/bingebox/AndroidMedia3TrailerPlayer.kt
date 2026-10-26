package com.example.bingebox

import android.content.Context
import android.content.Intent
import com.example.bingebox.domain.model.TrailerPlaybackResult
import com.example.bingebox.domain.model.TrailerPlayerRequest
import com.example.bingebox.presentation.screens.TrailerPlayer

class AndroidMedia3TrailerPlayer(
    private val context: Context,
) : TrailerPlayer {
    override val modeName: String = "Android Media3 player"
    override val supportsInLinePlayback: Boolean = true

    override suspend fun openTrailer(request: TrailerPlayerRequest): TrailerPlaybackResult {
        context.startActivity(
            Intent(context, AndroidTrailerPlayerActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                putExtra(AndroidTrailerPlayerActivity.EXTRA_TITLE, request.title)
                putExtra(AndroidTrailerPlayerActivity.EXTRA_URL, request.source.url)
            },
        )
        return TrailerPlaybackResult("Opening trailer for ${request.title}.", isSupported = true)
    }
}