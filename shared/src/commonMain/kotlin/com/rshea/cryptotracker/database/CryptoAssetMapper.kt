package com.rshea.cryptotracker.database

import com.rshea.cryptotracker.domain.CryptoAsset
import com.rshea.cryptotracker.domain.DataPoint
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
        isPricePositive = this.changePercent24Hr >= 0.0,

        // === WEEK 13 DISPLAY FORMATTING TRANSLATIONS === //
        volumeUsd24Hr = this.volumeUsd24Hr.toString(),
        totalCirculatingSupply = this.totalCirculatingSupply.toString(),
        priceChangeAbsoluteText = "${if (this.priceChangeAbsolute >= 0) "+" else ""}${this.priceChangeAbsolute}"

    )
}

// Maps entire storage collection streams seamlessly without allocating heavy loop objects
fun List<CryptoAssetRoomEntity>.toCryptoAssetList(): List<CryptoAsset> {
    return this.map { it.toCryptoAsset() }
}

/**
 * Translates a composite Room relational joint model package into a fully formatted
 * display-ready Domain presentation model, complete with mapped UI chart vectors.
 */
fun CryptoAssetWithHistory.toCryptoAssetWithChart(): CryptoAsset {
    // 1. Transform the child timeseries storage rows into unboxed UI chart data points
    val uiDataPoints = this.history.map { historyEntity ->
        DataPoint(
            x = historyEntity.timestamp.toFloat(), // Maps the raw timeline epoch primitive
            y = historyEntity.priceUsd.toFloat(),  // Maps the historical price coordinate primitive
            textLabel = "" // Let's keep this clean; we can populate formatted labels if needed
        )
    }

    // 2. Map the parent asset entity attributes directly using your optimized formatting tokens
    return CryptoAsset(
        id = this.asset.id,
        symbol = this.asset.symbol,
        name = this.asset.name,
        priceUsd = this.asset.priceUsd.toString(),
        marketCapUsd = this.asset.marketCapUsd.toString(),
        priceChange24hText = "${if (this.asset.changePercent24Hr >= 0) "+" else ""}${((this.asset.changePercent24Hr * 100).roundToInt() / 100.0)}%",
        isPricePositive = this.asset.changePercent24Hr >= 0.0,
        volumeUsd24Hr = this.asset.volumeUsd24Hr.toString(),
        totalCirculatingSupply = this.asset.totalCirculatingSupply.toString(),
        priceChangeAbsoluteText = "${if (this.asset.priceChangeAbsolute >= 0) "+" else ""}${this.asset.priceChangeAbsolute}"
        // Note: Make sure to verify if your main CryptoAsset constructor accepts the `dataPoints: List<DataPoint>` list property!
    )
}

/**
 * Maps composite collection relational join streams with zero-allocation looping mechanics.
 */
fun List<CryptoAssetWithHistory>.toCryptoAssetWithChartList(): List<CryptoAsset> {
    return this.map { it.toCryptoAssetWithChart() }
}

