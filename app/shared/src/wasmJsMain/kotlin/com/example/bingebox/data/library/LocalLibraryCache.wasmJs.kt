package com.example.bingebox.data.library

import androidx.room3.Room
import androidx.sqlite.driver.web.WebWorkerSQLiteDriver
import com.example.bingebox.data.library.database.LocalLibraryDatabase
import com.example.bingebox.data.library.database.LocalLibraryDatabaseConstructor
import org.w3c.dom.Worker

@JsModule("sqlite-wasm-worker/worker.js")
external val sqliteWorkerUrl: String

actual fun createRoomLocalLibraryCache(databaseName: String): LocalLibraryCache =
    try {
        val database =
            Room.databaseBuilder<LocalLibraryDatabase>(
                name = databaseName,
                factory = LocalLibraryDatabaseConstructor::initialize,
            )
                .setDriver(WebWorkerSQLiteDriver(Worker(sqliteWorkerUrl)))
                .build()
        RoomLocalLibraryCache(database)
    } catch (_: Throwable) {
        UnavailableLocalLibraryCache("Offline library storage is unavailable in this browser.")
    }