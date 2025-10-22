package com.kevin.receipttrackr.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kevin.receipttrackr.data.db.LineItem
import com.kevin.receipttrackr.data.db.ParsedLineItem
import com.kevin.receipttrackr.data.db.Receipt
import com.kevin.receipttrackr.data.repo.ReceiptRepository
import com.kevin.receipttrackr.debug.DebugState
import com.kevin.receipttrackr.debug.Logger
import com.kevin.receipttrackr.domain.Parser
import com.kevin.receipttrackr.ocr.OcrEngine
import com.kevin.receipttrackr.settings.SettingsStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReviewViewModel @Inject constructor(
    private val ocrEngine: OcrEngine,
    private val parser: Parser,
    private val repository: ReceiptRepository,
    private val settingsStore: SettingsStore
) : ViewModel() {

    private val tag = "ReviewViewModel"

    private val _state = MutableStateFlow<ReviewState>(ReviewState.Loading)
    val state: StateFlow<ReviewState> = _state.asStateFlow()

    sealed class ReviewState {
        object Loading : ReviewState()
        data class Success(
            val merchant: String,
            val dateEpochMs: Long,
            val subtotalCents: Int,
            val taxCents: Int,
            val totalCents: Int,
            val items: List<EditableLineItem>,
            val imageUri: String,
            val rawOcrText: String
        ) : ReviewState()
        data class Error(val message: String) : ReviewState()
    }

    data class EditableLineItem(
        val name: String,
        val qty: Int,
        val amountCents: Int,
        val category: String
    )

    fun processImage(imageUri: String) {
        viewModelScope.launch {
            try {
                _state.value = ReviewState.Loading
                Logger.d(tag, "Starting OCR for $imageUri")

                val uri = Uri.parse(imageUri)
                val ocrResult = ocrEngine.processImage(uri)

                // Update debug state
                DebugState.lastOcrBitmap = ocrResult.bitmap
                DebugState.lastOcrResult = ocrResult.text
                DebugState.lastOcrBlockCount = ocrResult.blockCount
                DebugState.lastOcrTimeMs = ocrResult.processingTimeMs

                if (ocrResult.text.isBlank()) {
                    _state.value = ReviewState.Error("No text found in image")
                    return@launch
                }

                Logger.d(tag, "Parsing OCR result")
                val parsed = parser.parse(ocrResult.text)

                // Update debug state
                DebugState.lastParsedReceipt = parsed

                val currency = settingsStore.currency.first()

                // Calculate total with proper fallback
                val calculatedTotal = parsed.totalCents ?: run {
                    val subtotal = parsed.subtotalCents ?: parsed.items.sumOf { it.amountCents }
                    val tax = parsed.taxCents ?: 0
                    subtotal + tax
                }

                _state.value = ReviewState.Success(
                    merchant = parsed.merchant,
                    dateEpochMs = parsed.dateEpochMs ?: System.currentTimeMillis(),
                    subtotalCents = parsed.subtotalCents ?: parsed.items.sumOf { it.amountCents },
                    taxCents = parsed.taxCents ?: 0,
                    totalCents = calculatedTotal,
                    items = parsed.items.map { it.toEditable() },
                    imageUri = imageUri,
                    rawOcrText = ocrResult.text
                )

                Logger.d(tag, "Review state updated successfully")

            } catch (e: Exception) {
                Logger.e(tag, "Error processing image: ${e.message}")
                _state.value = ReviewState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun updateMerchant(merchant: String) {
        val current = _state.value
        if (current is ReviewState.Success) {
            _state.value = current.copy(merchant = merchant)
        }
    }

    fun updateItem(index: Int, item: EditableLineItem) {
        val current = _state.value
        if (current is ReviewState.Success) {
            val updatedItems = current.items.toMutableList()
            updatedItems[index] = item
            _state.value = current.copy(items = updatedItems)
        }
    }

    suspend fun saveReceipt(): Long? {
        val current = _state.value
        if (current !is ReviewState.Success) return null

        return try {
            val currency = settingsStore.currency.first()

            val receipt = Receipt(
                merchant = current.merchant,
                dateEpochMs = current.dateEpochMs,
                subtotalCents = current.subtotalCents,
                taxCents = current.taxCents,
                totalCents = current.totalCents,
                currency = currency,
                imageUri = current.imageUri
            )

            val lineItems = current.items.map { item ->
                LineItem(
                    receiptId = 0, // Will be set by repository
                    name = item.name,
                    qty = item.qty.coerceAtLeast(1), // Ensure qty is at least 1
                    unitCents = if (item.qty > 0) item.amountCents / item.qty else item.amountCents,
                    amountCents = item.amountCents,
                    category = item.category,
                    rawText = item.name // Use name as raw text for now
                )
            }

            val receiptId = repository.insertReceiptWithItems(receipt, lineItems)
            Logger.d(tag, "Receipt saved with ID: $receiptId")
            receiptId

        } catch (e: Exception) {
            Logger.e(tag, "Error saving receipt: ${e.message}")
            null
        }
    }

    private fun ParsedLineItem.toEditable() = EditableLineItem(
        name = name,
        qty = qty,
        amountCents = amountCents,
        category = category
    )
}
