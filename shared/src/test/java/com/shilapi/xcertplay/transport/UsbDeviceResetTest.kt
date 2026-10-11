package com.shilapi.xcertplay.transport

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UsbDeviceResetTest {
    @Test
    fun `zero reports a completed reset`() {
        assertTrue(usbResetWasTriggered(0))
    }

    @Test
    fun `enodev reports a triggered reset because the handle disappeared`() {
        assertTrue(usbResetWasTriggered(19))
    }

    @Test
    fun `other errors remain failures`() {
        assertFalse(usbResetWasTriggered(1))
        assertFalse(usbResetWasTriggered(13))
        assertFalse(usbResetWasTriggered(22))
    }
}
