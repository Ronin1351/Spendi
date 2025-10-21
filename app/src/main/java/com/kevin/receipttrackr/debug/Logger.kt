package com.kevin.receipttrackr.debug

import android.util.Log
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue

object Logger {
    private const val MAX_LOGS = 200
    private val logs = ConcurrentLinkedQueue<LogEntry>()
    private val dateFormat = SimpleDateFormat("HH:mm:ss.SSS", Locale.US)

    data class LogEntry(
        val timestamp: Long,
        val level: String,
        val tag: String,
        val message: String
    ) {
        fun formatted(): String {
            val time = dateFormat.format(Date(timestamp))
            return "$time [$level] $tag: $message"
        }
    }

    fun init() {
        logs.clear()
    }

    fun d(tag: String, message: String) {
        log("D", tag, message)
        Log.d(tag, message)
    }

    fun e(tag: String, message: String) {
        log("E", tag, message)
        Log.e(tag, message)
    }

    fun i(tag: String, message: String) {
        log("I", tag, message)
        Log.i(tag, message)
    }

    fun w(tag: String, message: String) {
        log("W", tag, message)
        Log.w(tag, message)
    }

    private fun log(level: String, tag: String, message: String) {
        logs.add(LogEntry(System.currentTimeMillis(), level, tag, message))
        while (logs.size > MAX_LOGS) {
            logs.poll()
        }
    }

    fun getLogs(): List<LogEntry> = logs.toList()

    fun getLogsFiltered(tag: String? = null, level: String? = null): List<LogEntry> {
        return logs.filter { entry ->
            (tag == null || entry.tag == tag) && (level == null || entry.level == level)
        }
    }

    fun clear() {
        logs.clear()
    }
}
