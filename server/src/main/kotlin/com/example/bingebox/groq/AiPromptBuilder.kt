package com.example.bingebox.groq

import com.example.bingebox.core.content.ContentDetailsResponse
import com.example.bingebox.repository.ViewingHistoryRecord
import com.example.bingebox.repository.WatchListItemRecord

data class AiAccountSignals(
    val watchlist: List<WatchListItemRecord>,
    val history: List<ViewingHistoryRecord>,
)

/** Builds provider prompts only from Bingebox-owned metadata and the authenticated account's records. */
class AiPromptBuilder {
    fun recommendations(signals: AiAccountSignals): String =
        """
        Recommend films or series using only the account signals below. Do not claim the user watched, liked, or disliked anything not listed.
        Watchlist: ${signals.watchlist.describeWatchlist()}
        Viewing history: ${signals.history.describeHistory()}
        Return concise recommendations and clearly say when signals are insufficient.
        """.trimIndent()

    fun contentInsight(content: ContentDetailsResponse): String =
        """
        Give a concise, spoiler-aware insight based only on this Watchmode metadata.
        ${content.describe()}
        Do not invent cast, genres, ratings, availability, or plot details absent from the metadata.
        """.trimIndent()

    fun discovery(
        query: String,
        candidates: List<ContentDetailsResponse>,
    ): String =
        """
        Interpret the discovery request using only the listed Watchmode candidates. Do not recommend titles outside the candidates.
        Request: $query
        Candidates:
        ${candidates.joinToString("\n") { "- ${it.describe()}" }.ifBlank { "- No candidates available." }}
        """.trimIndent()

    fun viewingSummary(signals: AiAccountSignals): String =
        """
        Summarize viewing patterns only from the persisted activity below. Never infer preferences or habits from missing data.
        Viewing history: ${signals.history.describeHistory()}
        If there is too little history, provide starter guidance instead of a personalized summary.
        """.trimIndent()
}

private fun ContentDetailsResponse.describe(): String =
    listOfNotNull(
        "id=$id",
        "title=$title",
        "type=$type",
        year?.let { "year=$it" },
        runtimeMinutes?.let { "runtimeMinutes=$it" },
        overview?.let { "overview=$it" },
    ).joinToString(", ")

private fun List<WatchListItemRecord>.describeWatchlist(): String =
    joinToString("; ") { "titleId=${it.watchModeTitleId}, type=${it.mediaType}" }.ifBlank { "none" }

private fun List<ViewingHistoryRecord>.describeHistory(): String =
    joinToString("; ") {
        "titleId=${it.watchModeTitleId}, type=${it.mediaType}, activity=${it.activityType}, at=${it.occurredAt}"
    }.ifBlank { "none" }