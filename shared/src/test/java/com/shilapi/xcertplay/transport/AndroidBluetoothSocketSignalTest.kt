package com.shilapi.xcertplay.transport

import java.nio.ByteBuffer
import java.nio.ByteOrder
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class AndroidBluetoothSocketSignalTest {
    @Test
    fun parsesNativeBluetoothSocketSignal() {
        val bytes = ByteBuffer.allocate(20).order(ByteOrder.nativeOrder())
            .putShort(20)
            .put(byteArrayOf(0x80.toByte(), 0xb9.toByte(), 0x89.toByte(), 0x43, 0x99.toByte(), 0x7c))
            .putInt(7)
            .putInt(0)
            .putShort(990)
            .putShort(1010)
            .array()

        assertEquals(
            AndroidBluetoothSocketSignal(
                channel = 7,
                status = 0,
                maxTransmitPacketSize = 990,
                maxReceivePacketSize = 1010,
            ),
            AndroidBluetoothSocketSignal.parse(bytes),
        )
    }

    @Test
    fun rejectsWrongSignalSize() {
        val bytes = ByteBuffer.allocate(20).order(ByteOrder.nativeOrder())
            .putShort(19)
            .array()

        assertThrows(IllegalArgumentException::class.java) {
            AndroidBluetoothSocketSignal.parse(bytes)
        }
    }
}
