package com.example.bingebox.presentation.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModel
import com.example.bingebox.presentation.navigation.BingeBoxRoute

data class NavigationUiState(
    val backStack: List<BingeBoxRoute> = listOf(BingeBoxRoute.Home),
) {
    val currentRoute: BingeBoxRoute = backStack.last()
    val canGoBack: Boolean = backStack.size > 1
}

class NavigationViewModel : ViewModel() {
    var uiState by mutableStateOf(NavigationUiState())
        private set

    fun showHome() {
        uiState = NavigationUiState(listOf(BingeBoxRoute.Home))
    }

    fun showSearch() {
        pushOrReplaceTop(BingeBoxRoute.Search)
    }

    fun showDetails(titleId: Long) {
        if (titleId <= 0) return
        pushOrReplaceTop(BingeBoxRoute.Detail(titleId))
    }

    fun goBack() {
        if (!uiState.canGoBack) return
        uiState = uiState.copy(backStack = uiState.backStack.dropLast(1))
    }

    private fun pushOrReplaceTop(route: BingeBoxRoute) {
        val currentStack = uiState.backStack
        uiState =
            when {
                currentStack.last() == route -> uiState
                route == BingeBoxRoute.Home -> NavigationUiState(listOf(BingeBoxRoute.Home))
                currentStack.last()::class == route::class -> uiState.copy(backStack = currentStack.dropLast(1) + route)
                else -> uiState.copy(backStack = currentStack + route)
            }
    }
}