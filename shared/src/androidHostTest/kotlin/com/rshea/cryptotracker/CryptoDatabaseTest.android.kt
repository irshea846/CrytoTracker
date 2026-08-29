package com.rshea.cryptotracker

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.rshea.cryptotracker.database.CryptoDatabase

actual fun createInMemoryTestDriver(): SqlDriver {
    val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
    CryptoDatabase.Schema.create(driver) // Dynamically builds your tables in RAM
    return driver

}