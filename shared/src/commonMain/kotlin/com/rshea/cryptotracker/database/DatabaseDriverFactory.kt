package com.rshea.cryptotracker.database

import app.cash.sqldelight.db.SqlDriver

// Pure, standard Kotlin interface (100% stable, zero warnings)
expect class DatabaseDriverFactory {
    fun createDriver(): SqlDriver
}
