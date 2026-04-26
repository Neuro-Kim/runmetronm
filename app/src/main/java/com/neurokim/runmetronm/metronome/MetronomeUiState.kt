package com.neurokim.runmetronm.metronome

data class MetronomeUiState(
  val settings: MetronomeSettings = MetronomeSettings(),
  val isPlaying: Boolean = false,
  val activeBeat: Int = 1,
  val notificationsEnabled: Boolean = true,
) {
  val shouldPromptForNotifications: Boolean
    get() = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU && !notificationsEnabled
}

data class PlaybackState(
  val isPlaying: Boolean = false,
  val activeBeat: Int = 1,
)
