package com.example.bingebox.presentation.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.bingebox.core.content.ContentSummaryResponse
import com.example.bingebox.data.auth.remote.BingeBoxApiException
import com.example.bingebox.domain.repository.ContentRepository

data class SearchUiState(
    val query: String = "",
    val results: List<ContentSummaryResponse> = emptyList(),
    val hasSearched: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
) {
    val isSearchResEmpty: Boolean =
        hasSearched && !isLoading && errorMessage == null && results.isEmpty()
}

class SearchViewModel(
    private val repository: ContentRepository
) : ViewModel() {
    var uiState by mutableStateOf(SearchUiState())
        private set

    fun updateQuery(value: String) {
        uiState = uiState.copy(query = value, errorMessage = null)
    }

    suspend fun search(){
        val query = uiState.query.trim()
        if (query.length < 2) {
            uiState = uiState.copy(errorMessage = "Enter at least two characters.")
            return
        }
        uiState = uiState.copy(isLoading = true, errorMessage = null, hasSearched = true)
        uiState = try {
            uiState.copy(results = repository.search(query), isLoading = false)
        } catch (exception: BingeBoxApiException) {
            uiState.copy(isLoading = false, errorMessage = exception.message)
        } catch (_: Exception) {
            uiState.copy(isLoading = false, errorMessage = "Search is unavailable. Please try again.")
        }
    }
}