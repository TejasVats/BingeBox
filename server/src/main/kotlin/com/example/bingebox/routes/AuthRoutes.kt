package com.example.bingebox.routes

import com.example.bingebox.api.API_VERSION_PREFIX
import com.example.bingebox.api.FieldValidationError
import com.example.bingebox.api.ValidationErrorResponse
import com.example.bingebox.auth.AuthResult
import com.example.bingebox.auth.AuthService
import com.example.bingebox.auth.LoginResult
import com.example.bingebox.models.AccountCreatedResponse
import com.example.bingebox.models.EmailRequest
import com.example.bingebox.models.LoginAcceptedResponse
import com.example.bingebox.models.LoginRequest
import com.example.bingebox.models.MessageResponse
import com.example.bingebox.models.RefreshRequest
import com.example.bingebox.models.ResetPasswordRequest
import com.example.bingebox.models.SignUpRequest
import com.example.bingebox.models.TokenRequest
import com.example.bingebox.plugins.ApiError
import com.example.bingebox.security.RedisRateLimiter
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.plugins.callid.callId
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post

fun Route.authRoutes(authService: AuthService, rateLimiter: RedisRateLimiter) {
    post("$API_VERSION_PREFIX/auth/signup") {
        val request = call.receive<SignUpRequest>()
        if (!rateLimiter.allows("signup", request.email)) {
            call.respond(
                HttpStatusCode.TooManyRequests,
                ApiError("rate_limited", "Try again in a minute", call.callId)
            ); return@post
        }

        when (val result = authService.signUp(request.email, request.password)) {
            is AuthResult.Invalid -> call.respond(
                HttpStatusCode.UnprocessableEntity,
                ValidationErrorResponse(
                    fields = result.errors.map {
                        FieldValidationError(
                            it.field,
                            it.message
                        )
                    },
                    requestId = call.callId
                )
            )

            is AuthResult.Success -> call.respond(
                HttpStatusCode.Created,
                AccountCreatedResponse(
                    result.value.id.toString(),
                    result.value.email
                )
            )
        }
    }

    post("$API_VERSION_PREFIX/auth/login") {
        val request = call.receive<LoginRequest>()
        if (!rateLimiter.allows("login", request.email)) {
            call.respond(
                HttpStatusCode.TooManyRequests,
                ApiError("rate_limited", "Too Many Requests", call.callId)
            ); return@post
        }

        when (val result = authService.login(request.email, request.password)) {
            is LoginResult.Success -> call.respond(
                HttpStatusCode.OK,
                LoginAcceptedResponse(
                    accountId = result.account.id.toString(),
                    email = result.account.email,
                    accessToken = result.tokens.accessTokens,
                    refreshToken = result.tokens.refreshTokens
                )
            )

            is LoginResult.EmailNotVerified -> call.respond(
                HttpStatusCode.Forbidden,
                ApiError("email_not_verified", "Verify your email", call.callId)
            )

            is LoginResult.InvalidCredentials -> call.respond(
                HttpStatusCode.Unauthorized,
                ApiError(
                    code = "invalid_credentials",
                    message = "Email or Password is incorrect",
                    requestId = call.callId
                )
            )
        }
    }

    post("$API_VERSION_PREFIX/auth/request_verification"){
        authService.requestVerification(call.receive<EmailRequest>().email)
        call.respond(
            HttpStatusCode.Accepted,
            MessageResponse("If that account exists, a verification mail will be sent")
        )
    }

    post("$API_VERSION_PREFIX/auth/confirm-verification"){
        if(authService.confirmVerification(call.receive<TokenRequest>().token)){
            call.respond(
                MessageResponse("Email Verified")
            )
        } else {
            call.respond(
                HttpStatusCode.BadRequest,
                ApiError(
                    "invalid_token",
                    "The verification token is invalid, expired or already in used",
                    call.callId
                )
            )
        }
    }

    post("$API_VERSION_PREFIX/auth/forgot-password"){
        authService.requestPasswordReset(call.receive<EmailRequest>().email)
        call.respond(
            HttpStatusCode.Accepted,
            MessageResponse("If that account exists, a reset email will be sent.")
        )
    }

    post("$API_VERSION_PREFIX/auth/reset-password"){
        val request = call.receive<ResetPasswordRequest>()
        if(authService.resetPassword(
            request.token,
            request.password
        )){
            call.respond(MessageResponse("Password Reset"))
        } else {
            call.respond(
                HttpStatusCode.BadRequest,
                ApiError("invalid_reset", "The reset token password is invalid", call.callId)
            )
        }
    }

    post("$API_VERSION_PREFIX/auth/refresh"){
        val refreshToken = call.receive<RefreshRequest>().refreshToken
        if(!rateLimiter.allows("refresh", refreshToken,10)){
            call.respond(
                HttpStatusCode.TooManyRequests,
                ApiError("rate_limited", "Try again in a minute", call.callId)
            ); return@post
        }

        val tokens = authService.refresh(refreshToken)
        if(tokens == null) call.respond(
            HttpStatusCode.Unauthorized,
            ApiError("Invalid Session", "The refresh session is invalid", call.callId)
        ) else
            call.respond(tokens)
    }

    post("$API_VERSION_PREFIX/auth/logout"){
        authService.logout(call.receive<RefreshRequest>().refreshToken)
        call.respond(HttpStatusCode.NoContent)
    }

    authenticate("auth-jwt"){
        get("$API_VERSION_PREFIX/me"){
            val accountId = call.principal<JWTPrincipal>()!!.payload.subject
            val account = authService.currentAccount(accountId)
            if(account == null)
                call.respond(
                    HttpStatusCode.Unauthorized,
                    ApiError("invalid_token", "the access token is invalid", call.callId)
                )
            else
                call.respond(
                    AccountCreatedResponse(
                        accountId = account.id.toString(),
                        email = account.email,
                        verificationRequired = account.emailVerifiedAt == null
                    )
                )
        }
    }
}