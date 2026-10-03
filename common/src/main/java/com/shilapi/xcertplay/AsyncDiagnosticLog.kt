package com.shilapi.xcertplay

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Queued entries retain only their exact file target and a redacted, bounded metadata string. */
internal object AsyncDiagnosticLog {
    private data class Entry(
        val target: SessionLogFile,
        val message: String,
        val nowMillis: Long,
    )
    private val writer = BoundedDiagnosticWriter<Entry> { entry ->
        val timestamp = SimpleDateFormat("HH:mm:ss.SSS", Locale.US).format(Date(entry.nowMillis))
        entry.target.append("$timestamp  ${entry.message}")
    }

    fun append(target: SessionLogFile?, message: String, nowMillis: Long = System.currentTimeMillis()) {
        if (target == null) return
        val safe = DiagnosticRedactor.redact(message) ?: return
        runCatching { writer.enqueue(Entry(target, safe, nowMillis)) }
    }

    internal fun awaitIdle(timeoutMillis: Long): Boolean = writer.awaitIdle(timeoutMillis)
}
