package com.rshea.cryptotracker.database

/**
 * Technical verification class to ensure your SQLDelight compiler plugin
 * has correctly generated your type-safe Kotlin classes from your CryptoDatabase.sq file.
 */
class DatabaseModule(private val driverFactory: DatabaseDriverFactory) {

    // 1. Initialize your overall auto-generated database wrapper container
    private val database = CryptoDatabase(driverFactory.createDriver())

    // 2. FIXED: Verifies that the compiler generated your exact 'cryptoDatabaseQueries' accessor
    // based on your custom 'CryptoDatabase.sq' file name layout!
    val cryptoDatabaseQueries = database.cryptoDatabaseQueries
}
