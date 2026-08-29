package com.rshea.cryptotracker

import com.rshea.cryptotracker.data.CryptoApiService
import com.rshea.cryptotracker.data.CryptoRepositoryImpl
import com.rshea.cryptotracker.domain.UIResourceState
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest // KMP Coroutine test execution harness helper
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CryptoRepositoryIntegrationTest {

    // 1. Define a clean static mock JSON payload representing raw CoinGecko bytes
    private val mockCryptoJsonPayload = """
        [
          {
            "id": "bitcoin",
            "symbol": "btc",
            "name": "Bitcoin",
            "current_price": 62000.50,
            "market_cap": 1200000000000.0,
            "price_change_percentage_24h": 2.45
          }
        ]
    """.trimIndent()

    @Test
    fun testRepository_ReturnsFormattedDomainModel_OnSuccessfulNetworkCall() = runTest {

        // 2. Configure the Ktor MockEngine to intercept requests and stream back your static JSON bytes
        val mockEngine = MockEngine { request ->
            respond(
                content = mockCryptoJsonPayload,
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        // 3. Reconstruct a local HttpClient engine mapping kotlinx.serialization JSON rules
        val testHttpClient = HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    coerceInputValues = true
                })
            }
        }

        // 4. Inject the testing client into your architecture stream layers
        val testApiService = CryptoApiService(httpClient = testHttpClient)
        val repositoryUnderTest = CryptoRepositoryImpl(
            sqlDriver = createInMemoryTestDriver(),
            apiService = testApiService
        )

        // 5. TRIGGER EXECUTION: Pull data from the pipeline inside your test sandbox
        val resultState = repositoryUnderTest.getTrackedCryptoAssets()

        // 6. ASSERTIONS: Verify that parsing, exception mapping, and DTO-to-Domain conversions work perfectly
        assertTrue(resultState is UIResourceState.Success, "Repository must return a type-safe Success state signature")

        val cryptoList = resultState.data
        assertEquals(1, cryptoList.size, "List should parse exactly one crypto asset entry")

        val bitcoin = cryptoList.first()
        assertEquals("Bitcoin", bitcoin.name)
        assertEquals("BTC", bitcoin.symbol)
        assertEquals("${'$'}62000.5", bitcoin.priceUsd)      // Asserts that your mapper extension formatting works!
        assertEquals("1.2E12", bitcoin.marketCapUsd)    // Asserts that your shorthand currency calculation works!
        assertEquals("2.45%", bitcoin.priceChange24hText)   // Asserts that your dynamic plus/minus string assignment works!
        assertTrue(bitcoin.isPricePositive, "Price trend direction flag must validate as true")
    }

}