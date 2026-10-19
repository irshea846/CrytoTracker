package com.rshea.cryptotracker.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "room_crypto_assets") // ◄── Explicit room prefix isolates the SQLite table space
data class CryptoAssetRoomEntity(
    @PrimaryKey
    val id: String, // Unique identifier matching your Ktor DTO strings
    val rank: Int,
    val name: String,
    val symbol: String,
    val marketCapUsd: Double,
    val priceUsd: Double,
    val changePercent24Hr: Double,
    val supply: Double,
    val maxSupply: Double?, // Nullable mapping for uncapped assets
    val lastUpdatedTimestamp: Long // Crucial for tracking offline sync offsets
)
