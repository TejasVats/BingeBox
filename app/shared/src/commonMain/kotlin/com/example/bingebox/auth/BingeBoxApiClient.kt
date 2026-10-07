package com.example.bingebox.auth

import com.example.bingebox.core.auth.AccountResponse
import com.example.bingebox.core.auth.ApiErrorResponse
import com.example.bingebox.core.auth.AuthSession
import com.example.bingebox.core.auth.EmailRequest
import com.example.bingebox.core.auth.LoginRequest
import com.example.bingebox.core.auth.MessageResponse
import com.example.bingebox.core.auth.RefreshRequest
import com.example.bingebox.core.auth.ResetPasswordRequest
import com.example.bingebox.core.auth.SignUpRequest
import com.example.bingebox.core.auth.TokenRequest
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.accept
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.isSuccess

data class BingeBoxApiConfiguration(
    val baseUrl: String = "https://10.0.2.2:8080"
//    val baseUrl: String = "http://localhost:8080"
)

class BingeBoxApiClient(
    private val httpClient: HttpClient,
    private val configuration: BingeBoxApiConfiguration,
    private val tokenStorage: AuthTokenStorage
) {
    suspend fun signUp(request: SignUpRequest): AccountResponse =
        post("/api/v1/auth/signup", request)

    suspend fun login(request: LoginRequest): AuthSession {
        val session: AuthSession = post("/api/v1/auth/login", request)
        saveSession(session)
        return session
    }

    suspend fun requestVerification(request: EmailRequest): MessageResponse =
        post("/api/v1/auth/request-verification", request)

    suspend fun confirmVerification(request: TokenRequest): MessageResponse =
        post("/api/v1/auth/confirm-verification", request)

    suspend fun requestPasswordReset(request: EmailRequest): MessageResponse =
        post("/api/v1/auth/forgot-password", request)

    suspend fun resetPassword(request: ResetPasswordRequest): MessageResponse =
        post("/api/v1/auth/reset-password", request)

    suspend fun currentAccount(): AccountResponse? {
        if (tokenStorage.read() == null) return null
        var response = authenticatedGet("/api/v1/me")
        if (response.status == HttpStatusCode.Unauthorized && !refreshSession()) return null
        if (response.status == HttpStatusCode.Unauthorized) response =
            authenticatedGet("/api/v1/me")
        return if (response.status.isSuccess()) response.body() else null
    }

    suspend fun logout() {
        val refreshToken = tokenStorage.read()?.refreshToken ?: return
        post<Unit>("/api/v1/auth/logout", RefreshRequest(refreshToken), allowNoContent = true)
        tokenStorage.clear()
    }

    private suspend fun authenticatedGet(path: String) =
        httpClient.get(url(path)) {
            accept(ContentType.Application.Json)
            tokenStorage.read()
                ?.let { header(HttpHeaders.Authorization, "Bearer ${it.accessToken}") }
        }

    private suspend fun refreshSession(): Boolean {
        val refreshToken = tokenStorage.read()?.refreshToken ?: return false
        val response =
            httpClient.post(url("/api/v1/auth/refresh")) {
                accept(ContentType.Application.Json)
                contentType(ContentType.Application.Json)
                setBody(RefreshRequest(refreshToken))
            }
        if (!response.status.isSuccess()) {
            tokenStorage.clear()
            return false
        }
        saveSession(response.body())
        return true
    }

    private suspend inline fun <reified Response> post(
        path: String,
        request: Any,
        allowNoContent: Boolean = false
    ): Response {
        val response =
            httpClient.post(url(path)) {
                accept(ContentType.Application.Json)
                contentType(ContentType.Application.Json)
                setBody(request)
            }
        if (allowNoContent && response.status == HttpStatusCode.NoContent) {
            return Unit as Response
        }
        if (!response.status.isSuccess()) {
            throw BingeBoxApiException(
                statusCode = response.status.value,
                message = response.body<ApiErrorResponse>().message
            )
        }
        return response.body()
    }

    private fun url(path: String): String = configuration.baseUrl.trim('/') + path

    private fun saveSession(session: AuthSession) {
        tokenStorage.save(StoredAuthTokens(session.accessToken, session.refreshToken))
    }
}

class BingeBoxApiException(
    val statusCode: Int,
    override val message: String
) : Exception(message)