package com.example.bingebox

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import org.jetbrains.compose.resources.painterResource

import bingebox.app.shared.generated.resources.Res
import bingebox.app.shared.generated.resources.compose_multiplatform
import com.example.bingebox.presentation.AuthScreenContent
import com.example.bingebox.presentation.AuthViewModel
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
@Preview
fun App() {
    val authViewModel = koinInject<AuthViewModel>()
    val authState = authViewModel.uiState
    val scope = rememberCoroutineScope()

    LaunchedEffect(key1 = Unit) {
        authViewModel.restoreSession()
    }

    MaterialTheme {
        if (authState.account == null) {
            AuthScreenContent(authViewModel)
        } else {
            Column(
                modifier = Modifier
                    .background(
                        color = MaterialTheme.colorScheme.primaryContainer
                    )
                    .safeContentPadding()
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Signed in as ${authState.account.email}")
                Button(onClick = { scope.launch { authViewModel.logout() } }) {
                    Text("Sign out")
                }
            }
        }
    }
}