package com.rshea.cryptotracker.data

import com.rshea.cryptotracker.database.CryptoAssetEntity
import com.rshea.cryptotracker.domain.CryptoAssetDto
import com.rshea.cryptotracker.domain.CryptoAsset
import com.rshea.cryptotracker.util.formatToCap
import com.rshea.cryptotracker.util.formatToTwoDecimals
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Extension mapper to translate raw, volatile API Data Transfer Objects
 * into stable, pre-formatted Domain Models.
 */

fun CryptoAssetDto.toDomainModel(): CryptoAsset {
    // 1. Format the current price string seamlessly using common math helper
    val formattedPrice = "$${this.current_price.formatToTwoDecimals()}"

    // The Bug: Using .toString().take(4) on a calculated number doesn't look at the decimal point.
    // If your division results in 1.2345, .take(4) turns it into 1.23 (which looks fine).
    // But if your division results in 123.45, .take(4) turns it into 123.
    // (leaving a broken trailing decimal point on the user's screen).

    // The Senior Fix: Bypassing manual string splitting and leveraging an explicit extension
    // function is the cleaner path. Since Java's String.format() isn't fully available in Kotlin
    // Multiplatform common code yet, the cleanest KMP-native way to round a Double to exactly two
    // decimal places is to use a simple math helper:
    // 2. Format Market Cap Text into readable shorthand notations (Billions/Trillions)
    val formattedCap = this.market_cap.formatToCap()

    // 3. Determine positive trend state metrics
    val isPositive = this.price_change_percentage_24h >= 0.0
    val prefix = if (isPositive) "+" else "-"
    val formattedPercentage = "$prefix${abs(this.price_change_percentage_24h).toString().take(4)}%"

    return CryptoAsset(
        id = this.id,
        name = this.name,
        symbol = this.symbol.uppercase(),
        priceUsd = formattedPrice,
        marketCapUsd = formattedCap,
        priceChange24hText = formattedPercentage,
        isPricePositive = isPositive
    )
}

/**
 * Extension mapper to translate local SQLite Database Entities
 * into stable, pre-formatted Domain Models for UI consumption.
 */
fun CryptoAssetEntity.toDomainModel(): CryptoAsset {
    return CryptoAsset(
        id = this.id,
        symbol = this.symbol,
        name = this.name,
        priceUsd = "$${this.priceUsd.formatToTwoDecimals()}",
        marketCapUsd = this.marketCapUsd.formatToCap(),
        priceChange24hText = this.priceChange24hText,
        isPricePositive = this.isPricePositive == 1L
    )
}

/**
 * Helper collection mapper to transform lists of network payloads in a single pass.
 */
fun List<CryptoAssetDto>.toDomainModelList(): List<CryptoAsset> {
    return this.map { it.toDomainModel() }
}

/**
 * Helper collection mapper to transform lists of database entities in a single pass.
 */
fun List<CryptoAssetEntity>.toDomainModelListFromEntities(): List<CryptoAsset> {
    return this.map { it.toDomainModel() }
}
