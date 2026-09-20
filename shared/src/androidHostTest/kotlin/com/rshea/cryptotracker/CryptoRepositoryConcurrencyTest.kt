package com.rshea.cryptotracker

import app.cash.sqldelight.db.SqlDriver
import com.rshea.cryptotracker.data.CryptoRepositoryImpl
import com.rshea.cryptotracker.domain.UIResourceState
// FIXED: Replaced legacy junit.framework.TestCase with pure multiplatform assertions
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test

class CryptoRepositoryConcurrencyTest {
    private lateinit var sqlDriver: SqlDriver
    private lateinit var fakeApiService: FakeCryptoApiService
    private lateinit var repository: CryptoRepositoryImpl

    @Before
    fun setup() {
        // Injects an isolated, in-memory SQL driver to isolate execution tests
        sqlDriver = createInMemoryTestDriver()
        fakeApiService = FakeCryptoApiService()
        repository = CryptoRepositoryImpl(sqlDriver, fakeApiService)
    }

    @After
    fun tearDown() {
        sqlDriver.close()
    }

    @Test
    fun testRepository_ConcurrentNetworkFetches_ExecutesSequentiallyViaMutex() = runTest {
        val concurrentWorkerCount = 10

        // Act: Launch 10 simultaneous asynchronous background coroutines to bombard the write path
        val deferredResults = List(concurrentWorkerCount) {
            async {
                repository.getTrackedCryptoAssets()
            }
        }

        val results = deferredResults.awaitAll()

        // Assert 1: Verify all 10 parallel workers resolve with a successful domain model state
        assertEquals(concurrentWorkerCount, results.size)
        results.forEach { state ->
            assertTrue(state is UIResourceState.Success)
        }

        // Assert 2: Prove that only 1 network fetch hit the network card under heavy contention!
        // Because your Mutex locks out concurrent entries, subsequent calls immediately read the database cache cache!
        assertEquals(1, fakeApiService.getFetchInvocationCount())

    }
}