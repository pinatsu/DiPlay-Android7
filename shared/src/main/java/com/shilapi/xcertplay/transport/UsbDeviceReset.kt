package com.shilapi.xcertplay.transport

import android.hardware.usb.UsbDeviceConnection
import java.io.IOException

internal interface UsbDeviceResetBridge {
    /** Returns zero on success or the Linux errno value on failure. */
    fun reset(fileDescriptor: Int): Int
}

internal object UsbDeviceReset {
    private val bridge: UsbDeviceResetBridge by lazy { NativeUsbDeviceResetBridge }

    @Throws(IOException::class)
    fun reset(connection: UsbDeviceConnection) {
        val fileDescriptor = connection.fileDescriptor
        if (fileDescriptor < 0) throw IOException("USB reset has no open device file descriptor")
        val errno = bridge.reset(fileDescriptor)
        if (!usbResetWasTriggered(errno)) {
            throw IOException("USBDEVFS_RESET failed: errno=$errno")
        }
    }
}

/**
 * Some Android USB host kernels invalidate the usbfs file descriptor while completing the reset,
 * so the ioctl returns ENODEV even though the device immediately detaches and re-enumerates. The
 * controller still requires a fresh device to appear within its bounded discovery timeout.
 */
internal fun usbResetWasTriggered(errno: Int): Boolean = errno == 0 || errno == ENODEV

private const val ENODEV = 19

private object NativeUsbDeviceResetBridge : UsbDeviceResetBridge {
    init {
        System.loadLibrary("xcertplay_usb")
    }

    override fun reset(fileDescriptor: Int): Int = UsbDeviceResetNative.reset(fileDescriptor)
}

private object UsbDeviceResetNative {
    external fun reset(fileDescriptor: Int): Int
}
