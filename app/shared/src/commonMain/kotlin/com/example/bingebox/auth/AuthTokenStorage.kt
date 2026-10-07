package com.example.bingebox.auth

data class StoredAuthTokens(
    val accessToken: String,
    val refreshToken: String
)

interface AuthTokenStorage {
    fun read(): StoredAuthTokens?
    fun save(tokens: StoredAuthTokens)
    fun clear()
}

class InMemoryAuthTokenStorage : AuthTokenStorage{
    private var tokens: StoredAuthTokens? = null

    override fun read(): StoredAuthTokens? = tokens

    override fun save(tokens: StoredAuthTokens) {
        this.tokens = tokens
    }

    override fun clear() {
        tokens = null
    }

}