package com.rshea.cryptotracker.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.rshea.cryptotracker.domain.CryptoAsset
import com.rshea.cryptotracker.domain.UIResourceState
import com.rshea.cryptotracker.presentation.components.AssetSparklineChart
import kotlinx.coroutines.launch

@Composable
fun App(
    viewModel: CryptoListViewModel
) {
    val screenState by viewModel.screenState.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current

    val pagerState = rememberPagerState(pageCount = { 2 })
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(androidx.lifecycle.Lifecycle.State.STARTED) {
            viewModel.observeLiveMarketTicker()
        }
    }

    MaterialTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                containerColor = MaterialTheme.colorScheme.background,
                bottomBar = {
                    NavigationBar {
                        NavigationBarItem(
                            selected = pagerState.currentPage == 0,
                            onClick = {
                                coroutineScope.launch { pagerState.animateScrollToPage(0) }
                            },
                            icon = {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.List,
                                    contentDescription = "Market"
                                )
                            },
                            label = { Text("Market") }
                        )
                        NavigationBarItem(
                            selected = pagerState.currentPage == 1,
                            onClick = {
                                coroutineScope.launch { pagerState.animateScrollToPage(1) }
                            },
                            icon = {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = "Top Movers"
                                )
                            },
                            label = { Text("Top Movers") }
                        )
                    }
                }
            ) { innerPadding ->
                AppPagerContent(
                    screenState = screenState,
                    isRefreshing = isRefreshing,
                    pagerState = pagerState,
                    onRefresh = { viewModel.refreshMarketData() },
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
    }
}

@Composable
fun AppPagerContent(
    screenState: UIResourceState<List<CryptoAsset>>,
    isRefreshing: Boolean,
    pagerState: PagerState,
    onRefresh: () -> Unit,
    modifier: Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        Text(
            text = if (pagerState.currentPage == 0) {
                "Live Crypto Asset Board"
            } else {
                "Top Positive Movers"
            },
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(vertical = 4.dp)
        )
        Text(
            text = if (pagerState.currentPage == 0) {
                "Enterprise KMP Multi-Module Architecture"
            } else {
                "Filtered via Compose derivedStateOf"
            },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.outline,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Weight(1f) lets PullToRefreshBox & LazyColumn expand to fill the screen
        Box(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            when (val state = screenState) {
                is UIResourceState.Loading -> LoadingStateView()
                is UIResourceState.Error -> ErrorStateView(message = state.message)
                is UIResourceState.Success -> {
                    val positiveMovers by remember(state.data) {
                        derivedStateOf {
                            state.data.filter { it.isPricePositive }
                        }
                    }

                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize()
                    ) { page ->
                        val displayedAssets =
                            if (page == 0) state.data else positiveMovers
                        PullToRefreshAssetBoard(
                            cryptoAssets = displayedAssets,
                            isStale = state.isStale,
                            isRefreshing = isRefreshing,
                            onRefresh = onRefresh
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PullToRefreshAssetBoard(
    cryptoAssets: List<CryptoAsset>,
    isStale: Boolean,
    isRefreshing: Boolean,
    onRefresh: () -> Unit
) {
    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            AnimatedVisibility(visible = isStale) {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                ) {
                    Text(
                        text = "Offline Mode — Showing Cached Market Data",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }
            }

            SuccessStateView(cryptoAssets = cryptoAssets)
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
            text = "No asset loaded. Pull down or click trigger to query.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(
                items = cryptoAssets,
                key = { crypto -> crypto.id },
                contentType = { "crypto_asset_card" }
            ) { crypto ->
                Column(modifier = Modifier.fillMaxWidth()) {
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

                            AssetSparklineChart(
                                pricePoints = crypto.sparklineData,
                                isPositiveTrend = crypto.isPricePositive,
                                modifier = Modifier
                                    .padding(horizontal = 8.dp)
                                    .requiredSize(width = 84.dp, height = 36.dp)
                            )

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
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${crypto.name} (${crypto.symbol}): ${crypto.priceUsd}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = crypto.marketCapUsd,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}
