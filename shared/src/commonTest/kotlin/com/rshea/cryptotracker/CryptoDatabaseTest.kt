package com.rshea.cryptotracker

import app.cash.sqldelight.db.SqlDriver // ◄── Stable, cross-platform base driver import
import com.rshea.cryptotracker.database.CryptoDatabase
import com.rshea.cryptotracker.database.CryptoDatabaseQueries
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

// 1. Declare an expected test engine builder hook
expect fun createInMemoryTestDriver(): SqlDriver

class CryptoDatabaseTest {

    private lateinit var database: CryptoDatabase
    private lateinit var queries: CryptoDatabaseQueries

    @BeforeTest
    fun setup() {
        // 2. Request the platform-specific in-memory test driver handle natively
        val driver = createInMemoryTestDriver()
        database = CryptoDatabase(driver)
        queries = database.cryptoDatabaseQueries
        
        // 3. FORCE CLEAN SLATE: Ensure no data leaks between test methods
        queries.clearCryptoTable()
    }

    @Test
    fun testDatabase_insertAndFetchAll_OperatesPragmatically() {
        // Assert table begins completely empty
        val initialList = queries.getAllCryptoAssets().executeAsList()
        assertTrue(initialList.isEmpty())

        // 1. EXECUTE TRANSACTION: Insert mock cryptocurrency asset row
        queries.insertCryptoAsset(
            id = "btc",
            symbol = "BTC",
            name = "Bitcoin",
            priceUsd = 60000.00,
            changePercent24Hr = 2.4,
            supply = 19000000.0,
            marketCapUsd = 1200000000000.0,
            volumeUsd24Hr = 30000000000.0,
            priceChange24hText = "+2.4",
            isPricePositive = 1L
        )

        // 2. FETCH VALIDATION: Verify row entry matches state criteria fields
        val savedRecords = queries.getAllCryptoAssets().executeAsList()
        assertEquals(1, savedRecords.size)

        val bitcoinEntity = savedRecords.first()
        assertEquals("btc", bitcoinEntity.id)
        assertEquals("BTC", bitcoinEntity.symbol)
        assertEquals("Bitcoin", bitcoinEntity.name)
        assertEquals(1200000000000.0, bitcoinEntity.marketCapUsd)
    }

    @Test
    fun testDatabase_ClearTable_FlushesRecordsCleanly() {
        // Insert item to clear
        queries.insertCryptoAsset(
            id = "eth",
            symbol = "ETH",
            name = "Ethereum",
            priceUsd = 3000.00,
            changePercent24Hr = -1.2,
            supply = 120000000.0,
            marketCapUsd = 360000000000.0,
            volumeUsd24Hr = 15000000000.0,
            priceChange24hText = "-1.2",
            isPricePositive = 0L
        )

        // Execute flushing clean command
        queries.clearCryptoTable()

        // Assert data is entirely gone from active local schema tables
        val checkedList = queries.getAllCryptoAssets().executeAsList()
        assertTrue(checkedList.isEmpty())
    }
}
