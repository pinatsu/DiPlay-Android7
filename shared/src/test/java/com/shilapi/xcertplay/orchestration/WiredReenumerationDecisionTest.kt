package com.shilapi.xcertplay.orchestration

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WiredReenumerationDecisionTest {
    @Test fun everyWiredControllerResetsOnlyItsFirstReadyConfiguration() {
        assertTrue(shouldForceWiredReenumeration(true, true, 0))
        assertFalse(shouldForceWiredReenumeration(true, true, 1))
        assertFalse(shouldForceWiredReenumeration(false, true, 0))
        assertFalse(shouldForceWiredReenumeration(true, false, 0))
    }
}
