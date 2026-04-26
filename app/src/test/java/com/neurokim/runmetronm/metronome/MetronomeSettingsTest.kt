package com.neurokim.runmetronm.metronome

import org.junit.Assert.assertEquals
import org.junit.Test

class MetronomeSettingsTest {
  @Test
  fun sanitized_clampsPaceVolumeAndBeats() {
    val settings =
      MetronomeSettings(
        bpm = 120,
        clickVolume = 1.4f,
        beatsPerBar = 9,
      ).sanitized()

    assertEquals(MIN_BPM, settings.bpm)
    assertEquals(MAX_VOLUME, settings.clickVolume, 0.0001f)
    assertEquals(6, settings.beatsPerBar)
  }

  @Test
  fun steppedBpm_respectsRunningRange() {
    assertEquals(MAX_BPM, steppedBpm(218, 8))
    assertEquals(MIN_BPM, steppedBpm(121, -8))
    assertEquals(180, steppedBpm(178, 2))
  }
}
