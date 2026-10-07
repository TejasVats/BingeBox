package com.example.bingebox.core.auth

import kotlinx.serialization.Serializable

@Serializable
data class SignUpRequest(val email: String, val password: String)

@Serializable
data class LoginRequest(val email: String, val password: String)

@Serializable
data class AccountResponse(
    val accountId: String,
    val email: String,
    val verificationRequired: Boolean = true
)

@Serializable
data class AuthSession(
    val accountId: String,
    val email: String,
    val accessToken: String,
    val refreshToken: String
)

@Serializable
data class EmailRequest(val email: String)

@Serializable
data class TokenRequest(val token: String)

@Serializable
data class ResetPasswordRequest(val token: String, val password: String)

@Serializable
data class RefreshRequest(val refreshToken: String)

@Serializable
data class MessageResponse(val message: String)

@Serializable
data class ApiErrorResponse(
    val code: String = "Request_failed",
    val message: String = "The request could not be completed.",
    val fields: List<FieldValidationError> = emptyList()
)

@Serializable
data class FieldValidationError(
    val field: String,
    val message: String
)