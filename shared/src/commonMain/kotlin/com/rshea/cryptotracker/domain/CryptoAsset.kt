package com.rshea.cryptotracker.domain

/**
 * Pure Domain Presentation Model.
 * Represents data optimized exactly for UI consumption.
 */
data class CryptoAsset(
    val id: String,
    val name: String,
    val symbol: String,
    val priceUsd: String,          // Pre-formatted currency display (e.g., "$63,500.50")
    val marketCapUsd: String,      // Pre-formatted capitalization display (e.g., "$1.27T")
    val priceChange24hText: String, // Pre-formatted percentage display (e.g., "+1.25%")
    val isPricePositive: Boolean    // Flag to let Compose choose text color (Green vs Red)
)
