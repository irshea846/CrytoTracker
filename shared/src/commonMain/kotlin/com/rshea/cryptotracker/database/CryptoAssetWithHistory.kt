package com.rshea.cryptotracker.database

import androidx.room.Embedded
import androidx.room.Relation

/**
 * Composite data shape mapping a 1-to-Many relational join query natively in Room.
 * The presentation layer consumes this package to render synchronized trend lines.
 */
data class CryptoAssetWithHistory(
    @Embedded
    val asset: CryptoAssetRoomEntity, // Mounts the parent shell entity attributes directly

    @Relation(
        parentColumn = "id",
        entityColumn = "assetId"
    )
    val history: List<CryptoHistoryRoomEntity> // Automatically groups downstream chronological tick rows
)