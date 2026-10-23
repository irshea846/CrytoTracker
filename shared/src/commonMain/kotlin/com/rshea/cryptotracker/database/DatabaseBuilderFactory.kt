package com.rshea.cryptotracker.database

import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver

// Creates a platform-agnostic class signature that maps to native files
expect class DatabaseBuilderFactory {
    fun createBuilder(): RoomDatabase.Builder<AppDatabase>
}

// Global initialization helper to mount the Bundled SQLite Driver cleanly
fun createRoomDatabase(factory: DatabaseBuilderFactory): AppDatabase {
    return factory.createBuilder()
        .setDriver(BundledSQLiteDriver()) // Enforces consistent SQLite behavior across iOS & Android
        .build()
    // Make sure to double-check the physical labels when wiring up future schema migration passes!
}