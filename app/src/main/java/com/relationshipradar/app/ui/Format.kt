package com.relationshipradar.app.ui

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

object Format {
    private val date = DateTimeFormatter.ofPattern("MMM d, yyyy")
    private val dateTime = DateTimeFormatter.ofPattern("MMM d, h:mm a")

    fun date(ms: Long): String = Instant.ofEpochMilli(ms).atZone(ZoneId.systemDefault()).format(date)
    fun dateTime(ms: Long): String = Instant.ofEpochMilli(ms).atZone(ZoneId.systemDefault()).format(dateTime)

    fun ago(ms: Long?): String {
        if (ms == null) return "Never"
        val days = TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis() - ms)
        return when {
            days <= 0 -> "Today"
            days == 1L -> "Yesterday"
            days < 7 -> "$days days ago"
            days < 30 -> "${days / 7} wk ago"
            days < 365 -> "${days / 30} mo ago"
            else -> "${days / 365} yr ago"
        }
    }

    fun until(ms: Long): String {
        if (ms == Long.MAX_VALUE) return "indefinitely"
        return "until " + date(ms)
    }
}
