package com.shilapi.xcertplay.media

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NavigationDuckingEnvelopeTest {
    @Test fun fadesDownHoldsDuringPromptAndRestoresAfterSilence() {
        val envelope = NavigationDuckingEnvelope()

        assertTrue(envelope.navigationAudio(0))
        assertVolume(1f, envelope.volume(0))
        assertVolume(0.65f, envelope.volume(75))
        assertVolume(0.30f, envelope.volume(150))
        assertVolume(0.30f, envelope.volume(500))
        assertVolume(0.65f, envelope.volume(650))
        assertVolume(1f, envelope.volume(800))
        assertTrue(envelope.settled(800))
        assertEquals(30L, envelope.nextUpdateDelay(75, 30))
        assertEquals(350L, envelope.nextUpdateDelay(150, 30))
        assertEquals(30L, envelope.nextUpdateDelay(650, 30))
        assertEquals(null, envelope.nextUpdateDelay(800, 30))
    }

    @Test fun repeatedAudioExtendsTheHoldWithoutRestartingTheFade() {
        val envelope = NavigationDuckingEnvelope()

        assertTrue(envelope.navigationAudio(0))
        assertFalse(envelope.navigationAudio(400))
        assertVolume(0.30f, envelope.volume(850))
        assertVolume(0.30f, envelope.volume(900))
        assertVolume(0.65f, envelope.volume(1_050))
        assertVolume(1f, envelope.volume(1_200))
    }

    @Test fun aNewPromptDuringRestoreFadesFromTheCurrentVolumeWithoutJumping() {
        val envelope = NavigationDuckingEnvelope()
        envelope.navigationAudio(0)
        assertVolume(0.65f, envelope.volume(650))

        assertTrue(envelope.navigationAudio(650))
        assertVolume(0.65f, envelope.volume(650))
        assertVolume(0.475f, envelope.volume(725))
        assertVolume(0.30f, envelope.volume(800))
    }

    private fun assertVolume(expected: Float, actual: Float) {
        assertEquals(expected.toDouble(), actual.toDouble(), 0.001)
    }
}
