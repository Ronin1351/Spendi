package com.kevin.receipttrackr.ui

import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.kevin.receipttrackr.debug.DebugPanelFab
import com.kevin.receipttrackr.util.Formatters
import com.kevin.receipttrackr.viewmodel.ReviewViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewScreen(
    imageUri: String,
    onSaved: (Long) -> Unit,
    onBack: () -> Unit,
    viewModel: ReviewViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(imageUri) {
        viewModel.processImage(imageUri)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Review Receipt") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    if (state is ReviewViewModel.ReviewState.Success) {
                        IconButton(
                            onClick = {
                                scope.launch {
                                    val receiptId = viewModel.saveReceipt()
                                    if (receiptId != null) {
                                        onSaved(receiptId)
                                    }
                                }
                            }
                        ) {
                            Icon(Icons.Default.Save, "Save")
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            DebugPanelFab(recentReceipts = emptyList())
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (val currentState = state) {
                is ReviewViewModel.ReviewState.Loading -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Processing image...")
                    }
                }

                is ReviewViewModel.ReviewState.Error -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            "Error: ${currentState.message}",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = onBack) {
                            Text("Go Back")
                        }
                    }
                }

                is ReviewViewModel.ReviewState.Success -> {
                    ReviewContent(
                        state = currentState,
                        onMerchantChange = { viewModel.updateMerchant(it) },
                        onItemChange = { index, item -> viewModel.updateItem(index, item) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ReviewContent(
    state: ReviewViewModel.ReviewState.Success,
    onMerchantChange: (String) -> Unit,
    onItemChange: (Int, ReviewViewModel.EditableLineItem) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Receipt image
        item {
            Card {
                AsyncImage(
                    model = Uri.parse(state.imageUri),
                    contentDescription = "Receipt",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentScale = ContentScale.Crop
                )
            }
        }

        // Merchant
        item {
            OutlinedTextField(
                value = state.merchant,
                onValueChange = onMerchantChange,
                label = { Text("Merchant") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Date & Totals
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Date: ${Formatters.formatDate(state.dateEpochMs)}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Subtotal:")
                        Text(Formatters.formatCurrency(state.subtotalCents))
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Tax:")
                        Text(Formatters.formatCurrency(state.taxCents))
                    }
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Total:", style = MaterialTheme.typography.titleMedium)
                        Text(
                            Formatters.formatCurrency(state.totalCents),
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }
        }

        // Line items
        item {
            Text(
                "Items (${state.items.size})",
                style = MaterialTheme.typography.titleMedium
            )
        }

        itemsIndexed(state.items) { index, item ->
            LineItemCard(
                item = item,
                onItemChange = { updatedItem -> onItemChange(index, updatedItem) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LineItemCard(
    item: ReviewViewModel.EditableLineItem,
    onItemChange: (ReviewViewModel.EditableLineItem) -> Unit
) {
    var showCategorySheet by remember { mutableStateOf(false) }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            OutlinedTextField(
                value = item.name,
                onValueChange = { onItemChange(item.copy(name = it)) },
                label = { Text("Name") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = item.qty.toString(),
                    onValueChange = { 
                        val qty = it.toIntOrNull() ?: 1
                        onItemChange(item.copy(qty = qty))
                    },
                    label = { Text("Qty") },
                    modifier = Modifier.weight(1f)
                )

                OutlinedTextField(
                    value = Formatters.formatCurrency(item.amountCents),
                    onValueChange = { },
                    label = { Text("Amount") },
                    modifier = Modifier.weight(1f),
                    enabled = false
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            FilterChip(
                selected = true,
                onClick = { showCategorySheet = true },
                label = { Text(item.category) }
            )
        }
    }

    if (showCategorySheet) {
        CategoryPickerSheet(
            currentCategory = item.category,
            onCategorySelected = { 
                onItemChange(item.copy(category = it))
                showCategorySheet = false
            },
            onDismiss = { showCategorySheet = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryPickerSheet(
    currentCategory: String,
    onCategorySelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val categories = listOf(
        "Groceries", "Food & Drink", "Transport", 
        "Bills & Utilities", "Health", "Shopping", "Other"
    )

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Select Category",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            categories.forEach { category ->
                TextButton(
                    onClick = { onCategorySelected(category) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        category,
                        modifier = Modifier.fillMaxWidth(),
                        style = if (category == currentCategory) {
                            MaterialTheme.typography.titleMedium
                        } else {
                            MaterialTheme.typography.bodyLarge
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
