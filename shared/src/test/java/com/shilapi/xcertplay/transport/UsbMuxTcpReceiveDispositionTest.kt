package com.shilapi.xcertplay.transport

import org.junit.Assert.assertEquals
import org.junit.Test

class UsbMuxTcpReceiveDispositionTest {
    @Test fun inOrderPayloadIsAcceptedWhole() {
        assertEquals(
            UsbMuxTcpReceiveDisposition.Accept(0),
            usbMuxTcpReceiveDisposition(100, 100, 64),
        )
    }

    @Test fun completeRetransmissionIsIgnored() {
        assertEquals(
            UsbMuxTcpReceiveDisposition.Duplicate,
            usbMuxTcpReceiveDisposition(164, 100, 64),
        )
    }

    @Test fun overlappingRetransmissionDeliversOnlyNewSuffix() {
        assertEquals(
            UsbMuxTcpReceiveDisposition.Accept(32),
            usbMuxTcpReceiveDisposition(132, 100, 64),
        )
    }

    @Test fun futurePayloadIsNotDeliveredAcrossAGap() {
        assertEquals(
            UsbMuxTcpReceiveDisposition.Gap(32),
            usbMuxTcpReceiveDisposition(100, 132, 64),
        )
    }

    @Test fun sequenceWrapIsHandledAsUnsignedTcpArithmetic() {
        assertEquals(
            UsbMuxTcpReceiveDisposition.Accept(0),
            usbMuxTcpReceiveDisposition(0, 0, 64),
        )
        assertEquals(
            UsbMuxTcpReceiveDisposition.Accept(16),
            usbMuxTcpReceiveDisposition(8, -8, 32),
        )
    }
}
