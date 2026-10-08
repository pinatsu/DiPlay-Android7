package com.shilapi.xcertplay.orchestration

import java.io.IOException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class LockdownPairRecordErrorTest {
    @Test fun invalidHostIdTriggersRepair() {
        assertEquals("InvalidHostID", lockdownPairRecordRejection(IOException("StartSession failed: InvalidHostID")))
    }

    @Test fun invalidPairRecordTriggersRepairThroughCauseChain() {
        val cause = IOException("Lockdown rejected InvalidPairRecord")
        assertEquals("InvalidPairRecord", lockdownPairRecordRejection(IllegalStateException("open failed", cause)))
    }

    @Test fun unrelatedProtocolErrorDoesNotDiscardRecord() {
        assertNull(lockdownPairRecordRejection(IOException("StartService timed out")))
    }
}
