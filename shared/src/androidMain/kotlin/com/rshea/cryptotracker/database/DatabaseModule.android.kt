package com.rshea.cryptotracker.database

import android.content.Context

actual class DatabaseModule(
    private val context: Context // Context passed directly from the Android native app layer
) {
    private val databaseBuilderFactory by lazy {
        DatabaseBuilderFactory(context)
    }

    private val appDatabase by lazy {
        createRoomDatabase(databaseBuilderFactory)
    }

    actual fun provideAppDatabase(): AppDatabase {
        return appDatabase
    }

    actual fun provideCryptoAssetDao(): CryptoAssetDao {
        return appDatabase.cryptoAssetDao()
    }
}
