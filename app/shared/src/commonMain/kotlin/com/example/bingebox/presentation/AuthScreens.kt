package com.example.bingebox.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
fun AuthScreenContent(viewModel: AuthViewModel) {
    val state = viewModel.uiState
    val scope = rememberCoroutineScope()
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(titleFor(state.screen), style = MaterialTheme.typography.headlineMedium)
        state.message?.let { Text(it, color = MaterialTheme.colorScheme.primary) }
        state.errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        when (state.screen) {
            AuthScreen.Login, AuthScreen.SignUp, AuthScreen.ForgotPassword -> EmailField(
                state,
                viewModel
            )

            else -> Unit
        }
        when (state.screen) {
            AuthScreen.Login, AuthScreen.SignUp, AuthScreen.ResetPassword -> PasswordField(
                state,
                viewModel
            )

            else -> Unit
        }
        when (state.screen) {
            AuthScreen.VerifyEmail, AuthScreen.ResetPassword -> TokenField(state, viewModel)
            else -> Unit
        }
        Button(
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isLoading,
            onClick = { scope.launch { submit(state.screen, viewModel) } },
        ) { Text(if (state.isLoading) "Please wait…" else actionFor(state.screen)) }
        when (state.screen) {
            AuthScreen.Login -> {
                Button(onClick = { viewModel.show(AuthScreen.SignUp) }) { Text("Create account") }
                Button(onClick = { viewModel.show(AuthScreen.ForgotPassword) }) { Text("Forgot password") }
                Button(onClick = { viewModel.show(AuthScreen.VerifyEmail) }) { Text("Verify email") }
            }

            AuthScreen.SignUp, AuthScreen.ForgotPassword, AuthScreen.VerifyEmail, AuthScreen.ResetPassword ->
                Button(onClick = { viewModel.show(AuthScreen.Login) }) { Text("Back to sign in") }
        }
    }
}

@Composable
private fun EmailField(
    state: AuthUiState,
    viewModel: AuthViewModel,
) = OutlinedTextField(
    value = state.email,
    onValueChange = viewModel::updateEmail,
    modifier = Modifier.fillMaxWidth(),
    label = { Text("Email") },
    singleLine = true,
)

@Composable
private fun PasswordField(
    state: AuthUiState,
    viewModel: AuthViewModel,
) = OutlinedTextField(
    value = state.password,
    onValueChange = viewModel::updatePassword,
    modifier = Modifier.fillMaxWidth(),
    label = { Text("Password") },
    visualTransformation = PasswordVisualTransformation(),
    singleLine = true,
)

@Composable
private fun TokenField(
    state: AuthUiState,
    viewModel: AuthViewModel,
) = OutlinedTextField(
    value = state.token,
    onValueChange = viewModel::updateToken,
    modifier = Modifier.fillMaxWidth(),
    label = { Text("Email token") },
    singleLine = true,
)

private suspend fun submit(
    screen: AuthScreen,
    viewModel: AuthViewModel,
) = when (screen) {
    AuthScreen.Login -> viewModel.login()
    AuthScreen.SignUp -> viewModel.signUp()
    AuthScreen.VerifyEmail -> viewModel.confirmVerification()
    AuthScreen.ForgotPassword -> viewModel.requestPasswordReset()
    AuthScreen.ResetPassword -> viewModel.resetPassword()
}

private fun titleFor(screen: AuthScreen) =
    when (screen) {
        AuthScreen.Login -> "Welcome to NStream"
        AuthScreen.SignUp -> "Create your account"
        AuthScreen.VerifyEmail -> "Verify your email"
        AuthScreen.ForgotPassword -> "Reset your password"
        AuthScreen.ResetPassword -> "Choose a new password"
    }

private fun actionFor(screen: AuthScreen) =
    when (screen) {
        AuthScreen.Login -> "Sign in"
        AuthScreen.SignUp -> "Create account"
        AuthScreen.VerifyEmail -> "Verify email"
        AuthScreen.ForgotPassword -> "Send reset email"
        AuthScreen.ResetPassword -> "Reset password"
    }