package com.shilapi.xcertplay.transport

import android.hardware.usb.UsbConfiguration
import android.hardware.usb.UsbEndpoint
import android.hardware.usb.UsbInterface
import android.os.Parcelable
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], manifest = Config.NONE)
class NcmFunctionDiscoveryTest {
    private fun iface(id: Int, alt: Int, klass: Int, subclass: Int = 0, endpoints: Array<Parcelable> = emptyArray()): UsbInterface {
        return UsbInterface::class.java.getDeclaredConstructor(
            Int::class.javaPrimitiveType, Int::class.javaPrimitiveType, String::class.java,
            Int::class.javaPrimitiveType, Int::class.javaPrimitiveType, Int::class.javaPrimitiveType,
        ).apply { isAccessible = true }.newInstance(id, alt, "test", klass, subclass, 0).apply {
            javaClass.getMethod("setEndpoints", Array<Parcelable>::class.java).invoke(this, endpoints)
        }
    }

    private fun discover(vararg extra: UsbInterface): NcmFunctionDiscovery.NcmFunction {
        val control = iface(3, 0, 2, 13)
        val active = iface(4, 1, 10, endpoints = arrayOf(
            endpoint(0x88), endpoint(6),
        ))
        val configuration = UsbConfiguration::class.java.getDeclaredConstructor(
            Int::class.javaPrimitiveType, String::class.java,
            Int::class.javaPrimitiveType, Int::class.javaPrimitiveType,
        ).apply { isAccessible = true }.newInstance(6, "test", 0, 0)
        configuration.javaClass.getMethod("setInterfaces", Array<Parcelable>::class.java)
            .invoke(configuration, arrayOf<Parcelable>(control, active, *extra))
        return NcmFunctionDiscovery.find(configuration)!!
    }

    private fun endpoint(address: Int): UsbEndpoint = UsbEndpoint::class.java.getDeclaredConstructor(
        Int::class.javaPrimitiveType, Int::class.javaPrimitiveType,
        Int::class.javaPrimitiveType, Int::class.javaPrimitiveType,
    ).apply { isAccessible = true }.newInstance(address, 2, 512, 0)

    @Test fun onlyDescriptorVerifiedEmptySettingForSameDataInterfaceIsSelected() {
        val idle = iface(4, 0, 10)
        assertSame(idle, discover(iface(6, 0, 10), idle).idleData)
    }

    @Test fun missingIdleSettingDoesNotInventOne() {
        assertNull(discover(iface(6, 0, 10)).idleData)
        assertNull(discover(iface(4, 0, 255)).idleData)
    }

    @Test fun settingWithEndpointsIsNotIdle() {
        val notIdle = iface(4, 0, 10, endpoints = arrayOf(endpoint(0x89)))
        assertNull(discover(notIdle).idleData)
    }
}
