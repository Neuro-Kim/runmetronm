package com.neurokim.runmetronm.wear

import org.junit.Assert.assertEquals
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
}
