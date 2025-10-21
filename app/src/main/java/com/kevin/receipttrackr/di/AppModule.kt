package com.kevin.receipttrackr.di

import android.content.Context
import androidx.room.Room
import com.kevin.receipttrackr.data.db.AppDatabase
import com.kevin.receipttrackr.data.db.CategoryDao
import com.kevin.receipttrackr.data.db.ReceiptDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "receipt_trackr_db"
        ).build()
    }

    @Provides
    fun provideReceiptDao(database: AppDatabase): ReceiptDao {
        return database.receiptDao()
    }

    @Provides
    fun provideCategoryDao(database: AppDatabase): CategoryDao {
        return database.categoryDao()
    }

    @Provides
    @Singleton
    fun provideApplicationScope(): CoroutineScope {
        return CoroutineScope(SupervisorJob() + Dispatchers.Main)
    }
}
