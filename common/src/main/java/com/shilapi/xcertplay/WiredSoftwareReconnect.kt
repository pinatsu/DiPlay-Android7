package com.shilapi.xcertplay

import java.util.concurrent.atomic.AtomicBoolean

/** Process-local evidence that the next wired start replaces an existing wired software session. */
internal object WiredSoftwareReconnect {
    private val pending = AtomicBoolean(false)

    /** Wireless transitions and first/physical USB connections do not need a synthetic reset. */
    fun prepareForRestart(currentTransportWireless: Boolean, nextTransportWireless: Boolean) {
        pending.set(!currentTransportWireless && !nextTransportWireless)
    }

    fun isPending(): Boolean = pending.get()

    fun connected() {
        pending.set(false)
    }
}
