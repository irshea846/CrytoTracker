package com.rshea.cryptotracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.compose.viewModel // ◄── Crucial Android compose helper import
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.rshea.cryptotracker.data.CryptoRepositoryImpl
import com.rshea.cryptotracker.presentation.App
import com.rshea.cryptotracker.presentation.CryptoListViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        //enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            // 1. Initialize your repository implementation container dependency
            val repository = CryptoRepositoryImpl()

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

