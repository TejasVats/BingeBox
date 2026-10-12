package com.example.bingebox.presentation.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.bingebox.core.content.ContentSummaryResponse
import com.example.bingebox.data.auth.remote.BingeBoxApiException
import com.example.bingebox.domain.repository.ContentRepository

data class HomeContentRow(
    val title: String,
    val items: List<ContentSummaryResponse>
)

data class HomeUiState(
    val rows: List<HomeContentRow> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
) {
    val isEmpty: Boolean = !isLoading && errorMessage == null && rows.all { it.items.isEmpty() }
}

class HomeViewModel(
    private val repository: ContentRepository,
) : ViewModel() {
    var uiState by mutableStateOf(HomeUiState(isLoading = true))
        private set

    suspend fun loadHomeRows() {
        runRequest {
            val movies = repository.listTitles(types = "movie", page = 1).items
            val series = repository.listTitles(types = "tv_series", page = 1).items
            uiState =
                HomeUiState(
                    rows =
                        listOf(
                            HomeContentRow("Movies", movies),
                            HomeContentRow("Series", series),
                        ),
                )
        }
    }

    private suspend fun runRequest(request: suspend () -> Unit) {
        uiState = uiState.copy(isLoading = true, errorMessage = null)
        try {
            request()
        } catch (exception: BingeBoxApiException) {
            uiState = HomeUiState(errorMessage = exception.message)
        } catch (_: Exception) {
            uiState = HomeUiState(errorMessage = "Content is unavailable. Please try again.")
        }
    }
}