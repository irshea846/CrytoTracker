package com.rshea.cryptotracker.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface CryptoAssetDao {

    // Exposes a live, asynchronous SQLite tracking stream for all crypto assets
    @Query("SELECT * FROM room_crypto_assets ORDER BY rank ASC")
    fun getAllCryptoAssetsFlow(): Flow<List<CryptoAssetRoomEntity>>

    // Fetches a single asset out of the database for the detailed card presentation view
    @Query("SELECT * FROM room_crypto_assets WHERE id = :id LIMIT 1")
    suspend fun getCryptoAssetById(id: String): CryptoAssetRoomEntity?

    // Change return type from LongArray to List<Long>
    // This provides full cross-platform collection type safety inside commonMain metadata targets
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCryptoAssets(assets: List<CryptoAssetRoomEntity>): List<Long>

    // For database cleanup routines, force the query to return an Int representing the count of rows wiped
    @Query("DELETE FROM room_crypto_assets")
    suspend fun deleteAllCryptoAssets(): Int

    // === WEEK 13 MULTI-TABLE RELATIONAL QUERY ENGINES ===

    @Transaction // Enforces an isolation boundary so parent and children load in a single atomic snapshot
    @Query("SELECT * FROM room_crypto_assets ORDER BY rank ASC")
    fun getCryptoAssetsWithHistoryFlow(): Flow<List<CryptoAssetWithHistory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCryptoHistory(history: List<CryptoHistoryRoomEntity>): List<Long>

    // === WEEK 14 AUTOMATED ROLLING WINDOW ROBUST STORAGE PRUNING ===

    @Query("DELETE FROM room_crypto_history WHERE timestamp < :thresholdTimestamp")
    suspend fun pruneHistoryOlderThan(thresholdTimestamp: Long): Int

    /**
     * Production-Grade transactional pipeline: Inserts incoming historical price logs
     * and instantly runs a clean sweep to evict trend items older than the calculated threshold time.
     * FIX: Return Int instead of Unit to bypass the KSP2 'unexpected jvm signature V' compiler bug.
     */
    @Transaction
    suspend fun insertAndPruneHistory(history: List<CryptoHistoryRoomEntity>, thresholdTimestamp: Long): Int {
        insertCryptoHistory(history)
        val rowsWiped = pruneHistoryOlderThan(thresholdTimestamp)
        if (rowsWiped > 0) {
            println("🧹 [Room SQLite] Automated Pruning Sweeper completed. Cleared $rowsWiped historical data points older than 24h.")
        }
        return rowsWiped // ◄── RETURN THE INT WORKSPACE TOKEN DIRECTLY
    }
}
