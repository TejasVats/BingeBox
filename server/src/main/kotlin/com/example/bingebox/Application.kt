package com.example.bingebox

import com.example.bingebox.config.ServerConfigLoader
import com.example.bingebox.database.DatabaseMigrations
import com.example.bingebox.di.serverModule
import com.example.bingebox.plugins.configureHttp
import com.example.bingebox.routes.systemRoute
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.plugin.Koin

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    val config = ServerConfigLoader.load(environment)
    if(config.migrateOnStart){
        DatabaseMigrations.migrate(config)
    }
    install(Koin){
        modules(serverModule(config))
    }
    configureHttp()
    routing {
        systemRoute(config)
    }
}