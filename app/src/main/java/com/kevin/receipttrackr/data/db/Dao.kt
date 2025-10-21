package com.kevin.receipttrackr.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ReceiptDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReceipt(receipt: Receipt): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLineItems(items: List<LineItem>)

    @Transaction
    suspend fun insertReceiptWithItems(receipt: Receipt, items: List<LineItem>): Long {
        val receiptId = insertReceipt(receipt)
        val itemsWithReceiptId = items.map { it.copy(receiptId = receiptId) }
        insertLineItems(itemsWithReceiptId)
        return receiptId
    }

    @Query("SELECT * FROM receipts ORDER BY dateEpochMs DESC")
    fun getAllReceipts(): Flow<List<Receipt>>

    @Query("SELECT * FROM receipts WHERE id = :receiptId")
    suspend fun getReceiptById(receiptId: Long): Receipt?

    @Query("SELECT * FROM line_items WHERE receiptId = :receiptId")
    suspend fun getLineItemsForReceipt(receiptId: Long): List<LineItem>

    @Transaction
    suspend fun getReceiptWithItems(receiptId: Long): ReceiptWithItems? {
        val receipt = getReceiptById(receiptId) ?: return null
        val items = getLineItemsForReceipt(receiptId)
        return ReceiptWithItems(receipt, items)
    }

    @Query("""
        SELECT category, SUM(amountCents) as total 
        FROM line_items 
        WHERE receiptId IN (
            SELECT id FROM receipts 
            WHERE dateEpochMs >= :startMs AND dateEpochMs <= :endMs
        )
        GROUP BY category
    """)
    fun getMonthlyCategoryTotals(startMs: Long, endMs: Long): Flow<List<CategoryTotal>>

    @Query("SELECT * FROM receipts ORDER BY createdAtEpochMs DESC LIMIT :limit")
    suspend fun getRecentReceipts(limit: Int = 5): List<Receipt>
}

data class CategoryTotal(
    val category: String,
    val total: Int
)

@Dao
interface CategoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(categories: List<Category>)

    @Query("SELECT * FROM categories ORDER BY name")
    fun getAllCategories(): Flow<List<Category>>

    @Query("SELECT * FROM categories")
    suspend fun getCategoriesList(): List<Category>
}
