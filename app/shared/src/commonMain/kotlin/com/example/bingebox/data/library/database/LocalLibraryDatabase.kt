package com.example.bingebox.data.library.database

import androidx.room3.ConstructedBy
import androidx.room3.Database
import androidx.room3.RoomDatabase
import androidx.room3.RoomDatabaseConstructor
import com.example.bingebox.data.library.dao.LocalLibraryDao
import com.example.bingebox.data.model.CachedContentMetadataEntity
import com.example.bingebox.data.model.CachedViewingHistoryEntity
import com.example.bingebox.data.model.CachedWatchListItemEntity

@Database(
    entities = [
        CachedWatchListItemEntity::class,
        CachedViewingHistoryEntity::class,
        CachedContentMetadataEntity::class
    ],
    version = 1
)
@ConstructedBy(LocalLibraryDatabaseConstructor::class)
abstract class LocalLibraryDatabase : RoomDatabase() {
    abstract fun libraryDao(): LocalLibraryDao
}

expect object LocalLibraryDatabaseConstructor : RoomDatabaseConstructor<LocalLibraryDatabase> {
    override fun initialize(): LocalLibraryDatabase
}