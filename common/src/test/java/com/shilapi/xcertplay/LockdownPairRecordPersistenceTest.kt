package com.shilapi.xcertplay

import android.content.Context
import com.shilapi.xcertplay.transport.LockdownPairRecord
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33], manifest = Config.NONE)
class LockdownPairRecordPersistenceTest {
    private val context get() = RuntimeEnvironment.getApplication()
    private val prefs get() = context.getSharedPreferences("xcertplay_airplay", Context.MODE_PRIVATE)

    @Before fun clearPreferences() {
        prefs.edit().clear().commit()
    }

    @Test fun recordsAreScopedToEachIphone() {
        val first = record("first-host", 1)
        val second = record("second-host", 11)

        AirPlayPersistence.saveLockdownRecord(context, "phone-a", first)
        AirPlayPersistence.saveLockdownRecord(context, "phone-b", second)

        assertRecordEquals(first, AirPlayPersistence.loadLockdownRecord(context, "phone-a"))
        assertRecordEquals(second, AirPlayPersistence.loadLockdownRecord(context, "phone-b"))
        assertNull(AirPlayPersistence.loadLockdownRecord(context, "phone-c"))
    }

    @Test fun clearingOneIphonePreservesTheOther() {
        val first = record("first-host", 1)
        val second = record("second-host", 11)
        AirPlayPersistence.saveLockdownRecord(context, "phone-a", first)
        AirPlayPersistence.saveLockdownRecord(context, "phone-b", second)

        AirPlayPersistence.clearLockdownRecord(context, "phone-a")

        assertNull(AirPlayPersistence.loadLockdownRecord(context, "phone-a"))
        assertRecordEquals(second, AirPlayPersistence.loadLockdownRecord(context, "phone-b"))
    }

    @Test fun legacyGlobalRecordIsIgnored() {
        prefs.edit()
            .putString("lockdown_host_id", "legacy-host")
            .putString("lockdown_system_buid", "legacy-buid")
            .putString("lockdown_wifi_mac", "00:11:22:33:44:55")
            .putString("lockdown_device_public", "01")
            .putString("lockdown_device_cert", "02")
            .putString("lockdown_host_private", "03")
            .putString("lockdown_host_cert", "04")
            .putString("lockdown_root_private", "05")
            .putString("lockdown_root_cert", "06")
            .commit()

        assertNull(AirPlayPersistence.loadLockdownRecord(context, "phone-a"))
    }

    private fun record(hostId: String, seed: Int): LockdownPairRecord =
        LockdownPairRecord.restore(
            hostId = hostId,
            systemBuid = "buid-$hostId",
            wifiMacAddress = "00:11:22:33:44:${seed.toString(16).padStart(2, '0')}",
            devicePublicKeyPem = byteArrayOf(seed.toByte()),
            deviceCertificatePem = byteArrayOf((seed + 1).toByte()),
            hostPrivateKeyPem = byteArrayOf((seed + 2).toByte()),
            hostCertificatePem = byteArrayOf((seed + 3).toByte()),
            rootPrivateKeyPem = byteArrayOf((seed + 4).toByte()),
            rootCertificatePem = byteArrayOf((seed + 5).toByte()),
        )

    private fun assertRecordEquals(expected: LockdownPairRecord, actual: LockdownPairRecord?) {
        requireNotNull(actual)
        assertEquals(expected.hostId, actual.hostId)
        assertEquals(expected.systemBuid, actual.systemBuid)
        assertEquals(expected.wifiMacAddress, actual.wifiMacAddress)
        assertArrayEquals(expected.devicePublicKeyPem, actual.devicePublicKeyPem)
        assertArrayEquals(expected.deviceCertificatePem, actual.deviceCertificatePem)
        assertArrayEquals(expected.hostPrivateKeyPem, actual.hostPrivateKeyPem)
        assertArrayEquals(expected.hostCertificatePem, actual.hostCertificatePem)
        assertArrayEquals(expected.rootPrivateKeyPem, actual.rootPrivateKeyPem)
        assertArrayEquals(expected.rootCertificatePem, actual.rootCertificatePem)
    }
}
