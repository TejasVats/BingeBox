package com.example.bingebox.groq

import com.example.bingebox.repository.ViewingHistoryRepository
import com.example.bingebox.repository.WatchListRepository
import com.example.bingebox.watchmode.WatchModeClient
import com.example.bingebox.watchmode.WatchModeResults
import java.security.MessageDigest
import java.util.UUID

sealed interface AiServiceResult {
    data class Success(
        val text: String,
    ) : AiServiceResult

    data object ContentUnavailable : AiServiceResult
    data object RateLimited : AiServiceResult
    data object Unavailable : AiServiceResult
}

interface AiService {
    suspend fun recommendation(accountId: UUID): AiServiceResult
    suspend fun insight(titleId: Long): AiServiceResult
    suspend fun discovery(query: String): AiServiceResult
    suspend fun viewingSummary(accountId: UUID): AiServiceResult
}

class GroqAiService(
    private val groq: GroqClient,
    private val prompts: AiPromptBuilder,
    private val watchMode: WatchModeClient,
    private val watchList: WatchListRepository,
    private val history: ViewingHistoryRepository,
    private val cache: AiResponseCache,
    private val validator: AiOutputValidator
) : AiService {
    override suspend fun recommendation(accountId: UUID): AiServiceResult =
        prompts
            .recommendations(signals = signals(accountId))
            .let { complete("recommendations:$accountId", GroqModel.ADVANCED, it) }

    override suspend fun insight(titleId: Long): AiServiceResult =
        when (val content = watchMode.details(titleId)) {
            is WatchModeResults.Success ->
                prompts
                    .contentInsight(content.value)
                    .let { complete("insight:$titleId", GroqModel.FAST, it) }

            else -> AiServiceResult.ContentUnavailable
        }

    override suspend fun discovery(query: String): AiServiceResult =
        when (val results = watchMode.search(query, null)) {
            is WatchModeResults.Success ->
                complete(
                    "discovery${digest(query)}",
                    GroqModel.FAST,
                    "Interpret this discovery request using only the listed Watchmode candidates. Do not invent titles. Request: $query\n" +
                            results.value.joinToString(
                                "\n",
                            ) { "- id=${it.id}, title=${it.title}, type=${it.type}, year=${it.year}" },
                )

            else -> AiServiceResult.ContentUnavailable
        }

    override suspend fun viewingSummary(accountId: UUID): AiServiceResult =
        prompts
            .viewingSummary(signals(accountId))
            .let { complete("summary:$accountId", GroqModel.FAST, it) }


    private suspend fun signals(accountId: UUID) =
        AiAccountSignals(
            watchList.list(accountId, 100, 0),
            history.list(accountId, 100, 0)
        )

    private suspend fun complete(
        cachePrefix: String,
        model: GroqModel,
        prompt: String
    ): AiServiceResult {
        val cacheKey = "$cachePrefix: ${digest(prompt)}"
        cache.read(cacheKey)?.let { return AiServiceResult.Success(it) }
        return when (
            val result = groq.complete(model, SYSTEM_PROMPT, prompt)
        ) {
            is GroqResult.Success ->
                validator.textFrom(result.text)?.let {
                    cache.write(cacheKey, it)
                    AiServiceResult.Success(it)
                } ?: AiServiceResult.Unavailable

            GroqResult.RateLimited -> AiServiceResult.RateLimited
            GroqResult.Unavailable -> AiServiceResult.Unavailable
        }
    }

    private fun digest(value: String): String =
        MessageDigest
            .getInstance("SHA-256")
            .digest(value.toByteArray())
            .joinToString("") { "%02x".format(it) }

    private companion object {
        const val SYSTEM_PROMPT =
            "You are Bingebox's assistant. Follow the grounded user data exactly and be concise." +
                    "Return only in the form of {\"text\":\"your response\"}."
    }
}