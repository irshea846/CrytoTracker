package com.rshea.cryptotracker.data

import com.rshea.cryptotracker.domain.CryptoAssetDto
import kotlin.test.Test
import kotlin.test.assertEquals

class CryptoAssetMapperTest {

    @Test
    fun testMapper_TransformsBulkListPayload_ToFormattedDomainModelList() {
        // 1. Arrange: Instantiate an array collection list holding your raw mock objects
        val mockDtoList = listOf(
            CryptoAssetDto(
                id = "bitcoin",
                symbol = "btc",
                name = "Bitcoin",
                current_price = 62000.50,
                market_cap = 1200000000000.0,
                price_change_percentage_24h = 2.45
            )
        )

        // 2. Act: Execute your collection extension transformation line
        val domainList = mockDtoList.toDomainModelList()

        // 3. Assert: Verify structural matrix sizing and formatting match parameters
        assertEquals(1, domainList.size)

        val bitcoinCard = domainList.first()
        assertEquals("bitcoin", bitcoinCard.id)
        assertEquals("BTC", bitcoinCard.symbol)
        assertEquals("$62000.50", bitcoinCard.priceUsd)
        assertEquals("$1.20T", bitcoinCard.marketCapUsd)
    }
}