package com.example.bingebox.data.library

import androidx.room3.Room
import androidx.sqlite.driver.web.WebWorkerSQLiteDriver
import com.example.bingebox.data.library.database.LocalLibraryDatabase
import com.example.bingebox.data.library.database.LocalLibraryDatabaseConstructor
import com.example.bingebox.data.library.worker.createSQLiteWorker

actual fun createRoomLocalLibraryCache(databaseName: String): LocalLibraryCache =
    try {
        val database =
            Room.databaseBuilder<LocalLibraryDatabase>(
                name = databaseName,
                factory = LocalLibraryDatabaseConstructor::initialize,
            )
                .setDriver(WebWorkerSQLiteDriver(createSQLiteWorker()))
                .build()
        RoomLocalLibraryCache(database)
    } catch (_: Throwable) {
        UnavailableLocalLibraryCache("Offline library storage is unavailable in this browser.")
    }