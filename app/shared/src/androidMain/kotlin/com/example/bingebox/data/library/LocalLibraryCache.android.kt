package com.example.bingebox.data.library

import android.content.Context
import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.example.bingebox.data.library.database.LocalLibraryDatabase
import com.example.bingebox.data.library.database.LocalLibraryDatabaseConstructor
import kotlinx.coroutines.Dispatchers

actual fun createRoomLocalLibraryCache(databaseName: String): LocalLibraryCache =
    createRoomLocalLibraryCache(databaseName, null)

fun createRoomLocalLibraryCache(
    databaseName: String,
    context:
    Context?,
): LocalLibraryCache {
    val database =
        if (context != null) {
            Room.databaseBuilder<LocalLibraryDatabase>(
                context = context.applicationContext,
                name = databaseName,
                factory = LocalLibraryDatabaseConstructor::initialize,
            )
        } else {
            Room.databaseBuilder<LocalLibraryDatabase>(
                name = databaseName,
                factory = LocalLibraryDatabaseConstructor::initialize,
            )
        }
            .setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(Dispatchers.Default)
            .build()
    return RoomLocalLibraryCache(database)
}