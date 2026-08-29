package com.rshea.cryptotracker

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import androidx.test.platform.app.InstrumentationRegistry
import com.rshea.cryptotracker.database.CryptoDatabase

actual fun createInMemoryTestDriver(): SqlDriver {
    return AndroidSqliteDriver(
        schema = CryptoDatabase.Schema,
        context = InstrumentationRegistry.getInstrumentation().targetContext,
        name = null // null for in-memory database
    )
}
