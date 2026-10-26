package com.example.bingebox

import com.example.bingebox.domain.model.TrailerPlaybackResult
import com.example.bingebox.domain.model.TrailerPlayerRequest
import com.example.bingebox.presentation.screens.TrailerPlayer
import javafx.embed.swing.JFXPanel
import javafx.scene.Scene
import javafx.scene.layout.StackPane
import java.awt.BorderLayout
import javax.swing.JFrame
import javafx.scene.media.Media
import javafx.scene.media.MediaPlayer
import javafx.scene.media.MediaView
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.swing.SwingUtilities
import javafx.application.Platform

class DesktopJavaFxTrailerPlayer : TrailerPlayer {
    override val modeName: String = "Desktop JavaFX player"
    override val supportsInLinePlayback: Boolean
        get() = true

    override suspend fun openTrailer(request: TrailerPlayerRequest): TrailerPlaybackResult {
        SwingUtilities.invokeLater {
            DesktopTrailerWindow.show(request.title, request.source.url)
        }
        return TrailerPlaybackResult("Opening trailer for ${request.title}.", isSupported = true)
    }
}

private object DesktopTrailerWindow {
    fun show(
        title: String,
        url: String,
    ) {
        Platform.setImplicitExit(false)
        val panel = JFXPanel()
        val frame =
            JFrame(title).apply {
                defaultCloseOperation = JFrame.DISPOSE_ON_CLOSE
                layout = BorderLayout()
                add(panel, BorderLayout.CENTER)
                setSize(960, 540)
                setLocationRelativeTo(null)
                isVisible = true
            }
        Platform.runLater {
            val media = Media(url)
            val player =
                MediaPlayer(media).apply {
                    isAutoPlay = true
                }
            val mediaView =
                MediaView(player).apply {
                    isPreserveRatio = true
                    fitWidth = 960.0
                    fitHeight = 540.0
                }
            panel.scene =
                Scene(
                    StackPane(mediaView).apply {
                        style = "-fx-background-color: black;"
                    },
                )
            frame.addWindowListener(
                object : WindowAdapter() {
                    override fun windowClosed(event: WindowEvent?) {
                        Platform.runLater {
                            player.dispose()
                        }
                    }
                },
            )
        }
    }
}