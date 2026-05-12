package com.neurokim.runmetronm.metronome

const val MIN_BPM = 40
const val MAX_BPM = 240
const val DEFAULT_BPM = 175
const val DEFAULT_VOLUME = 0.72f
const val DEFAULT_BEATS_PER_BAR = 4
const val MIN_VOLUME = 0.15f
const val MAX_VOLUME = 1.0f
const val VOLUME_STEP = 0.1f

data class MetronomeSettings(
  val bpm: Int = DEFAULT_BPM,
  val clickVolume: Float = DEFAULT_VOLUME,
  val beatsPerBar: Int = DEFAULT_BEATS_PER_BAR,
  val accentEnabled: Boolean = true,
  val toneProfile: MetronomeToneProfile = MetronomeToneProfile.SOFT_WOOD,
) {
  val millisPerBeat: Long
    get() = 60_000L / bpm

  fun sanitized(): MetronomeSettings =
    copy(
      bpm = bpm.clampBpm(),
      clickVolume = clickVolume.coerceIn(MIN_VOLUME, MAX_VOLUME),
      beatsPerBar = beatsPerBar.coerceIn(2, 6),
    )
}

fun Int.clampBpm(): Int = coerceIn(MIN_BPM, MAX_BPM)

fun steppedBpm(current: Int, delta: Int): Int = (current + delta).clampBpm()
