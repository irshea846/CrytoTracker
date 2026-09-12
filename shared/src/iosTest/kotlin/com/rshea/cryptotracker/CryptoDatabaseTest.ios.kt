package com.rshea.cryptotracker

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import com.rshea.cryptotracker.database.CryptoDatabase

actual fun createInMemoryTestDriver(): SqlDriver {
    // Use a physical file for the test database on iOS to avoid sandbox permission issues with empty strings.
    // Isolation is handled by explicitly clearing tables in the common Test setup.
    return NativeSqliteDriver(CryptoDatabase.Schema, "crypto_test.db")
}
