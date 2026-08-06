package com.rshea.cryptotracker.domain

import kotlinx.serialization.Serializable

/**
 * 2. Data Transfer Object (DTO) matching the API response structure
 */
@Serializable
data class CryptoAssetDto(
    val id: String,
    val symbol: String,
    val name: String,
    val current_price: Double,
    val market_cap: Double,
    val price_change_percentage_24h: Double
)
