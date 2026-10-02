package com.shilapi.xcertplay.transport

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.net.LocalSocket
import android.os.ParcelFileDescriptor
import android.os.ParcelUuid
import java.io.FileDescriptor
import java.io.IOException
import java.io.InputStream
import java.lang.reflect.InvocationTargetException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.UUID

/**
 * Bypasses firmware that replaces BluetoothSocket.connect() with an address-only GOC SPP proxy.
 *
 * Android 7's underlying IBluetooth service still exposes the normal UUID-aware RFCOMM socket
 * API. This connector is deliberately selected only for the affected API-25 test target.
 */
object Android7AospRfcommConnector {
    fun open(
        adapter: BluetoothAdapter,
        device: BluetoothDevice,
        uuid: UUID,
        timeoutMillis: Int,
    ): BluetoothRfcommDuplexStream {
        require(timeoutMillis > 0) { "timeoutMillis must be positive" }
        var descriptor: ParcelFileDescriptor? = null
        var socket: LocalSocket? = null
        try {
            val callbackClass = Class.forName("android.bluetooth.IBluetoothManagerCallback")
            val getService = BluetoothAdapter::class.java.getDeclaredMethod(
                "getBluetoothService",
                callbackClass,
            ).apply { isAccessible = true }
            val service = invokeReflectively { getService.invoke(adapter, null) }
                ?: throw IOException("Android Bluetooth service is unavailable")
            val connectSocket = service.javaClass.methods.firstOrNull { method ->
                method.name == "connectSocket" && method.parameterTypes.size == 5
            }?.apply { isAccessible = true }
                ?: throw IOException("Android Bluetooth service has no UUID-aware connectSocket")
            descriptor = invokeReflectively {
                connectSocket.invoke(
                    service,
                    device,
                    RFCOMM_SOCKET_TYPE,
                    ParcelUuid(uuid),
                    AUTO_ASSIGN_RFCOMM_CHANNEL,
                    AUTHENTICATED_ENCRYPTED_SECURITY_FLAGS,
                )
            } as? ParcelFileDescriptor
                ?: throw IOException("Android Bluetooth service returned no RFCOMM descriptor")

            val createSocket = LocalSocket::class.java.getDeclaredMethod(
                "createConnectedLocalSocket",
                FileDescriptor::class.java,
            ).apply { isAccessible = true }
            socket = invokeReflectively {
                createSocket.invoke(null, descriptor.fileDescriptor)
            } as? LocalSocket ?: throw IOException("Could not wrap the RFCOMM descriptor")
            socket.setSoTimeout(timeoutMillis)
            val input = socket.inputStream
            val channel = readNativeInt(input)
            if (channel <= 0) throw IOException("RFCOMM channel lookup failed: $channel")
            val signal = AndroidBluetoothSocketSignal.parse(readFully(input, SIGNAL_BYTES))
            if (signal.status != 0) {
                throw IOException("RFCOMM connection failed with status ${signal.status}")
            }
            socket.setSoTimeout(0)
            val ownedDescriptor = descriptor
            val ownedSocket = socket
            descriptor = null
            socket = null
            return BluetoothRfcommDuplexStream(
                input = input,
                output = ownedSocket.outputStream,
                closeTransport = AutoCloseable {
                    var failure: Throwable? = null
                    try {
                        ownedSocket.close()
                    } catch (error: Throwable) {
                        failure = error
                    }
                    try {
                        ownedDescriptor.close()
                    } catch (error: Throwable) {
                        if (failure == null) failure = error else failure.addSuppressed(error)
                    }
                    failure?.let { throw it }
                },
            )
        } catch (error: Throwable) {
            runCatching { socket?.close() }
            runCatching { descriptor?.close() }
            if (error is Error) throw error
            throw if (error is IOException) error else IOException(
                "Could not open the Android Bluetooth RFCOMM service",
                error,
            )
        }
    }

    private fun readNativeInt(input: InputStream): Int =
        ByteBuffer.wrap(readFully(input, Integer.BYTES))
            .order(ByteOrder.nativeOrder())
            .int

    private fun readFully(input: InputStream, size: Int): ByteArray {
        val result = ByteArray(size)
        var offset = 0
        while (offset < result.size) {
            val count = input.read(result, offset, result.size - offset)
            if (count < 0) throw IOException("RFCOMM socket closed during connection handshake")
            if (count == 0) continue
            offset += count
        }
        return result
    }

    private inline fun invokeReflectively(block: () -> Any?): Any? = try {
        block()
    } catch (error: InvocationTargetException) {
        val cause = error.targetException ?: error
        if (cause is Error) throw cause
        throw IOException(cause.message ?: cause.javaClass.simpleName, cause)
    }

    private const val RFCOMM_SOCKET_TYPE = 1
    private const val AUTO_ASSIGN_RFCOMM_CHANNEL = -1
    // BluetoothSocket.getSecurityFlags(): authenticated=2, encrypted=1.
    private const val AUTHENTICATED_ENCRYPTED_SECURITY_FLAGS = 3
    private const val SIGNAL_BYTES = 20
}

internal data class AndroidBluetoothSocketSignal(
    val channel: Int,
    val status: Int,
    val maxTransmitPacketSize: Int,
    val maxReceivePacketSize: Int,
) {
    companion object {
        fun parse(bytes: ByteArray): AndroidBluetoothSocketSignal {
            require(bytes.size == 20) { "Bluetooth socket signal must be 20 bytes" }
            val buffer = ByteBuffer.wrap(bytes).order(ByteOrder.nativeOrder())
            val size = buffer.short.toInt() and 0xffff
            require(size == bytes.size) { "Bluetooth socket signal declares $size bytes" }
            buffer.position(buffer.position() + 6) // Remote Bluetooth address.
            return AndroidBluetoothSocketSignal(
                channel = buffer.int,
                status = buffer.int,
                maxTransmitPacketSize = buffer.short.toInt() and 0xffff,
                maxReceivePacketSize = buffer.short.toInt() and 0xffff,
            )
        }
    }
}
