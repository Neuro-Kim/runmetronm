package com.neurokim.runmetronm.metronome

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.neurokim.runmetronm.data.MetronomePreferencesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

class MetronomeController(
  private val appContext: Context,
  private val preferencesRepository: MetronomePreferencesRepository,
) {
  private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
  private val playbackState = MutableStateFlow(PlaybackState())
  private val notificationsEnabled = MutableStateFlow(readNotificationState())

  val uiState: StateFlow<MetronomeUiState> =
    combine(preferencesRepository.settings, playbackState, notificationsEnabled) { settings, playback, canNotify ->
      MetronomeUiState(
        settings = settings,
        isPlaying = playback.isPlaying,
        activeBeat = playback.activeBeat,
        notificationsEnabled = canNotify,
      )
    }.stateIn(scope, SharingStarted.Eagerly, MetronomeUiState())

  fun togglePlayback() {
    if (playbackState.value.isPlaying) stop() else start()
  }

  fun start() {
    ContextCompat.startForegroundService(appContext, serviceIntent(MetronomeService.ACTION_START))
  }

  fun stop() {
    appContext.startService(serviceIntent(MetronomeService.ACTION_STOP))
  }

  fun setBpm(value: Int, refreshService: Boolean = true) {
    preferencesRepository.setBpm(value)
    refreshRunningService(refreshService)
  }

  fun stepBpm(delta: Int, refreshService: Boolean = true) {
    preferencesRepository.stepBpm(delta)
    refreshRunningService(refreshService)
  }

  fun setClickVolume(value: Float, refreshService: Boolean = true) {
    preferencesRepository.setClickVolume(value)
    refreshRunningService(refreshService)
  }

  fun setBeatsPerBar(value: Int, refreshService: Boolean = true) {
    preferencesRepository.setBeatsPerBar(value)
    refreshRunningService(refreshService)
  }

  fun setAccentEnabled(value: Boolean, refreshService: Boolean = true) {
    preferencesRepository.setAccentEnabled(value)
    refreshRunningService(refreshService)
  }

  fun setToneProfile(value: MetronomeToneProfile, refreshService: Boolean = true) {
    preferencesRepository.setToneProfile(value)
    refreshRunningService(refreshService)
  }

  fun refreshNotificationPermissionState() {
    notificationsEnabled.value = readNotificationState()
  }

  fun currentSettings(): MetronomeSettings = preferencesRepository.settings.value

  fun updatePlayback(isPlaying: Boolean, activeBeat: Int = 1) {
    playbackState.value = PlaybackState(isPlaying = isPlaying, activeBeat = activeBeat)
  }

  fun updateActiveBeat(activeBeat: Int) {
    playbackState.update { current ->
      if (!current.isPlaying) current else current.copy(activeBeat = activeBeat)
    }
  }

  private fun refreshRunningService(enabled: Boolean) {
    if (!enabled || !playbackState.value.isPlaying) {
      return
    }

    appContext.startService(serviceIntent(MetronomeService.ACTION_REFRESH))
  }

  private fun readNotificationState(): Boolean {
    val manager = NotificationManagerCompat.from(appContext)
    val platformManager = appContext.getSystemService(NotificationManager::class.java)
    return manager.areNotificationsEnabled() || platformManager == null
  }

  private fun serviceIntent(action: String): Intent =
    Intent(appContext, MetronomeService::class.java).setAction(action)
}
