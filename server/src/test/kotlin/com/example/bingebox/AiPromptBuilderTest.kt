package com.example.bingebox

import com.example.bingebox.groq.AiAccountSignals
import com.example.bingebox.groq.AiPromptBuilder
import com.example.bingebox.repository.ViewingHistoryRecord
import com.example.bingebox.repository.WatchListItemRecord
import com.example.bingebox.watchmode.ContentDetails
import java.time.OffsetDateTime
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertFalse

class AiPromptBuilderTest {
    @Test fun `recommendation prompt includes only account owned library signals`() {
        val prompt =
            AiPromptBuilder().recommendations(
                AiAccountSignals(
                    watchlist = listOf(WatchListItemRecord(12, "movie", OffsetDateTime.now())),
                    history = listOf(
                        ViewingHistoryRecord(
                            44,
                            "tv_series",
                            "detail_opened",
                            OffsetDateTime.now()
                        )
                    ),
                ),
            )

        assertContains(prompt, "titleId=12, type=movie")
        assertContains(prompt, "titleId=44, type=tv_series, activity=detail_opened")
        assertContains(prompt, "Do not claim the user watched")
    }

    @Test
    fun `content prompt is grounded in provided Watchmode metadata`() {
        val prompt =
            AiPromptBuilder().contentInsight(
                ContentDetails(7, "Arrival", "movie", "A linguist meets visitors.", 116, 2016),
            )

        assertContains(prompt, "title=Arrival")
        assertContains(prompt, "overview=A linguist meets visitors.")
        assertFalse(prompt.contains("cast="))
    }
}