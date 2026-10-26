package com.example.bingebox.presentation.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.bingebox.core.content.ContentDetailsResponse
import com.example.bingebox.core.content.StreamingSourceResponse
import com.example.bingebox.core.content.TrailerSourceType
import com.example.bingebox.data.auth.remote.BingeBoxApiException
import com.example.bingebox.domain.model.TrailerPlayerRequest
import com.example.bingebox.domain.repository.ContentRepository
import com.example.bingebox.domain.repository.DETAIL_OPENED_ACTIVITY
import com.example.bingebox.domain.repository.LibraryRepository
import com.example.bingebox.domain.repository.TRAILER_STARTED_ACTIVITY
import com.example.bingebox.presentation.screens.TrailerPlayer

data class ContentDetailUiState(
    val titleId: Long? = null,
    val details: ContentDetailsResponse? = null,
    val sources: List<StreamingSourceResponse> = emptyList(),
    val isLoading: Boolean = false,
    val isRecordingTrailer: Boolean = false,
    val trailerMessage: String? = null,
    val errorMessage: String? = null
) {
    val hasTrailerMetadata: Boolean = details?.trailerSource?.url?.isNotBlank() == true
    val canPlayTrailer: Boolean = details?.trailerSource?.sourceType == TrailerSourceType.DIRECT
}

class ContentDetailViewModel(
    private val contentRepository: ContentRepository,
    private val libraryRepository: LibraryRepository,
    private val trailerPlayer: TrailerPlayer
) {
    var uiState by mutableStateOf(ContentDetailUiState())
        private set

    suspend fun openTitle(titleId: Long) {
        if (titleId <= 0 || uiState.titleId == titleId && uiState.details != null) return
        uiState = ContentDetailUiState(titleId = titleId, isLoading = true)
        try {
            val details = contentRepository.details(titleId)
            val sources = contentRepository.sources(titleId)
            uiState =
                ContentDetailUiState(
                    titleId = titleId,
                    details = details,
                    sources = sources
                )
            runCatching { libraryRepository.cacheContentMetadata(details) }
            recordActivity(details, DETAIL_OPENED_ACTIVITY)
        } catch (exception: BingeBoxApiException) {
            uiState = ContentDetailUiState(titleId = titleId, errorMessage = exception.message)
        } catch (_: Exception) {
            uiState =
                ContentDetailUiState(titleId = titleId, errorMessage = "Details Not Available")
        }
    }

    suspend fun startTrailer() {
        val details = uiState.details ?: return
        val trailerSource = details.trailerSource
        if (trailerSource == null || !uiState.hasTrailerMetadata) {
            uiState = uiState.copy(trailerMessage = "Trailer is unavailable for this title.")
            return
        }
        if (trailerSource.sourceType != TrailerSourceType.DIRECT) {
            uiState =
                uiState.copy(
                    trailerMessage = "A licensed direct trailer has not been configured for this title"
                )
            return
        }

        uiState = uiState.copy(isRecordingTrailer = true, trailerMessage = null)
        recordActivity(details, TRAILER_STARTED_ACTIVITY)
        val result = trailerPlayer.openTrailer(
            TrailerPlayerRequest(
                titleId = details.id,
                title = details.title,
                source = trailerSource
            )
        )
        uiState = uiState.copy(isRecordingTrailer = false, trailerMessage = result.message)

        uiState =
            uiState.copy(isRecordingTrailer = false, trailerMessage = "Trailer activity recorded.")
    }

    private suspend fun recordActivity(
        details: ContentDetailsResponse,
        activity: String
    ) {
        runCatching {
            libraryRepository.recordActivity(
                titleId = details.id,
                mediaType = details.type,
                activityType = activity
            )
        }
    }
}