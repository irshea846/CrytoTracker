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

    // FIX: Change return type from LongArray to List<Long>
    // This provides full cross-platform collection type safety inside commonMain metadata targets
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCryptoAssets(assets: List<CryptoAssetRoomEntity>): List<Long>

    // FIX: For database cleanup routines, force the query to return an Int representing the count of rows wiped
    @Query("DELETE FROM room_crypto_assets")
    suspend fun deleteAllCryptoAssets(): Int
}
