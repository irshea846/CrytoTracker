package com.rshea.cryptotracker

import com.rshea.cryptotracker.domain.CryptoAsset
import com.rshea.cryptotracker.domain.CryptoRepository
import com.rshea.cryptotracker.domain.UIResourceState
import com.rshea.cryptotracker.presentation.CryptoListViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class CryptoListViewModelTest {

    // 1. Set up a dedicated single-threaded testing coroutine dispatcher
    private val testDispatcher = StandardTestDispatcher()

    // 2. Create a lightweight local Mock Repository to control data returns deterministically
    private class FakeCryptoRepository : CryptoRepository {
        var shouldReturnError = false
        val mockDataList = listOf(
            CryptoAsset(
                id = "sol",
                symbol = "SOL",
                name = "Solana",
                priceUsd = "$140.00",
                marketCapUsd = "$65B",
                priceChange24hText = "+4.2%",
                isPricePositive = true
            )
        )

        override suspend fun getTrackedCryptoAssets(): UIResourceState<List<CryptoAsset>> {
            return if (shouldReturnError) {
                UIResourceState.Error("Simulated Network Disruption")
            } else {
                UIResourceState.Success(mockDataList)
            }
        }

    }

    @BeforeTest
    fun setUp() {
        // Redirect Kotlin's global Main coroutine execution path straight to our test harness clock
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        // Reset the main dispatcher to prevent memory leaks across the testing lab suite
        Dispatchers.resetMain()
    }

    @Test
    fun testViewModel_StateTransition_FromLoadingToSuccess() = runTest(testDispatcher) {
        // Initialize our fake data container layer and inject it into the architecture orchestrator
        val fakeRepository = FakeCryptoRepository()
        val viewModelUnderTest = CryptoListViewModel(repository = fakeRepository)

        // ASSERTION 1: Initial state must be initialized as a flat Success container containing empty arrays
        assertTrue(viewModelUnderTest.screenState.value is UIResourceState.Success)
        assertEquals(0, (viewModelUnderTest.screenState.value as UIResourceState.Success).data.size)

        // TRIGGER ACTION: Fire the asynchronous remote data query hook
        viewModelUnderTest.loadCryptoMarketData()

        // ASSERTION 2: Instantly check the state value before running the clock. It must transition to Loading!
        assertTrue(viewModelUnderTest.screenState.value is UIResourceState.Loading)

        // Advance the coroutine execution virtual clock to let internal launch blocks evaluate
        advanceUntilIdle()

        // ASSERTION 3: Verify the stream successfully catches data and transforms into a type-safe Success state signature
        assertTrue(viewModelUnderTest.screenState.value is UIResourceState.Success)

        val parsedList = (viewModelUnderTest.screenState.value as UIResourceState.Success).data
        assertEquals(1, parsedList.size)
        assertEquals("Solana", parsedList.first().name)
    }

}