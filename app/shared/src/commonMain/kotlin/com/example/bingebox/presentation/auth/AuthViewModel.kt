package com.example.bingebox.presentation.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.bingebox.data.auth.remote.BingeBoxApiException
import com.example.bingebox.core.auth.AccountResponse
import com.example.bingebox.domain.auth.AuthRepository

enum class AuthScreen {
    SignUp,
    Login,
    VerifyEmail,
    ForgotPassword,
    ResetPassword
}

data class AuthUiState(
    val screen: AuthScreen = AuthScreen.Login,
    val email: String = "",
    val password: String = "",
    val token: String = "",
    val isLoading: Boolean = false,
    val account: AccountResponse? = null,
    val message: String? = null,
    val errorMessage: String? = null,
)

class AuthViewModel(
    private val repository: AuthRepository
) : ViewModel() {
    var uiState
            by mutableStateOf(AuthUiState())
        private set

    fun updateEmail(value: String) = update { copy(email = value, errorMessage = null) }

    fun updatePassword(value: String) = update { copy(password = value, errorMessage = null) }

    fun updateToken(value: String) = update { copy(token = value, errorMessage = null) }

    fun show(screen: AuthScreen) =
        update { copy(screen = screen, message = null, errorMessage = null) }

    suspend fun restoreSession() {
        runRequest {
            val account = repository.currentAccount()
            if (account == null) {
                update { copy(message = "Please sign in to continue") }
            } else {
                update { copy(account = account) }
            }
        }
    }

    suspend fun signUp() {
        if (!validCredentials()) return
        runRequest {
            repository.signUp(
                email = uiState.email.trim(),
                password = uiState.password
            )
            update {
                copy(
                    screen = AuthScreen.VerifyEmail,
                    message = "Check your email for verification token",
                    password = ""
                )
            }
        }
    }

    suspend fun login() {
        if (!validCredentials()) return
        runRequest {
            val session = repository.login(uiState.email.trim(), uiState.password)
            update {
                copy(
                    account = AccountResponse(session.accountId, session.email),
                    password = "",
                    message = null
                )
            }
        }
    }

    suspend fun requestVerification() {
        if (!validEmail()) return
        runRequest {
            val response = repository.requestVerification(uiState.email.trim())
            update { copy(message = response.message) }
        }
    }

    suspend fun confirmVerification() {
        if (uiState.token.isBlank()) return showValidation("Enter the verification token.")
        runRequest {
            val response = repository.confirmVerification(uiState.token.trim())
            update {
                copy(
                    screen = AuthScreen.Login,
                    token = "",
                    message = "${response.message} You can now sign in."
                )
            }
        }
    }

    suspend fun requestPasswordReset() {
        if (!validEmail()) return
        runRequest {
            val response = repository.requestPasswordReset(uiState.email.trim())
            update { copy(screen = AuthScreen.ResetPassword, message = response.message) }
        }
    }

    suspend fun resetPassword() {
        if (uiState.token.isBlank()) return showValidation("Enter the reset token.")
        if (uiState.password.length < 8) return showValidation("Use at least 8 characters for your password.")
        runRequest {
            val response = repository.resetPassword(uiState.token.trim(), uiState.password)
            update {
                copy(
                    screen = AuthScreen.Login,
                    token = "",
                    password = "",
                    message = response.message
                )
            }
        }
    }

    suspend fun logout() {
        runRequest {
            repository.logout()
            uiState = AuthUiState(message = "You have signed out.")
        }
    }

    private fun update(transform: AuthUiState.() -> AuthUiState) {
        uiState = uiState.transform()
    }

    private suspend fun runRequest(request: suspend () -> Unit) {
        update { copy(isLoading = true, errorMessage = null) }
        try {
            request()
            update { copy(isLoading = false) }
        } catch (exception: BingeBoxApiException) {
            update { copy(isLoading = false, errorMessage = exception.message) }
        } catch (_: Exception) {
            update { copy(isLoading = false, errorMessage = "We could not reach BingeBox") }
        }
    }

    private fun validEmail(): Boolean =
        if (uiState.email.contains('@')) {
            true
        } else {
            showValidation("Enter a valid Email Address")
            false
        }

    private fun showValidation(message: String?) = update { copy(errorMessage = message) }

    private fun validCredentials(): Boolean =
        validEmail() &&
                if (uiState.password.length >= 8) {
                    true
                } else {
                    showValidation("Use atleast 8 characters for password")
                    false
                }

}