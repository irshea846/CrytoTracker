package com.rshea.cryptotracker.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rshea.cryptotracker.domain.CryptoAsset
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
            ) {
                // 1. Structured Corporate Header Layout
                Text(
                    text = "Live Crypto Asset Board",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
                Text(
                    text = "Enterprise KMP Multi-Module Architecture",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.padding(vertical = 16.dp)
                )

                // 2. State-Driven Adaptive Content Container
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    when (val state = screenState) {
                        is UIResourceState.Loading -> LoadingStateView()
                        is UIResourceState.Error -> ErrorStateView(message = state.message)
                        is UIResourceState.Success -> SuccessStateView(cryptoAssets = state.data)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 3. Persistent Action Execution Hook
                Button(
                    onClick = { viewModel.loadCryptoMarketData() },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    enabled = screenState !is UIResourceState.Loading
                ) {
                    Text(
                        text = if (screenState is UIResourceState.Loading) "Query Remote Nodes..."
                                    else "Refresh Market Data",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun LoadingStateView() {
    CircularProgressIndicator(
        color = MaterialTheme.colorScheme.primary,
        strokeWidth = 4.dp
    )
}

@Composable
fun ErrorStateView(message: String) {
    Text(
        text = message,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.error,
        fontWeight = FontWeight.SemiBold
    )
}

@Composable
fun SuccessStateView(cryptoAssets: List<CryptoAsset>) {
    if (cryptoAssets.isEmpty()) {
        Text(
            text = "No asset loaded. Click trigger to query.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(cryptoAssets) { crypto ->
                // Polish Card Component Wrapper
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = crypto.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = crypto.symbol,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = crypto.priceUsd,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = crypto.priceChange24hText,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (crypto.isPricePositive)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
                Text(text = "${crypto.name} (${crypto.symbol}): ${crypto.priceUsd}")
            }
        }
    }
}
