package com.notch.dynamicislanddemo.utils

import android.util.Log

object Logger {

    private var lastLogTime: Long = System.currentTimeMillis()
    val ADSLOG = "ADSLOG"
    val TAG = "TAG_NEO"

    private const val MAX_LOG_SIZE = 4000

    private fun getLogOrigin(): String {
        val element = Throwable().stackTrace
            .firstOrNull { !it.className.contains(Logger::class.java.name) }
            ?: return ""

        val className = element.className.substringAfterLast(".").substringBefore("$")
        return "$className:${element.lineNumber}"
    }

    private fun getClickableOrigin(): String {
        val element = Throwable().stackTrace
            .firstOrNull { !it.className.contains(Logger::class.java.name) }
            ?: return ""

        return "(${element.fileName}:${element.lineNumber})"
    }

    private fun log(
        level: Int,
        tag: String,
        message: String,
        throwable: Throwable? = null
    ) {
        val currentTime = System.currentTimeMillis()
        val timeElapsed = currentTime - lastLogTime
        lastLogTime = currentTime

        val origin = getClickableOrigin()

        val fullMessage = buildString {
            append("$origin -> ${timeElapsed}ms: ")
            append(message)
            if (throwable != null) {
                append("\n")
                append(Log.getStackTraceString(throwable))
            }
        }

        printLargeLog(level, tag, fullMessage)
    }

    private fun printLargeLog(level: Int, tag: String, message: String) {
        var start = 0
        val length = message.length

        while (start < length) {
            val end = (start + MAX_LOG_SIZE).coerceAtMost(length)
            val part = message.substring(start, end)

            when (level) {
                Log.VERBOSE -> Log.v(tag, part)
                Log.DEBUG -> Log.d(tag, part)
                Log.INFO -> Log.i(tag, part)
                Log.WARN -> Log.w(tag, part)
                Log.ERROR -> Log.e(tag, part)
            }
            start = end
        }
    }

    fun v(tag: String, message: String) = log(Log.VERBOSE, tag, message)
    fun v(tag: String, message: String, t: Throwable) = log(Log.VERBOSE, tag, message, t)

    fun d(tag: String, message: String) = log(Log.DEBUG, tag, message)
    fun d(tag: String, message: String, t: Throwable) = log(Log.DEBUG, tag, message, t)

    fun i(tag: String, message: String) = log(Log.INFO, tag, message)
    fun i(tag: String, message: String, t: Throwable) = log(Log.INFO, tag, message, t)

    fun w(tag: String, message: String) = log(Log.WARN, tag, message)
    fun w(tag: String, message: String, t: Throwable) = log(Log.WARN, tag, message, t)

    fun e(tag: String, message: String) = log(Log.ERROR, tag, message)
    fun e(tag: String, message: String, t: Throwable) = log(Log.ERROR, tag, message, t)
}
