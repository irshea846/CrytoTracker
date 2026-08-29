package com.rshea.cryptotracker

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver // ◄── Resolves natively for Apple architectures!
import com.rshea.cryptotracker.database.CryptoDatabase

actual fun createInMemoryTestDriver(): SqlDriver {
    // Passing "in-memory" initialization states to the native schema builder
    val driver = NativeSqliteDriver(CryptoDatabase.Schema, "crypto_test.db")
    return driver
}