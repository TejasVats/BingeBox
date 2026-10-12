package com.example.bingebox.presentation.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface BingeBoxRoute : NavKey {

    val titleID: Long

    @Serializable
    data object Home : BingeBoxRoute {
        override val titleID: Long = 0L
    }

    @Serializable
    data object Search : BingeBoxRoute {
        override val titleID: Long = 0L
    }

    @Serializable
    data class Detail(val titleId: Long) : BingeBoxRoute {
        override val titleID: Long get() = titleId
    }

    @Serializable
    data object Watchlist : BingeBoxRoute {
        override val titleID: Long = 0L
    }

    @Serializable
    data object Trailer : BingeBoxRoute {
        override val titleID: Long = 0L
    }

    @Serializable
    data object AiRecommendations : BingeBoxRoute{
        override val titleID: Long = 0L
    }

    @Serializable
    data object AiInsights : BingeBoxRoute{
        override val titleID: Long = 0L
    }

    @Serializable
    data object NaturalLanguageSearch : BingeBoxRoute {
        override val titleID: Long = 0L
    }

    @Serializable
    data object ViewingDashboard : BingeBoxRoute {
        override val titleID: Long = 0L
    }
}