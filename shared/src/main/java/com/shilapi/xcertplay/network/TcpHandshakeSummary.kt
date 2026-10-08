package com.shilapi.xcertplay.network

/** Header metadata only: never includes audio, video, or application payload. */
internal object TcpHandshakeSummary {
    fun describe(packet: ByteArray, offset: Int): String {
        if (offset < 0 || offset > packet.size - 20) return " tcpHeader=truncated"
        val headerBytes = ((packet[offset + 12].toInt() and 0xff) ushr 4) * 4
        if (headerBytes < 20 || headerBytes > packet.size - offset) return " tcpHeader=invalid"
        val flags = packet[offset + 13].toInt() and 0xff
        val names = listOf(
            0x01 to "FIN", 0x02 to "SYN", 0x04 to "RST", 0x08 to "PSH",
            0x10 to "ACK", 0x20 to "URG", 0x40 to "ECE", 0x80 to "CWR",
        ).filter { (mask, _) -> flags and mask != 0 }.joinToString("|") { it.second }
        return " flags=${names.ifEmpty { "NONE" }} seq=${u32(packet, offset + 4)}" +
            " ack=${u32(packet, offset + 8)} dataBytes=${packet.size - offset - headerBytes}"
    }

    private fun u32(packet: ByteArray, offset: Int): Long {
        var result = 0L
        for (index in offset until offset + 4) result = (result shl 8) or (packet[index].toLong() and 0xff)
        return result
    }
}
