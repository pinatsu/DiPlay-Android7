package com.shilapi.xcertplay

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WiredSoftwareReconnectTest {
    @Test fun pendingMarkerLivesUntilAConnectionSucceeds() {
        WiredSoftwareReconnect.connected()
        assertFalse(WiredSoftwareReconnect.isPending())
        WiredSoftwareReconnect.prepareForRestart(nextTransportWireless = false)
        assertTrue(WiredSoftwareReconnect.isPending())
        WiredSoftwareReconnect.connected()
        assertFalse(WiredSoftwareReconnect.isPending())
    }

    @Test fun existingSessionStartingWiredRequestsAUsbReset() {
        WiredSoftwareReconnect.connected()
        WiredSoftwareReconnect.prepareForRestart(nextTransportWireless = false)
        assertTrue(WiredSoftwareReconnect.isPending())
    }

    @Test fun existingSessionStartingWirelessDoesNotRequestAUsbReset() {
        WiredSoftwareReconnect.prepareForRestart(nextTransportWireless = false)
        WiredSoftwareReconnect.prepareForRestart(nextTransportWireless = true)
        assertFalse(WiredSoftwareReconnect.isPending())
    }
}
