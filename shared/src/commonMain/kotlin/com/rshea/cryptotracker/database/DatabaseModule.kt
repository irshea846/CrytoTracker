package com.rshea.cryptotracker.database

// Defines a platform-agnostic manual injection container module
expect class DatabaseModule {
    // Provides a fully initialized, shared AppDatabase instance at runtime
    fun provideAppDatabase(): AppDatabase

    // Provides the thread-safe CryptoAssetDao data access instance
    fun provideCryptoAssetDao(): CryptoAssetDao
}
