package com.rshea.cryptotracker.database

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "room_crypto_history",
    foreignKeys = [
        ForeignKey(
            entity = CryptoAssetRoomEntity::class,
            parentColumns = ["id"],
            childColumns = ["assetId"],
            onDelete = ForeignKey.CASCADE // Automatic cleanup of old logs if a parent asset drops off
        )
    ],
    indices = [
        Index(value = ["assetId", "timestamp"], name = "idx_asset_timestamp") // Optimized binary lookups for high-frequency charts
    ]
)
data class CryptoHistoryRoomEntity(
    @PrimaryKey(autoGenerate = true)
    val historyId: Long = 0, // Unique increment tracking identifier
    val assetId: String,     // Parent relational linking key matching your asset ID
    val timestamp: Long,     // Chronological line snapshot moment
    val priceUsd: Double     // High-precision historical price value primitive
)
