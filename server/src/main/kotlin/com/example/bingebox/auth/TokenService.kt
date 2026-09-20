package com.example.bingebox.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.example.bingebox.config.ServerConfig
import com.example.bingebox.repository.AccountRecord
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Date
import java.time.Instant
import java.util.Base64

class TokenService(config: ServerConfig){
    private val algorithm = Algorithm.HMAC256(config.jwtSecret)

    fun createAccessToken(account: AccountRecord): String = JWT.create()
        .withSubject(account.id.toString())
        .withClaim("email", account.email)
        .withIssuedAt(Date.from(Instant.now()))
        .withExpiresAt(Date.from(Instant.now().plusSeconds(15 * 60)))
        .sign(algorithm)

    fun createOpaqueToken() : String =
        Base64.getUrlEncoder()
            .withoutPadding()
            .encodeToString(
                ByteArray(
                    48
                ).also(random::nextBytes)
            )

    fun hashOpaqueToken(token: String) : String = MessageDigest.getInstance("SHA-256")
        .digest(token.toByteArray(Charsets.UTF_8))
        .joinToString(""){"%02x".format(it)}


    private val random = SecureRandom()
}