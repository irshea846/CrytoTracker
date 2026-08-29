package com.rshea.cryptotracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rshea.cryptotracker.data.CryptoApiService
import com.rshea.cryptotracker.data.CryptoRepositoryImpl
import com.rshea.cryptotracker.database.DatabaseDriverFactory
import com.rshea.cryptotracker.presentation.App
import com.rshea.cryptotracker.presentation.CryptoListViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val androidDriverFactory = DatabaseDriverFactory(applicationContext)

        val apiService = CryptoApiService()

        setContent {
            // 1. Initialize your repository implementation container dependency
            val repository = CryptoRepositoryImpl(androidDriverFactory.createDriver(), apiService)

            // 2. Use Android's standard retained viewmodel factory block
            // This ensures your ViewModel survives native screen rotations!
            val cryptoViewModel: CryptoListViewModel = viewModel {
                CryptoListViewModel(repository = repository)
            }

            // 3. FIXED: Inject the initialized ViewModel straight into your shared UI entry point!
            App(viewModel = cryptoViewModel)
        }
    }
}

