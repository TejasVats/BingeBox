package com.example.bingebox.presentation.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.bingebox.core.library.ViewingHistoryResponse
import com.example.bingebox.domain.repository.LibraryRepository

data class ViewingHistoryUiState(
    val items: List<ViewingHistoryResponse> = emptyList(),
    val isLoading: Boolean = false,
    val isFromCache: Boolean = false,
    val message: String? = null,
    val errorMessage: String? = null,
) {
    val isEmpty: Boolean = !isLoading && errorMessage == null && items.isEmpty()
}

class ViewingHistoryViewModel(
    private val repository: LibraryRepository
): ViewModel(){
    var uiState by mutableStateOf(ViewingHistoryUiState(isLoading = true))
        private set

    suspend fun loadViewingHistory(){
        uiState = uiState.copy(isLoading = true, errorMessage = null)
        val result = repository.viewingHistory()
        uiState = ViewingHistoryUiState(
            items = result.items,
            isFromCache = result.isFromCache,
            message = result.refreshErrorMessage?: result.storageUnavailableMessage,
            errorMessage = if(result.items.isEmpty()) result.refreshErrorMessage else null
        )
    }
}