package com.kevin.receipttrackr.data.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "receipts")
data class Receipt(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val merchant: String,
    val dateEpochMs: Long,
    val subtotalCents: Int,
    val taxCents: Int,
    val totalCents: Int,
    val currency: String = "USD",
    val imageUri: String,
    val createdAtEpochMs: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "line_items",
    foreignKeys = [
        ForeignKey(
            entity = Receipt::class,
            parentColumns = ["id"],
            childColumns = ["receiptId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("receiptId")]
)
data class LineItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val receiptId: Long,
    val name: String,
    val qty: Int = 1,
    val unitCents: Int,
    val amountCents: Int,
    val category: String,
    val rawText: String
)

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val color: Long // ARGB color
)

// Domain models for UI
data class ReceiptWithItems(
    val receipt: Receipt,
    val items: List<LineItem>
)

data class ParsedReceipt(
    val merchant: String,
    val dateEpochMs: Long?,
    val subtotalCents: Int?,
    val taxCents: Int?,
    val totalCents: Int?,
    val items: List<ParsedLineItem>,
    val rawText: String
)

data class ParsedLineItem(
    val name: String,
    val qty: Int,
    val amountCents: Int,
    val category: String,
    val rawText: String
)
