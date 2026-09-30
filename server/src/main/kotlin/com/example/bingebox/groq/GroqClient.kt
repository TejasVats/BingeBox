package com.example.bingebox.groq

import com.example.bingebox.config.ServerConfig
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory

enum class GroqModel { FAST, ADVANCED }

sealed interface GroqResult {
    data class Success(val text: String) : GroqResult
    data object RateLimited : GroqResult
    data object Unavailable : GroqResult
}

class GroqClient(
    private val config: ServerConfig,
    private val http: HttpClient = createHttpClient()
) {

    private val logger = LoggerFactory.getLogger(GroqClient::class.java)
    private val json = Json { ignoreUnknownKeys = true }
    suspend fun complete(
        model: GroqModel,
        systemPrompt: String,
        userPrompt: String
    ): GroqResult =
        try {
            val response = http.post("${config.groqBaseUrl.trimEnd('/')}/chat/completions") {
                header(HttpHeaders.Authorization, "Bearer ${config.groqApiKey}")
                contentType(ContentType.Application.Json)
                setBody(
                    json.encodeToString(
                        serializer = GroqChatRequest.serializer(),
                        value = GroqChatRequest(
                            model = modelName(model),
                            messages = listOf(
                                GroqMessage("system", systemPrompt),
                                GroqMessage("user", content = userPrompt)
                            )
                        )
                    )
                )
            }
            when (response.status) {
                HttpStatusCode.OK ->
                    json
                        .decodeFromString<GroqChatResponse>(response.bodyAsText())
                        .choices
                        .firstOrNull()
                        ?.message?.content
                        ?.takeIf { it.isNotBlank() }
                        ?.let(GroqResult::Success) ?: run {
                        logger.error("Groq response choice content was blank or missing: ${response.bodyAsText()}")
                        GroqResult.Unavailable
                    }

                HttpStatusCode.TooManyRequests -> {
                    logger.warn("Groq API rate limited (429)")
                    GroqResult.RateLimited
                }

                else -> {
                    logger.error("Groq API error ${response.status}: ${response.bodyAsText()}")
                    GroqResult.Unavailable
                }
            }
        } catch (e: Exception) {
            logger.error("Groq API request failed", e)
            GroqResult.Unavailable
        }

    private fun modelName(model: GroqModel): String =
        when (model) {
            GroqModel.FAST -> config.groqFastModel
            GroqModel.ADVANCED -> config.groqAdvanceModel
        }

    companion object {
        private fun createHttpClient() =
            HttpClient(CIO) {
                install(HttpTimeout) {
                    requestTimeoutMillis = 12_000
                    connectTimeoutMillis = 3_000
                    socketTimeoutMillis = 12_000
                }
            }
    }
}

@Serializable
private data class GroqChatRequest(
    val model: String,
    val messages: List<GroqMessage>,
    @SerialName("response_format")
    val responseFormat: GroqResponseFormat? = GroqResponseFormat("json_object")
)

@Serializable
private data class GroqResponseFormat(
    val type: String
)

@Serializable
private data class GroqMessage(
    val role: String,
    val content: String
)

@Serializable
private data class GroqChatResponse(
    val choices: List<GroqChoice> = emptyList()
)

@Serializable
private data class GroqChoice(
    val message: GroqMessage
)