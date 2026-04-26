package com.neurokim.runmetronm

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.neurokim.runmetronm.metronome.MetronomeUiState
import com.neurokim.runmetronm.metronome.MetronomeToneProfile
import kotlinx.coroutines.flow.StateFlow

class MainViewModel(application: Application) : AndroidViewModel(application) {
  private val controller = application.appContainer.metronomeController

  val uiState: StateFlow<MetronomeUiState> = controller.uiState

  fun togglePlayback() = controller.togglePlayback()

  fun setBpm(value: Int) = controller.setBpm(value)

  fun stepBpm(delta: Int) = controller.stepBpm(delta)

  fun setClickVolume(value: Float) = controller.setClickVolume(value)

  fun setBeatsPerBar(value: Int) = controller.setBeatsPerBar(value)

  fun setAccentEnabled(value: Boolean) = controller.setAccentEnabled(value)

  fun setToneProfile(value: MetronomeToneProfile) = controller.setToneProfile(value)

  fun refreshNotificationPermissionState() = controller.refreshNotificationPermissionState()
}
