package com.example.bingebox.groq

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class AiOutputValidator{
    private val json = Json { ignoreUnknownKeys = true }

    fun textFrom(providerText: String) : String? =
        runCatching {
            json
                .decodeFromString<AiStructuredOutput>(providerText)
                .text
                .trim()
                .takeIf { it.length in 1..2_000 }
        }.getOrNull()
}

@Serializable
private data class AiStructuredOutput(
    val text: String
)