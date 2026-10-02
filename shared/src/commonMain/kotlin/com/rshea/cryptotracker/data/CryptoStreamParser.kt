package com.rshea.cryptotracker.data

import com.rshea.cryptotracker.domain.DataPoint


object CryptoStreamParser {

    /**
     * Parses a raw text frame from the WebSocket server using high-velocity string matching.
     * Expected format: "SYMBOL,PRICE,TIMESTAMP_FLOAT" -> e.g., "BTC,64250.75,1696176000"
     *
     * @param rawText The raw incoming string ticker frame.
     * @return A clean, unboxed DataPoint ready for canvas consumption, or null if malformed.
     */
    fun parseTickerFrame(rawText: String): DataPoint? {
        if (rawText.isBlank()) return null

        try {
            // 1. Split string by delimiter without utilizing slow regex evaluation trees
            val segments = rawText.split(",")
            if (segments.size < 3) return null

            val symbol = segments[0]
            val priceFloat = segments[1].toFloatOrNull() ?: return null
            val timeFloat = segments[2].toFloatOrNull() ?: return null

            // 2. Map coordinates cleanly straight into our unboxed domain data container
            return DataPoint(
                timeFloat,
                priceFloat,
                "$symbol: \$${segments[1]}"
            )
        } catch (e: Exception) {
            println("⚠️ [Parser] Failed to parse high-velocity string frame: ${e.message}")
            return null
        }
    }
}