package com.example.bingebox.database

import com.example.bingebox.config.ServerConfig
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.sql.Database

object DatabaseMigrations {
    fun connect(config: ServerConfig) : Database = Database.connect(
        url = config.databaseUrl,
        driver = "org.postgresql.Driver",
        user = config.databaseUser,
        password = config.databasePassword
    )
    fun migrate(config: ServerConfig){
        Flyway.configure()
            .dataSource(
                config.databaseUrl,config.databaseUser,config.databasePassword
            )
            .locations("classpath:db/migration")
            .load()
            .migrate()
    }
}