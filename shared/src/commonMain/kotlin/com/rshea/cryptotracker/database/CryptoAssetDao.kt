package com.rshea.cryptotracker.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CryptoAssetDao {

    // Exposes a live, asynchronous SQLite tracking stream for all crypto assets
    @Query("SELECT * FROM room_crypto_assets ORDER BY rank ASC")
    fun getAllCryptoAssetsFlow(): Flow<List<CryptoAssetRoomEntity>>

    // Fetches a single asset out of the database for the detailed card presentation view
    @Query("SELECT * FROM room_crypto_assets WHERE id = :id LIMIT 1")
    suspend fun getCryptoAssetById(id: String): CryptoAssetRoomEntity?

    // Upserts network packets natively, replacing stale lines on ID conflict matching
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCryptoAssets(assets: List<CryptoAssetRoomEntity>)

    // Flushes out storage rows cleanly during clear cache/refresh passes
    @Query("DELETE FROM room_crypto_assets")
    suspend fun deleteAllCryptoAssets()
}
