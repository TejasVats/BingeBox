package com.example.bingebox

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.tooling.preview.Preview

import com.example.bingebox.presentation.auth.AuthScreenContent
import com.example.bingebox.presentation.auth.AuthViewModel
import com.example.bingebox.presentation.screens.MainScreen
import org.koin.compose.koinInject

@Composable
@Preview
fun App() {
    val authViewModel = koinInject<AuthViewModel>()
    val authState = authViewModel.uiState
    LaunchedEffect(key1 = Unit) {
        authViewModel.restoreSession()
    }

    MaterialTheme {
        if (authState.account == null) {
            AuthScreenContent(authViewModel)
        } else {
            MainScreen(authViewModel)
        }
    }
}