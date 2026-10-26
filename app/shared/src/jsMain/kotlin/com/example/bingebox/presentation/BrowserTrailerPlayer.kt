package com.example.bingebox.presentation

import com.example.bingebox.domain.model.TrailerPlaybackResult
import com.example.bingebox.domain.model.TrailerPlayerRequest
import com.example.bingebox.presentation.screens.TrailerPlayer
import kotlinx.browser.document
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLVideoElement

class BrowserTrailerPlayer(
    override val modeName: String,
) : TrailerPlayer {
    override val supportsInLinePlayback: Boolean
        get() = true

    override suspend fun openTrailer(request: TrailerPlayerRequest): TrailerPlaybackResult {
        BrowserTrailerOverlay.show(request.title, request.source.url)
        return TrailerPlaybackResult("Opening trailer for ${request.title}.", isSupported = true)
    }
}

private object BrowserTrailerOverlay {
    private const val OVERLAY_ID: String = "nstream-trailer-overlay"

    fun show(
        title: String,
        url: String,
    ) {
        removeExisting()
        val body = document.body ?: return
        val overlay =
            (document.createElement("div") as HTMLDivElement).apply {
                id = OVERLAY_ID
                setAttribute(
                    "style",
                    "position:fixed;inset:0;z-index:9999;background:rgba(0,0,0,0.92);display:flex;align-items:center;justify-content:center;padding:24px;box-sizing:border-box;",
                )
            }
        val closeButton =
            (document.createElement("button") as HTMLButtonElement).apply {
                textContent = "Close"
                setAttribute(
                    "style",
                    "position:absolute;top:16px;right:16px;padding:10px 14px;border:none;border-radius:6px;background:#ffffff;color:#111111;font-size:14px;cursor:pointer;",
                )
                onclick = {
                    removeExisting()
                    null
                }
            }
        val video =
            (document.createElement("video") as HTMLVideoElement).apply {
                src = url
                controls = true
                autoplay = true
                setAttribute(
                    "style",
                    "max-width:min(1100px,100%);max-height:calc(100vh - 96px);width:100%;height:auto;background:#000;border-radius:8px;",
                )
                setAttribute("title", title)
            }
        overlay.appendChild(closeButton)
        overlay.appendChild(video)
        body.appendChild(overlay)
    }

    private fun removeExisting() {
        document.getElementById(OVERLAY_ID)?.remove()
    }
}