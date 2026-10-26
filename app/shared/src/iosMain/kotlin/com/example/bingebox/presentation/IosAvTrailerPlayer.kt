package com.example.bingebox.presentation

import com.example.bingebox.domain.model.TrailerPlaybackResult
import com.example.bingebox.domain.model.TrailerPlayerRequest
import com.example.bingebox.presentation.screens.TrailerPlayer
import platform.AVFoundation.AVPlayer
import platform.AVKit.AVPlayerViewController
import platform.Foundation.NSURL
import platform.UIKit.UIViewController


object IosTrailerPlayerHost {
    var rootViewController: UIViewController? = null
}

class IosAvTrailerPlayer : TrailerPlayer {
    override val modeName: String = "iOS AVPlayer"
    override val supportsInLinePlayback: Boolean = true
    override suspend fun openTrailer(request: TrailerPlayerRequest): TrailerPlaybackResult {
        val rootController = IosTrailerPlayerHost.rootViewController
            ?: return TrailerPlaybackResult(
                "iOS player is unavailable until app startup finishes.",
                isSupported = false
            )
        val url = NSURL.URLWithString(request.source.url)
            ?: return TrailerPlaybackResult("The trailer URL is invalid.", isSupported = false)
        val player = AVPlayer.playerWithURL(url)
        val controller =
            AVPlayerViewController().apply {
                this.player = player
            }
        rootController.topPresentedController().presentViewController(
            viewControllerToPresent = controller,
            animated = true,
            completion = null,
        )
        return TrailerPlaybackResult("Opening trailer for ${request.title}.", isSupported = true)
    }


}

private fun UIViewController.topPresentedController(): UIViewController {
    var current = this
    while (current.presentedViewController != null) {
        current = current.presentedViewController!!
    }
    return current
}