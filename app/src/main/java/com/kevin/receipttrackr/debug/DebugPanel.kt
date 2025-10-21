package com.kevin.receipttrackr.debug

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kevin.receipttrackr.BuildConfig
import com.kevin.receipttrackr.data.db.ParsedReceipt

object DebugState {
    var lastOcrBitmap: Bitmap? by mutableStateOf(null)
    var lastOcrResult: String by mutableStateOf("")
    var lastOcrBlockCount: Int by mutableStateOf(0)
    var lastOcrTimeMs: Long by mutableStateOf(0L)
    var lastParsedReceipt: ParsedReceipt? by mutableStateOf(null)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebugPanelFab(
    recentReceipts: List<String>,
    onDismiss: () -> Unit = {}
) {
    var showPanel by remember { mutableStateOf(false) }

    // Only show in debug builds or if explicitly enabled
    if (!BuildConfig.ENABLE_DEBUG_PANEL) return

    FloatingActionButton(
        onClick = { showPanel = true },
        modifier = Modifier.padding(16.dp)
    ) {
        Icon(Icons.Default.BugReport, "Debug")
    }

    if (showPanel) {
        ModalBottomSheet(
            onDismissRequest = {
                showPanel = false
                onDismiss()
            },
            modifier = Modifier.fillMaxHeight(0.9f)
        ) {
            DebugPanelContent(recentReceipts)
        }
    }
}

@Composable
private fun DebugPanelContent(recentReceipts: List<String>) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Logs", "OCR", "Parse", "DB")

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            "Debug Panel",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(16.dp)
        )

        TabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) }
                )
            }
        }

        when (selectedTab) {
            0 -> LogsTab()
            1 -> OcrTab()
            2 -> ParseTab()
            3 -> DbTab(recentReceipts)
        }
    }
}

@Composable
private fun LogsTab() {
    val logs = remember { Logger.getLogs() }
    var filterTag by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = filterTag,
            onValueChange = { filterTag = it },
            label = { Text("Filter by tag") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp)
        ) {
            val filteredLogs = if (filterTag.isBlank()) {
                logs
            } else {
                logs.filter { it.tag.contains(filterTag, ignoreCase = true) }
            }

            items(filteredLogs.reversed()) { log ->
                Text(
                    text = log.formatted(),
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }
        }
    }
}

@Composable
private fun OcrTab() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Last OCR Result", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))

        Text("Blocks: ${DebugState.lastOcrBlockCount}")
        Text("Processing time: ${DebugState.lastOcrTimeMs}ms")
        Text("Text length: ${DebugState.lastOcrResult.length} chars")

        Spacer(modifier = Modifier.height(16.dp))

        DebugState.lastOcrBitmap?.let { bitmap ->
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "OCR Bitmap",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("OCR Text:", style = MaterialTheme.typography.titleSmall)
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            item {
                Text(
                    text = DebugState.lastOcrResult.ifEmpty { "No OCR result yet" },
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

@Composable
private fun ParseTab() {
    val parsed = DebugState.lastParsedReceipt

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp)
    ) {
        if (parsed == null) {
            item {
                Text("No parsed receipt yet")
            }
            return@LazyColumn
        }

        item {
            Text("Parsed Receipt", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
        }

        item {
            Text("Merchant: ${parsed.merchant}")
            Text("Date: ${parsed.dateEpochMs?.let { java.util.Date(it).toString() } ?: "Not found"}")
            Text("Subtotal: ${parsed.subtotalCents?.let { "$${it / 100.0}" } ?: "Not found"}")
            Text("Tax: ${parsed.taxCents?.let { "$${it / 100.0}" } ?: "Not found"}")
            Text("Total: ${parsed.totalCents?.let { "$${it / 100.0}" } ?: "Not found"}")
            Spacer(modifier = Modifier.height(8.dp))
            Text("Items: ${parsed.items.size}")
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text("Line Items:", style = MaterialTheme.typography.titleSmall)
            Spacer(modifier = Modifier.height(8.dp))
        }

        items(parsed.items.size) { index ->
            val item = parsed.items[index]
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Text("${item.name} (${item.category})", style = MaterialTheme.typography.bodyMedium)
                    Text("Qty: ${item.qty}, Amount: $${item.amountCents / 100.0}", fontSize = 12.sp)
                    Text("Raw: ${item.rawText}", fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            val itemsTotal = parsed.items.sumOf { it.amountCents }
            val expectedTotal = (parsed.subtotalCents ?: 0) + (parsed.taxCents ?: 0)
            val actualTotal = parsed.totalCents ?: 0

            Text("Validation:", style = MaterialTheme.typography.titleSmall)
            Text("Sum of items: $${itemsTotal / 100.0}")
            Text("Subtotal + Tax: $${expectedTotal / 100.0}")
            Text("Parsed Total: $${actualTotal / 100.0}")

            if (itemsTotal != actualTotal && actualTotal != 0) {
                Text(
                    "⚠️ Mismatch: Items don't sum to total",
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun DbTab(recentReceipts: List<String>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp)
    ) {
        item {
            Text("Recent Receipts", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
        }

        items(recentReceipts) { receipt ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Text(
                    text = receipt,
                    modifier = Modifier.padding(8.dp),
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        if (recentReceipts.isEmpty()) {
            item {
                Text("No receipts in database yet")
            }
        }
    }
}
