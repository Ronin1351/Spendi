package com.kevin.receipttrackr.util

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

object Formatters {
    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale.US)
    private val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.US)
    private val monthFormat = SimpleDateFormat("MMMM yyyy", Locale.US)

    fun formatCurrency(cents: Int, currencyCode: String = "USD"): String {
        return try {
            val format = NumberFormat.getCurrencyInstance(Locale.US).apply {
                currency = Currency.getInstance(currencyCode)
            }
            format.format(cents / 100.0)
        } catch (e: Exception) {
            "$${cents / 100.0}"
        }
    }

    fun formatDate(epochMs: Long): String {
        return dateFormat.format(Date(epochMs))
    }

    fun formatMonth(epochMs: Long): String {
        return monthFormat.format(Date(epochMs))
    }

    fun getMonthStartMs(epochMs: Long = System.currentTimeMillis()): Long {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = epochMs
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return calendar.timeInMillis
    }

    fun getMonthEndMs(epochMs: Long = System.currentTimeMillis()): Long {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = epochMs
            set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }
        return calendar.timeInMillis
    }
}
