package com.shilapi.xcertplay

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WiredSoftwareReconnectTest {
    @Test fun pendingMarkerLivesUntilAConnectionSucceeds() {
        WiredSoftwareReconnect.connected()
        assertFalse(WiredSoftwareReconnect.isPending())
        WiredSoftwareReconnect.prepareForRestart(false, false)
        assertTrue(WiredSoftwareReconnect.isPending())
        WiredSoftwareReconnect.connected()
        assertFalse(WiredSoftwareReconnect.isPending())
    }

    @Test fun wirelessTransitionsDoNotRequestAUsbReset() {
        for ((currentWireless, nextWireless) in listOf(true to true, true to false, false to true)) {
            WiredSoftwareReconnect.prepareForRestart(false, false)
            WiredSoftwareReconnect.prepareForRestart(currentWireless, nextWireless)
            assertFalse(WiredSoftwareReconnect.isPending())
        }
    }
}
