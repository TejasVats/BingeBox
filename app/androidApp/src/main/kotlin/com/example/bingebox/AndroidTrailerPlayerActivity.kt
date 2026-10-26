package com.example.bingebox

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView

class AndroidTrailerPlayerActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val title = intent.getStringExtra(EXTRA_TITLE).orEmpty()
        val url = intent.getStringExtra(EXTRA_URL).orEmpty()
        setContent {
            MaterialTheme {
                AndroidTrailerPlayerScreen(
                    title = title,
                    url = url,
                )
            }
        }
    }

    companion object {
        const val EXTRA_TITLE: String = "trailer_title"
        const val EXTRA_URL: String = "trailer_url"
    }
}

@Composable
private fun AndroidTrailerPlayerScreen(
    title: String,
    url: String,
) {
    val context = LocalContext.current
    val player =
        remember(url) {
            ExoPlayer.Builder(context)
                .build()
                .apply {
                    setMediaItem(MediaItem.fromUri(url))
                    prepare()
                    playWhenReady = true
                }
        }
    DisposableEffect(player) {
        onDispose {
            player.release()
        }
    }
    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { viewContext ->
            PlayerView(viewContext).apply {
                this.player = player
                useController = true
                contentDescription = title
            }
        },
        update = { playerView ->
            playerView.player = player
        },
    )
}