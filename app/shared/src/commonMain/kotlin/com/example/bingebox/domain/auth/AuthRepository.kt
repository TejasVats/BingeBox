package com.example.bingebox.domain.auth

import com.example.bingebox.core.auth.AccountResponse
import com.example.bingebox.core.auth.AuthSession
import com.example.bingebox.core.auth.MessageResponse

interface AuthRepository {
    suspend fun signUp(
        email: String,
        password: String
    ): AccountResponse

    suspend fun login(
        email: String,
        password: String
    ): AuthSession

    suspend fun requestVerification(email: String): MessageResponse

    suspend fun confirmVerification(token: String): MessageResponse

    suspend fun requestPasswordReset(email: String): MessageResponse

    suspend fun resetPassword(
        token: String,
        password: String
    ): MessageResponse

    suspend fun currentAccount(): AccountResponse?

    suspend fun logout()
}