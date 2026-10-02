package com.shilapi.xcertplay.orchestration

import java.io.IOException
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LockdownPairRecordErrorTest {
    @Test fun invalidHostIdTriggersRepair() {
        assertTrue(isInvalidLockdownPairRecord(IOException("StartSession failed: InvalidHostID")))
    }

    @Test fun invalidPairRecordTriggersRepairThroughCauseChain() {
        val cause = IOException("Lockdown rejected InvalidPairRecord")
        assertTrue(isInvalidLockdownPairRecord(IllegalStateException("open failed", cause)))
    }

    @Test fun unrelatedProtocolErrorDoesNotDiscardRecord() {
        assertFalse(isInvalidLockdownPairRecord(IOException("StartService timed out")))
    }
}
