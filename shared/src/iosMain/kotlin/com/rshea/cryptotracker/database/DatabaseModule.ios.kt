package com.rshea.cryptotracker.database

actual class DatabaseModule {
    private val databaseBuilderFactory by lazy {
        DatabaseBuilderFactory()
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
