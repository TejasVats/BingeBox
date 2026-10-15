package com.example.bingebox.presentation.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.bingebox.core.library.WatchListItemResponse
import com.example.bingebox.data.auth.remote.BingeBoxApiException
import com.example.bingebox.domain.repository.LibraryRepository

data class WatchListUiState(
    val items: List<WatchListItemResponse> = emptyList(),
    val isLoading: Boolean = false,
    val isFromCache: Boolean = false,
    val message: String? = null,
    val errorMessage: String? = null
) {
    val isEmpty: Boolean = !isLoading && errorMessage == null && items.isEmpty()
}

class WatchListViewModel(
    private val repository: LibraryRepository
) : ViewModel() {
    var uiState by mutableStateOf(WatchListUiState(isLoading = true))
        private set

    suspend fun loadWatchList() {
        uiState = uiState.copy(isLoading = true, errorMessage = null, message = null)
        val result = repository.watchList()

        uiState =
            WatchListUiState(
                items = result.items,
                isFromCache = result.isFromCache,
                message = result.refreshErrorMessage ?: result.storageUnavailableMessage,
                errorMessage = if (result.items.isEmpty()) result.refreshErrorMessage else null,
            )
    }

    suspend fun addItem(
        titleId: Long,
        mediaType: String,
    ) {
        if (titleId <= 0 || mediaType.isBlank()) return
        uiState = uiState.copy(isLoading = true, errorMessage = null, message = null)
        try {
            repository.addToWatchlist(titleId, mediaType)
            loadWatchList()
        } catch (exception: BingeBoxApiException) {
            uiState = uiState.copy(isLoading = false, errorMessage = exception.message)
        } catch (_: Exception) {
            uiState = uiState.copy(
                isLoading = false,
                errorMessage = "Watchlist update failed. Please try again."
            )
        }
    }

    suspend fun removeItem(item: WatchListItemResponse) {
        uiState = uiState.copy(isLoading = true, errorMessage = null, message = null)
        try {
            repository.removeFromWatchlist(item.titleId, item.mediaType)
            loadWatchList()
        } catch (exception: BingeBoxApiException) {
            uiState = uiState.copy(isLoading = false, errorMessage = exception.message)
        } catch (_: Exception) {
            uiState = uiState.copy(
                isLoading = false,
                errorMessage = "Watchlist update failed. Please try again."
            )
        }
    }
}