package com.rshea.cryptotracker.database

import com.rshea.cryptotracker.domain.CryptoAsset
import kotlin.math.roundToInt

// Translates a Room database storage row into your exact UI display domain model
fun CryptoAssetRoomEntity.toCryptoAsset(): CryptoAsset {
    return CryptoAsset(
        id = this.id,
        symbol = this.symbol,
        name = this.name,
        // FIX: Convert Double storage primitives into the Strings expected by the Domain constructor
        priceUsd = this.priceUsd.toString(),
        marketCapUsd = this.marketCapUsd.toString(),
        // FIX: Derive the required text field from the raw 24hr percentage change primitive
        priceChange24hText = "${if (this.changePercent24Hr >= 0) "+" else ""}${((this.changePercent24Hr * 100).roundToInt() / 100.0)}%",
        // FIX: Evaluate the raw numeric change state into a clear business Boolean indicator
        isPricePositive = this.changePercent24Hr >= 0.0
    )
}

// Maps entire storage collection streams seamlessly without allocating heavy loop objects
fun List<CryptoAssetRoomEntity>.toCryptoAssetList(): List<CryptoAsset> {
    return this.map { it.toCryptoAsset() }
}
