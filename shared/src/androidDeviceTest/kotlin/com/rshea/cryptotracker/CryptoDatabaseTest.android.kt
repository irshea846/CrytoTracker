package com.rshea.cryptotracker

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import androidx.test.platform.app.InstrumentationRegistry
import com.rshea.cryptotracker.database.CryptoDatabase

// 10/21 TEMPORARY SEPARATION: Comment out the legacy SQLDelight test stubs
// until we implement our fresh Room Multiplatform Test Drivers later this week.
/*
actual fun createInMemoryTestDriver(): SqlDriver {
    return AndroidSqliteDriver(
        schema = CryptoDatabase.Schema,
        context = InstrumentationRegistry.getInstrumentation().targetContext,
        name = null // null for in-memory database
    )
}
*/