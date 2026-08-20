package com.rshea.cryptotracker.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rshea.cryptotracker.domain.UIResourceState

@Composable
fun App(
    // 1. Dependency Inversion: Pass the service into the screen function as a parameter boundary
    viewModel: CryptoListViewModel
) {
    MaterialTheme {
        // 2. Natively collect your StateFlow stream. UI redraws automatically on any state change!
        val screenState by viewModel.screenState.collectAsStateWithLifecycle()

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier.fillMaxSize().padding(40.dp),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Structural Header Title Layout Element
                Text(
                    text = "Crypto Tracker 2026",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(vertical = 16.dp)
                )

                // Centered Loading Spinner Container View (XML FrameLayout Equivalent)
                Box(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    when (val state = screenState) {
                        is UIResourceState.Loading -> CircularProgressIndicator()
                        is UIResourceState.Error -> Text(text = state.message, color = MaterialTheme.colorScheme.error)
                        is UIResourceState.Success -> {
                            LazyColumn {
                                items(state.data) { crypto ->
                                    // Render rows cleanly using state.data fields
                                    Text(text = "${crypto.name} (${crypto.symbol}): ${crypto.priceUsd}")
                                }
                            }
                        }
                    }
                }

                // 4. ACTION PULL BUTTON: UI triggers execution without managing asynchronous logic directly
                Button(
                    onClick = { viewModel.loadCryptoMarketData() },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    enabled = screenState !is UIResourceState.Loading
                ) {
                    Text(text = "Trigger API Pull Event")
                }
            }
        }
    }
}