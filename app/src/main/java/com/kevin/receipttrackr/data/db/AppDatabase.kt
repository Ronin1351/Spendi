package com.kevin.receipttrackr.data.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [Receipt::class, LineItem::class, Category::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun receiptDao(): ReceiptDao
    abstract fun categoryDao(): CategoryDao
}
