package com.kevin.receipttrackr.ui

import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.compose.AsyncImage
import com.kevin.receipttrackr.data.db.ReceiptWithItems
import com.kevin.receipttrackr.data.repo.ReceiptRepository
import com.kevin.receipttrackr.debug.DebugPanelFab
import com.kevin.receipttrackr.util.Formatters
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailsViewModel @Inject constructor(
    private val repository: ReceiptRepository
) : ViewModel() {
    private val _receipt = MutableStateFlow<ReceiptWithItems?>(null)
    val receipt: StateFlow<ReceiptWithItems?> = _receipt.asStateFlow()

    fun loadReceipt(receiptId: Long) {
        viewModelScope.launch {
            _receipt.value = repository.getReceiptWithItems(receiptId)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailsScreen(
    receiptId: Long,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    viewModel: DetailsViewModel = hiltViewModel()
) {
    val receipt by viewModel.receipt.collectAsState()

    LaunchedEffect(receiptId) {
        viewModel.loadReceipt(receiptId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Receipt Details") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, "Edit")
                    }
                }
            )
        },
        floatingActionButton = {
            DebugPanelFab(recentReceipts = emptyList())
        }
    ) { padding ->
        if (receipt == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            DetailsContent(
                receipt = receipt!!,
                modifier = Modifier.padding(padding)
            )
        }
    }
}

@Composable
private fun DetailsContent(
    receipt: ReceiptWithItems,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Receipt image
        item {
            Card {
                AsyncImage(
                    model = Uri.parse(receipt.receipt.imageUri),
                    contentDescription = "Receipt",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentScale = ContentScale.Crop
                )
            }
        }

        // Merchant and date
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        receipt.receipt.merchant,
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Text(
                        Formatters.formatDate(receipt.receipt.dateEpochMs),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        // Totals
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Subtotal:")
                        Text(Formatters.formatCurrency(receipt.receipt.subtotalCents))
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Tax:")
                        Text(Formatters.formatCurrency(receipt.receipt.taxCents))
                    }
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Total:", style = MaterialTheme.typography.titleMedium)
                        Text(
                            Formatters.formatCurrency(receipt.receipt.totalCents),
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }
        }

        // Line items
        item {
            Text(
                "Items (${receipt.items.size})",
                style = MaterialTheme.typography.titleMedium
            )
        }

        items(receipt.items) { item ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(item.name, style = MaterialTheme.typography.bodyLarge)
                            Text(
                                "${item.category} • Qty: ${item.qty}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        Text(
                            Formatters.formatCurrency(item.amountCents),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        }
    }
}
