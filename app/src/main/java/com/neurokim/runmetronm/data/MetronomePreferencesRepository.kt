package com.neurokim.runmetronm.data

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.neurokim.runmetronm.metronome.DEFAULT_BEATS_PER_BAR
import com.neurokim.runmetronm.metronome.DEFAULT_BPM
import com.neurokim.runmetronm.metronome.DEFAULT_VOLUME
import com.neurokim.runmetronm.metronome.MetronomeSettings
import com.neurokim.runmetronm.metronome.MetronomeToneProfile
import com.neurokim.runmetronm.metronome.clampBpm
import com.neurokim.runmetronm.metronome.steppedBpm
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MetronomePreferencesRepository(context: Context) {
  private val prefs: SharedPreferences =
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

  private val listener =
    SharedPreferences.OnSharedPreferenceChangeListener { _, _ ->
      _settings.value = readSettings()
    }

  private val _settings = MutableStateFlow(readSettings())
  val settings: StateFlow<MetronomeSettings> = _settings.asStateFlow()

  init {
    prefs.registerOnSharedPreferenceChangeListener(listener)
  }

  fun setBpm(value: Int) {
    prefs.edit { putInt(KEY_BPM, value.clampBpm()) }
    _settings.value = readSettings()
  }

  fun stepBpm(delta: Int): Int {
    val next = steppedBpm(settings.value.bpm, delta)
    setBpm(next)
    return next
  }

  fun setClickVolume(value: Float) {
    prefs.edit { putFloat(KEY_CLICK_VOLUME, value) }
    _settings.value = readSettings()
  }

  fun setBeatsPerBar(value: Int) {
    prefs.edit { putInt(KEY_BEATS_PER_BAR, value.coerceIn(2, 6)) }
    _settings.value = readSettings()
  }

  fun setAccentEnabled(value: Boolean) {
    prefs.edit { putBoolean(KEY_ACCENT_ENABLED, value) }
    _settings.value = readSettings()
  }

  fun setToneProfile(value: MetronomeToneProfile) {
    prefs.edit { putString(KEY_TONE_PROFILE, value.storageValue) }
    _settings.value = readSettings()
  }

  private fun readSettings(): MetronomeSettings =
    MetronomeSettings(
      bpm = prefs.getInt(KEY_BPM, DEFAULT_BPM),
      clickVolume = prefs.getFloat(KEY_CLICK_VOLUME, DEFAULT_VOLUME),
      beatsPerBar = prefs.getInt(KEY_BEATS_PER_BAR, DEFAULT_BEATS_PER_BAR),
      accentEnabled = prefs.getBoolean(KEY_ACCENT_ENABLED, true),
      toneProfile = MetronomeToneProfile.fromStorageValue(prefs.getString(KEY_TONE_PROFILE, null)),
    ).sanitized()

  private companion object {
    const val PREFS_NAME = "runmetro_settings"
    const val KEY_BPM = "bpm"
    const val KEY_CLICK_VOLUME = "click_volume"
    const val KEY_BEATS_PER_BAR = "beats_per_bar"
    const val KEY_ACCENT_ENABLED = "accent_enabled"
    const val KEY_TONE_PROFILE = "tone_profile"
  }
}
