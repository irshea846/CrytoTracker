package com.rshea.cryptotracker

import androidx.compose.ui.window.ComposeUIViewController
import com.rshea.cryptotracker.data.CryptoRepositoryImpl
import com.rshea.cryptotracker.presentation.App
import com.rshea.cryptotracker.presentation.CryptoListViewModel
import platform.UIKit.UIViewController

fun MainViewController(): UIViewController = ComposeUIViewController {
    // 1. Initialize the repository implementation container
    val repository = CryptoRepositoryImpl()

    // 2. Initialize a local instance for the iOS compilation frame
    val cryptoViewModel = CryptoListViewModel(repository = repository)

    // 3. FIXED: Pass it right into your shared App entrance parameter
    App(viewModel = cryptoViewModel)
}