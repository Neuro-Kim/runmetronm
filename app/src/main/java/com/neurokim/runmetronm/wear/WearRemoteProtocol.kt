package com.neurokim.runmetronm.wear

import java.nio.ByteBuffer

object WearRemoteProtocol {
  const val BPM_ADJUST_PATH = "/remote/bpm/adjust"
  const val BPM_REQUEST_PATH = "/remote/bpm/request"
  const val TOGGLE_PATH = "/remote/metronome/toggle"
  const val VOLUME_ADJUST_PATH = "/remote/volume/adjust"
  const val TONE_CYCLE_PATH = "/remote/tone/cycle"
  const val STATE_SYNC_PATH = "/remote/state/sync"
  private const val DELTA_SIZE = Int.SIZE_BYTES
  private const val STATE_SIZE = Int.SIZE_BYTES + 1 + Float.SIZE_BYTES + 1

  fun encodeDelta(delta: Int): ByteArray = ByteBuffer.allocate(DELTA_SIZE).putInt(delta).array()

  fun encodeState(
    bpm: Int,
    isPlaying: Boolean,
    clickVolume: Float,
    toneOrdinal: Int,
  ): ByteArray =
    ByteBuffer
      .allocate(STATE_SIZE)
      .putInt(bpm)
      .put(if (isPlaying) 1 else 0)
      .putFloat(clickVolume)
      .put(toneOrdinal.toByte())
      .array()

  fun decodeDelta(payload: ByteArray): Int? {
    if (payload.size != DELTA_SIZE) {
      return null
    }
    val delta = ByteBuffer.wrap(payload).int
    return delta.takeIf { it in -5..5 && it != 0 }
  }

  fun decodeState(payload: ByteArray): WearRemoteState? {
    if (payload.size != STATE_SIZE) return null
    val buf = ByteBuffer.wrap(payload)
    return WearRemoteState(
      bpm = buf.int,
      isPlaying = buf.get() != 0.toByte(),
      clickVolume = buf.float,
      toneOrdinal = buf.get().toInt(),
    )
  }
}

data class WearRemoteState(
  val bpm: Int,
  val isPlaying: Boolean,
  val clickVolume: Float,
  val toneOrdinal: Int,
)
