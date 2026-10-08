package com.shilapi.xcertplay.network

import org.junit.Assert.assertEquals
import org.junit.Test

class TcpHandshakeSummaryTest {
    @Test fun synAckWithUnsignedSequenceAndOptions() {
        val packet = ByteArray(28)
        packet[12] = 0x60
        packet[13] = 0x12
        for (index in 4..7) packet[index] = 0xff.toByte()
        packet[11] = 7
        assertEquals(" flags=SYN|ACK seq=4294967295 ack=7 dataBytes=4", TcpHandshakeSummary.describe(packet, 0))
    }

    @Test fun resetAtEthernetIpv6Offset() {
        val packet = ByteArray(74)
        packet[66] = 0x50
        packet[67] = 0x04
        assertEquals(" flags=RST seq=0 ack=0 dataBytes=0", TcpHandshakeSummary.describe(packet, 54))
    }

    @Test fun rejectsTruncationAndInvalidHeaderLengths() {
        assertEquals(" tcpHeader=truncated", TcpHandshakeSummary.describe(ByteArray(19), 0))
        assertEquals(" tcpHeader=truncated", TcpHandshakeSummary.describe(ByteArray(20), -1))
        assertEquals(" tcpHeader=invalid", TcpHandshakeSummary.describe(ByteArray(20), 0))
        val packet = ByteArray(20).apply { this[12] = 0x60 }
        assertEquals(" tcpHeader=invalid", TcpHandshakeSummary.describe(packet, 0))
    }
}
