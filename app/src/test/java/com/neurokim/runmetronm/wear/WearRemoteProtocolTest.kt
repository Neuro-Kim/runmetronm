package com.neurokim.runmetronm.wear

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class WearRemoteProtocolTest {
  @Test
  fun decodeDelta_returnsExpectedValue() {
    assertEquals(1, WearRemoteProtocol.decodeDelta(WearRemoteProtocol.encodeDelta(1)))
    assertEquals(-1, WearRemoteProtocol.decodeDelta(WearRemoteProtocol.encodeDelta(-1)))
  }

  @Test
  fun decodeDelta_rejectsOutOfRangePayload() {
    assertNull(WearRemoteProtocol.decodeDelta(WearRemoteProtocol.encodeDelta(9)))
    assertNull(WearRemoteProtocol.decodeDelta(byteArrayOf(1, 2)))
  }

  @Test
  fun stateRoundTrip_preservesAllFields() {
    val encoded = WearRemoteProtocol.encodeState(bpm = 175, isPlaying = true, clickVolume = 0.42f, toneOrdinal = 2)
    val decoded = WearRemoteProtocol.decodeState(encoded)
    assertNotNull(decoded)
    assertEquals(175, decoded!!.bpm)
    assertEquals(true, decoded.isPlaying)
    assertEquals(0.42f, decoded.clickVolume, 0.0001f)
    assertEquals(2, decoded.toneOrdinal)
  }

  @Test
  fun decodeState_rejectsWrongSize() {
    assertNull(WearRemoteProtocol.decodeState(ByteArray(5)))
  }
}
