package com.rshea.cryptotracker.domain

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

class CryptoAssetDtoTest {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    @Test
    fun testCryptoAssetDto_DataInstantiation() {
        // Verifies your data layer can initialize models cleanly in memory
        val mockAsset = CryptoAssetDto(
            id = "solana",
            symbol = "sol",
            name = "Solana",
            current_price = 75.87,
            market_cap = 4.4166126924,
            price_change_percentage_24h = 3.1
        )

        assertEquals("Solana", mockAsset.name)
        assertEquals(75.87, mockAsset.current_price, 0.001)
        assertEquals(4.4166126924, mockAsset.market_cap, 0.001)
        assertEquals(3.1, mockAsset.price_change_percentage_24h, 0.001)
    }

    @Test
    fun testDeserialization() {
        val jsonString = """
            {
                "id": "bitcoin",
                "symbol": "btc",
                "name": "Bitcoin",
                "current_price": 65000.50,
                "market_cap": 1200000000.0,
                "price_change_percentage_24h": -1.5,
                "extra_field": "should be ignored"
            }
        """.trimIndent()

        val dto = json.decodeFromString<CryptoAssetDto>(jsonString)

        assertEquals("bitcoin", dto.id)
        assertEquals("btc", dto.symbol)
        assertEquals("Bitcoin", dto.name)
        assertEquals(65000.50, dto.current_price, 0.001)
        assertEquals(1200000000.0, dto.market_cap, 0.001)
        assertEquals(-1.5, dto.price_change_percentage_24h, 0.001)
    }

    @Test
    fun testSerialization() {
        val dto = CryptoAssetDto(
            id = "ethereum",
            symbol = "eth",
            name = "Ethereum",
            current_price = 3500.0,
            market_cap = 400000000.0,
            price_change_percentage_24h = 2.0
        )

        val jsonString = json.encodeToString(CryptoAssetDto.serializer(), dto)
        
        // Verify we can decode it back to the same object
        val decoded = json.decodeFromString<CryptoAssetDto>(jsonString)
        assertEquals(dto, decoded)
    }

    @Test
    fun testDataClassEquality() {
        val dto1 = CryptoAssetDto("id", "sym", "name", 1.0, 2.0, 3.0)
        val dto2 = CryptoAssetDto("id", "sym", "name", 1.0, 2.0, 3.0)
        
        assertEquals(dto1, dto2)
        assertEquals(dto1.hashCode(), dto2.hashCode())
    }
}
