package com.neurokim.runmetronm.wear

import java.nio.ByteBuffer

object WearRemoteProtocol {
  const val BPM_ADJUST_PATH = "/remote/bpm/adjust"
  const val BPM_REQUEST_PATH = "/remote/bpm/request"
  const val TOGGLE_PATH = "/remote/metronome/toggle"
  const val STATE_SYNC_PATH = "/remote/state/sync"
  private const val DELTA_SIZE = Int.SIZE_BYTES
  private const val STATE_SIZE = Int.SIZE_BYTES + 1

  fun encodeDelta(delta: Int): ByteArray = ByteBuffer.allocate(DELTA_SIZE).putInt(delta).array()

  fun decodeState(payload: ByteArray): Pair<Int, Boolean>? {
    if (payload.size != STATE_SIZE) return null
    val buf = ByteBuffer.wrap(payload)
    return Pair(buf.int, buf.get() != 0.toByte())
  }
}
