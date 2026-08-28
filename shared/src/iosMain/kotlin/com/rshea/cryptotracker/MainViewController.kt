package com.rshea.cryptotracker

import androidx.compose.ui.window.ComposeUIViewController
import com.rshea.cryptotracker.data.CryptoApiService
import com.rshea.cryptotracker.data.CryptoRepositoryImpl
import com.rshea.cryptotracker.database.DatabaseDriverFactory // ◄── Your clean iOS interface driver
import com.rshea.cryptotracker.presentation.App
import com.rshea.cryptotracker.presentation.CryptoListViewModel
import platform.UIKit.UIViewController

fun MainViewController(): UIViewController = ComposeUIViewController {

    // 1. Instantiate your native iOS concrete SQLite driver factory
    val iosDriverFactory = DatabaseDriverFactory()

    // 2. Explicitly instantiate your standard Ktor networking api pipeline
    val apiService = CryptoApiService()

    // 3. Inject BOTH components directly into your cross-platform repository constructor!
    val repository = CryptoRepositoryImpl(
        iosDriverFactory,
        apiService
    )

    // 4. Pass your wired data tier up into your architecture ViewModel orchestrator
    val cryptoViewModel = CryptoListViewModel(repository = repository)

    // 5. Load your core cross-platform view tree container layout parameters
    App(viewModel = cryptoViewModel)
}