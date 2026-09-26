package com.example.bingebox.repository

import com.example.bingebox.database.tables.ViewingHistoryTable
import com.example.bingebox.database.tables.WatchlistItemsTable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.UUID

class ExposedWatchlistRepository : WatchListRepository {
    override suspend fun add(
        accountId: UUID,
        watchModeTitleId: Long,
        media: String
    ): WatchListItemRecord = query {
        WatchlistItemsTable.selectAll().where {
            (WatchlistItemsTable.accountId eq accountId) and (WatchlistItemsTable.mediaType eq media) and (WatchlistItemsTable.watchModeTitleId eq watchModeTitleId)
        }.singleOrNull()?.toWatchListItem() ?: WatchlistItemsTable.insert {
            it[WatchlistItemsTable.accountId] = accountId
            it[WatchlistItemsTable.watchModeTitleId] = watchModeTitleId
            it[WatchlistItemsTable.mediaType] = media
            it[addedAt] = OffsetDateTime.now(ZoneOffset.UTC)
        }.resultedValues!!.single().toWatchListItem()
    }

    override suspend fun remove(
        accountId: UUID,
        watchModeTitleId: Long,
        media: String
    ): Boolean = query {
        WatchlistItemsTable.deleteWhere {
            SqlExpressionBuilder.run {
                (WatchlistItemsTable.accountId eq accountId) and
                        (WatchlistItemsTable.watchModeTitleId eq watchModeTitleId) and
                        (WatchlistItemsTable.mediaType eq mediaType)
            }
        } > 0
    }

    override suspend fun list(
        accountId: UUID,
        limit: Int,
        offset: Long
    ): List<WatchListItemRecord> = query {
        WatchlistItemsTable.selectAll().where { WatchlistItemsTable.accountId eq accountId }
            .orderBy(WatchlistItemsTable.addedAt to SortOrder.DESC)
            .limit(limit)
            .offset(offset)
            .map { it.toWatchListItem() }
    }

    private fun ResultRow.toWatchListItem() = WatchListItemRecord(
        watchModeTitleId = this[WatchlistItemsTable.watchModeTitleId],
        mediaType = this[WatchlistItemsTable.mediaType],
        addedAt = this[WatchlistItemsTable.addedAt]
    )
}

private suspend fun <T> query(block: () -> T): T = withContext(Dispatchers.IO) {
    transaction { block() }
}

class ExposedViewingHistoryRepository : ViewingHistoryRepository {
    override suspend fun record(
        accountId: UUID,
        watchModeTitleId: Long,
        mediaType: String,
        activityType: String,
    ): ViewingHistoryRecord = query {
        ViewingHistoryTable.insert {
            it[ViewingHistoryTable.accountId] = accountId
            it[ViewingHistoryTable.watchModeTitleId] = watchModeTitleId
            it[ViewingHistoryTable.mediaType] = mediaType
            it[ViewingHistoryTable.activityType] = activityType
            it[occurredAt] = OffsetDateTime.now(ZoneOffset.UTC)
        }.resultedValues!!.single().toViewingHistory()
    }

    override suspend fun list(
        accountId: UUID,
        limit: Int,
        offset: Long
    ): List<ViewingHistoryRecord> = query {
        ViewingHistoryTable.selectAll().where { ViewingHistoryTable.accountId eq accountId }
            .orderBy(ViewingHistoryTable.occurredAt to SortOrder.DESC)
            .limit(limit)
            .offset(offset)
            .map { it.toViewingHistory() }
    }

    private fun ResultRow.toViewingHistory() = ViewingHistoryRecord(
        watchModeTitleId = this[ViewingHistoryTable.watchModeTitleId],
        mediaType = this[ViewingHistoryTable.mediaType],
        activityType = this[ViewingHistoryTable.activityType],
        occurredAt = this[ViewingHistoryTable.occurredAt],
    )
}