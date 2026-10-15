package com.example.bingebox.data.library.worker

import org.w3c.dom.Worker

@JsModule("sqlite-wasm-worker/worker.js")
@JsNonModule
external val sqliteWorkerUrl: dynamic

fun createSQLiteWorker(): Worker = Worker(sqliteWorkerUrl.unsafeCast<String>())
