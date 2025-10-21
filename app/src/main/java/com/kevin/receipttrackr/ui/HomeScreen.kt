package com.kevin.receipttrackr.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kevin.receipttrackr.debug.DebugPanelFab
import com.kevin.receipttrackr.util.Formatters
import com.kevin.receipttrackr.viewmodel.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onImportClick: () -> Unit,
    onReceiptClick: (Long) -> Unit,
    onSettingsClick: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val receipts by viewModel.allReceipts.collectAsState()
    val categoryTotals by viewModel.monthlyCategoryTotals.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("ReceiptTrackr") },
                actions = {
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Default.Settings, "Settings")
                    }
                }
            )
        },
        floatingActionButton = {
            Box {
                FloatingActionButton(onClick = onImportClick) {
                    Icon(Icons.Default.Add, "Import Receipt")
                }
                // Debug panel positioned separately
                Box(modifier = Modifier.align(Alignment.TopEnd)) {
                    DebugPanelFab(
                        recentReceipts = receipts.take(5).map { 
                            "ID: ${it.id}, ${it.merchant}, ${Formatters.formatDate(it.dateEpochMs)}"
                        }
                    )
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // Monthly summary
            Text(
                "This Month",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            if (categoryTotals.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        categoryTotals.forEach { total ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(total.category)
                                Text(
                                    Formatters.formatCurrency(total.total),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Total", style = MaterialTheme.typography.titleMedium)
                            Text(
                                Formatters.formatCurrency(categoryTotals.sumOf { it.total }),
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                }
            } else {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No receipts this month")
                    }
                }
            }

            // Recent receipts
            Text(
                "Recent Receipts",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            if (receipts.isEmpty()) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No receipts yet. Tap + to import one!")
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(receipts) { receipt ->
                        ReceiptCard(
                            receipt = receipt,
                            onClick = { onReceiptClick(receipt.id) }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReceiptCard(
    receipt: com.kevin.receipttrackr.data.db.Receipt,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = receipt.merchant,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = Formatters.formatDate(receipt.dateEpochMs),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
            Text(
                text = Formatters.formatCurrency(receipt.totalCents),
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}
