package com.example.bingebox.data.library

import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.example.bingebox.data.library.database.LocalLibraryDatabase
import com.example.bingebox.data.library.database.LocalLibraryDatabaseConstructor
import kotlinx.coroutines.Dispatchers

actual fun createRoomLocalLibraryCache(databaseName: String): LocalLibraryCache {
    val database =
        Room.databaseBuilder<LocalLibraryDatabase>(
            name = databaseName,
            factory = LocalLibraryDatabaseConstructor::initialize,
        )
            .setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(Dispatchers.Default)
            .build()
    return RoomLocalLibraryCache(database)
}