package com.shilapi.xcertplay.media

import android.media.AudioTrack
import java.io.Closeable
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.TimeUnit

/** Pure timing envelope shared by the runtime coordinator and unit tests. */
internal class NavigationDuckingEnvelope(
    private val duckVolume: Float = 0.30f,
    private val fadeDownMillis: Long = 150L,
    private val silenceMillis: Long = 500L,
    private val fadeUpMillis: Long = 300L,
) {
    private var lastNavigationAudioMillis: Long? = null
    private var fadeDownStartedMillis = 0L
    private var fadeDownFrom = 1f

    /** Returns true when this activity starts a new prompt rather than extending one. */
    fun navigationAudio(nowMillis: Long): Boolean {
        val previous = lastNavigationAudioMillis
        val startsPrompt = previous == null || nowMillis - previous > silenceMillis
        if (startsPrompt) {
            fadeDownFrom = volume(nowMillis)
            fadeDownStartedMillis = nowMillis
        }
        lastNavigationAudioMillis = nowMillis
        return startsPrompt
    }

    fun volume(nowMillis: Long): Float {
        val lastAudio = lastNavigationAudioMillis ?: return 1f
        val restoreStarted = lastAudio + silenceMillis
        return if (nowMillis <= restoreStarted) {
            interpolate(fadeDownFrom, duckVolume, nowMillis - fadeDownStartedMillis, fadeDownMillis)
        } else {
            interpolate(duckVolume, 1f, nowMillis - restoreStarted, fadeUpMillis)
        }
    }

    fun settled(nowMillis: Long): Boolean {
        val lastAudio = lastNavigationAudioMillis ?: return true
        return nowMillis >= lastAudio + silenceMillis + fadeUpMillis
    }

    /** Delay until the volume can next change, avoiding periodic work while fully ducked. */
    fun nextUpdateDelay(nowMillis: Long, rampStepMillis: Long): Long? {
        val lastAudio = lastNavigationAudioMillis ?: return null
        val fadeDownEnds = fadeDownStartedMillis + fadeDownMillis
        val restoreStarts = lastAudio + silenceMillis
        val restoreEnds = restoreStarts + fadeUpMillis
        return when {
            nowMillis < fadeDownEnds -> minOf(rampStepMillis, fadeDownEnds - nowMillis)
            nowMillis < restoreStarts -> restoreStarts - nowMillis
            nowMillis < restoreEnds -> minOf(rampStepMillis, restoreEnds - nowMillis)
            else -> null
        }
    }

    fun reset() {
        lastNavigationAudioMillis = null
        fadeDownFrom = 1f
        fadeDownStartedMillis = 0L
    }

    private fun interpolate(from: Float, to: Float, elapsedMillis: Long, durationMillis: Long): Float {
        if (durationMillis <= 0L) return to
        val fraction = (elapsedMillis.coerceAtLeast(0L).toFloat() / durationMillis).coerceIn(0f, 1f)
        return from + (to - from) * fraction
    }
}

/**
 * Applies the navigation envelope only to CarPlay media tracks. It wakes for short ramps while a
 * prompt is active and otherwise keeps no periodic work scheduled.
 */
internal class NavigationAudioDucker(
    private val enabled: Boolean,
    private val report: (String) -> Unit = {},
    private val nowMillis: () -> Long = { System.nanoTime() / 1_000_000L },
) : Closeable {
    private val envelope = NavigationDuckingEnvelope()
    private val mediaTracks = LinkedHashSet<AudioTrack>()
    private val executor = if (enabled) {
        ScheduledThreadPoolExecutor(1) { task ->
            Thread(task, "diplay-audio-duck").apply { isDaemon = true }
        }.apply {
            removeOnCancelPolicy = true
            setKeepAliveTime(1L, TimeUnit.SECONDS)
            allowCoreThreadTimeOut(true)
        }
    } else null
    private var updateScheduled = false
    private var closed = false

    @Synchronized
    fun registerMedia(track: AudioTrack) {
        if (!enabled || closed) return
        mediaTracks += track
        applyVolume(track, envelope.volume(nowMillis()))
    }

    @Synchronized
    fun unregisterMedia(track: AudioTrack) {
        mediaTracks.remove(track)
    }

    fun navigationAudio() {
        if (!enabled) return
        val started = synchronized(this) {
            if (closed) return
            val now = nowMillis()
            val promptStarted = envelope.navigationAudio(now)
            if (promptStarted) applyVolumeLocked(envelope.volume(now))
            scheduleUpdateLocked()
            promptStarted
        }
        if (started) runCatching { report("Audio: navigation duck active mediaVolume=30%") }
    }

    override fun close() {
        val tracks: List<AudioTrack>
        synchronized(this) {
            if (closed) return
            closed = true
            tracks = mediaTracks.toList()
            mediaTracks.clear()
            envelope.reset()
            updateScheduled = false
        }
        tracks.forEach { applyVolume(it, 1f) }
        executor?.shutdownNow()
    }

    private fun update() {
        var restored = false
        synchronized(this) {
            if (closed) return
            val now = nowMillis()
            applyVolumeLocked(envelope.volume(now))
            val nextDelay = envelope.nextUpdateDelay(now, UPDATE_INTERVAL_MILLIS)
            if (nextDelay == null) {
                envelope.reset()
                updateScheduled = false
                restored = true
            } else {
                executor?.schedule(::update, nextDelay, TimeUnit.MILLISECONDS)
            }
        }
        if (restored) runCatching { report("Audio: navigation duck released mediaVolume=100%") }
    }

    private fun scheduleUpdateLocked() {
        if (updateScheduled) return
        updateScheduled = true
        executor?.schedule(::update, UPDATE_INTERVAL_MILLIS, TimeUnit.MILLISECONDS)
    }

    private fun applyVolumeLocked(volume: Float) {
        mediaTracks.forEach { applyVolume(it, volume) }
    }

    private fun applyVolume(track: AudioTrack, volume: Float) {
        @Suppress("DEPRECATION")
        runCatching { track.setStereoVolume(volume, volume) }
    }

    private companion object {
        const val UPDATE_INTERVAL_MILLIS = 30L
    }
}
