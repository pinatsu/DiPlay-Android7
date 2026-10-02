package com.shilapi.xcertplay.util

import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

/** Pure-Kotlin Base64 so protocol code also runs before java.util.Base64 exists (API < 26). */
@OptIn(ExperimentalEncodingApi::class)
object Base64Codec {
    fun encode(bytes: ByteArray): String = Base64.Default.encode(bytes)

    fun decode(value: String): ByteArray = Base64.Default.decode(value)

    fun decodeMime(value: String): ByteArray = Base64.Mime.decode(value)

    fun decodeMime(value: ByteArray): ByteArray = Base64.Mime.decode(value)

    fun encodePem(bytes: ByteArray): String = encode(bytes).chunked(64).joinToString("\n")
}
