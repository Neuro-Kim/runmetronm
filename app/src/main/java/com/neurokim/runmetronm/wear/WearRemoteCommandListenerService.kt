package com.neurokim.runmetronm.wear

import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.Wearable
import com.google.android.gms.wearable.WearableListenerService
import com.neurokim.runmetronm.appContainer

class WearRemoteCommandListenerService : WearableListenerService() {
  override fun onMessageReceived(messageEvent: MessageEvent) {
    val controller = applicationContext.appContainer.metronomeController
    val newIsPlaying: Boolean
    when (messageEvent.path) {
      WearRemoteProtocol.BPM_ADJUST_PATH -> {
        val delta = WearRemoteProtocol.decodeDelta(messageEvent.data) ?: return
        controller.stepBpm(delta)
        newIsPlaying = controller.uiState.value.isPlaying
      }
      WearRemoteProtocol.TOGGLE_PATH -> {
        newIsPlaying = !controller.uiState.value.isPlaying
        controller.togglePlayback()
      }
      WearRemoteProtocol.BPM_REQUEST_PATH -> {
        newIsPlaying = controller.uiState.value.isPlaying
      }
      else -> return
    }
    Wearable.getMessageClient(applicationContext).sendMessage(
      messageEvent.sourceNodeId,
      WearRemoteProtocol.STATE_SYNC_PATH,
      WearRemoteProtocol.encodeState(controller.currentSettings().bpm, newIsPlaying),
    )
  }
}
