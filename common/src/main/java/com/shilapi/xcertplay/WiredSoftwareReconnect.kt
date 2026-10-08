package com.shilapi.xcertplay

import java.util.concurrent.atomic.AtomicBoolean

/** Process-local evidence that the next wired start replaces an existing wired software session. */
internal object WiredSoftwareReconnect {
    private val pending = AtomicBoolean(false)

    /** Any existing session switching to wired needs a clean USB enumeration. */
    fun prepareForRestart(nextTransportWireless: Boolean) {
        pending.set(!nextTransportWireless)
    }

    fun isPending(): Boolean = pending.get()

    fun connected() {
        pending.set(false)
    }
}
