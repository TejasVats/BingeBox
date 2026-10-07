package com.example.bingebox.data.repository

import com.example.bingebox.auth.BingeBoxApiClient
import com.example.bingebox.core.auth.AccountResponse
import com.example.bingebox.core.auth.AuthSession
import com.example.bingebox.core.auth.EmailRequest
import com.example.bingebox.core.auth.LoginRequest
import com.example.bingebox.core.auth.MessageResponse
import com.example.bingebox.core.auth.ResetPasswordRequest
import com.example.bingebox.core.auth.SignUpRequest
import com.example.bingebox.core.auth.TokenRequest
import com.example.bingebox.domain.repository.AuthRepository

class AuthRepositoryImp(
    val apiClient: BingeBoxApiClient
) : AuthRepository {
    override suspend fun signUp(
        email: String,
        password: String
    ): AccountResponse = apiClient.signUp(
        SignUpRequest(
            email, password
        )
    )

    override suspend fun login(
        email: String,
        password: String
    ): AuthSession = apiClient.login(
        LoginRequest(
            email, password
        )
    )

    override suspend fun requestVerification(email: String): MessageResponse =
        apiClient.requestVerification(
            EmailRequest(
                email
            )
        )

    override suspend fun confirmVerification(token: String): MessageResponse =
        apiClient.confirmVerification(
            TokenRequest(token)
        )

    override suspend fun requestPasswordReset(email: String): MessageResponse =
        apiClient.requestPasswordReset(
            EmailRequest(email)
        )

    override suspend fun resetPassword(
        token: String,
        password: String
    ): MessageResponse = apiClient.resetPassword(ResetPasswordRequest(token, password))

    override suspend fun currentAccount(): AccountResponse? = apiClient.currentAccount()

    override suspend fun logout() = apiClient.logout()
    //41:00

}