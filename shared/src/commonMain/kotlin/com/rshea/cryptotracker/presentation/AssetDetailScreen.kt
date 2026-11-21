package com.rshea.cryptotracker.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rshea.cryptotracker.domain.CryptoAsset
import com.rshea.cryptotracker.domain.DataPoint
import com.rshea.cryptotracker.presentation.components.InteractiveCryptoPriceChart

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssetDetailScreen(
    asset: CryptoAsset?,
    onBackClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = asset?.let { "${it.name} (${it.symbol})" } ?: "Asset Details",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        if (asset == null) {
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text("Asset not found.")
            }
            return@Scaffold
        }

        println("🔍 [Detail] ${asset.id} history count: ${asset.sparklineData.size} -> ${asset.sparklineData}")

        val chartDataPoints = remember(asset.sparklineData) {
            asset.sparklineData.mapIndexed { index, price ->
                DataPoint(
                    x = index.toFloat(),
                    y = price.toFloat(),
                    textLabel = "$${price}"
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = asset.priceUsd,
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "24h Change: ${asset.priceChange24hText} (${asset.priceChangeAbsoluteText})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = if (asset.isPricePositive) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.error
                }
            )

            Card(
                modifier = Modifier.fillMaxWidth().height(250.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                if (chartDataPoints.size >= 2) {
                    InteractiveCryptoPriceChart(
                        dataPoints = chartDataPoints,
                        modifier = Modifier.fillMaxSize().padding(16.dp)
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Collecting live price ticks for interactive chart...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            MetricSummaryCard(label = "Market Cap", value = asset.marketCapUsd)
            MetricSummaryCard(label = "24h Trading Volume", value = asset.volumeUsd24Hr)
            MetricSummaryCard(label = "Circulating Supply", value = asset.totalCirculatingSupply)
        }
    }
}

@Composable
private fun MetricSummaryCard(label: String, value: String) {
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
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
