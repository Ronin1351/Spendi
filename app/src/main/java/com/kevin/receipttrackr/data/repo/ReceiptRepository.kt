package com.kevin.receipttrackr.data.repo

import com.kevin.receipttrackr.data.db.*
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReceiptRepository @Inject constructor(
    private val receiptDao: ReceiptDao,
    private val categoryDao: CategoryDao
) {
    fun getAllReceipts(): Flow<List<Receipt>> = receiptDao.getAllReceipts()

    suspend fun getReceiptWithItems(receiptId: Long): ReceiptWithItems? {
        return receiptDao.getReceiptWithItems(receiptId)
    }

    suspend fun insertReceiptWithItems(receipt: Receipt, items: List<LineItem>): Long {
        return receiptDao.insertReceiptWithItems(receipt, items)
    }

    fun getMonthlyCategoryTotals(startMs: Long, endMs: Long): Flow<List<CategoryTotal>> {
        return receiptDao.getMonthlyCategoryTotals(startMs, endMs)
    }

    fun getAllCategories(): Flow<List<Category>> = categoryDao.getAllCategories()

    suspend fun getCategoriesList(): List<Category> = categoryDao.getCategoriesList()

    suspend fun seedCategories() {
        val existingCategories = getCategoriesList()
        if (existingCategories.isEmpty()) {
            val defaultCategories = listOf(
                Category(name = "Groceries", color = 0xFF4CAF50),
                Category(name = "Food & Drink", color = 0xFFFF9800),
                Category(name = "Transport", color = 0xFF2196F3),
                Category(name = "Bills & Utilities", color = 0xFFF44336),
                Category(name = "Health", color = 0xFFE91E63),
                Category(name = "Shopping", color = 0xFF9C27B0),
                Category(name = "Other", color = 0xFF607D8B)
            )
            categoryDao.insertAll(defaultCategories)
        }
    }

    suspend fun getRecentReceipts(limit: Int = 5): List<Receipt> {
        return receiptDao.getRecentReceipts(limit)
    }
}
