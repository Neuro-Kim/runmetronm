package com.neurokim.runmetronm.wear

import android.content.Context
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class WearRemoteMessenger(private val context: Context) {
  suspend fun sendBpmDelta(delta: Int): Boolean = sendDelta(WearRemoteProtocol.BPM_ADJUST_PATH, delta)

  suspend fun sendVolumeDelta(delta: Int): Boolean = sendDelta(WearRemoteProtocol.VOLUME_ADJUST_PATH, delta)

  suspend fun sendToneCycle(delta: Int): Boolean = sendDelta(WearRemoteProtocol.TONE_CYCLE_PATH, delta)

  suspend fun sendToggle() = sendEmpty(WearRemoteProtocol.TOGGLE_PATH)

  suspend fun requestState() = sendEmpty(WearRemoteProtocol.BPM_REQUEST_PATH)

  fun addStateSyncListener(
    onState: (state: WearRemoteState) -> Unit,
  ): MessageClient.OnMessageReceivedListener {
    val listener = MessageClient.OnMessageReceivedListener { event ->
      if (event.path == WearRemoteProtocol.STATE_SYNC_PATH) {
        WearRemoteProtocol.decodeState(event.data)?.let(onState)
      }
    }
    Wearable.getMessageClient(context).addListener(listener)
    return listener
  }

  fun removeStateSyncListener(listener: MessageClient.OnMessageReceivedListener) {
    Wearable.getMessageClient(context).removeListener(listener)
  }

  private suspend fun sendDelta(path: String, delta: Int): Boolean =
    withContext(Dispatchers.IO) {
      val targetNode = nearbyNode() ?: return@withContext false
      runCatching {
        Tasks.await(
          Wearable
            .getMessageClient(context)
            .sendMessage(targetNode.id, path, WearRemoteProtocol.encodeDelta(delta)),
        )
      }.isSuccess
    }

  private suspend fun sendEmpty(path: String) =
    withContext(Dispatchers.IO) {
      val targetNode = nearbyNode() ?: return@withContext
      runCatching {
        Tasks.await(
          Wearable.getMessageClient(context).sendMessage(targetNode.id, path, ByteArray(0)),
        )
      }
    }

  private suspend fun nearbyNode() =
    withContext(Dispatchers.IO) {
      val nodes =
        runCatching {
          Tasks.await(Wearable.getNodeClient(context).connectedNodes)
        }.getOrElse { return@withContext null }
      nodes.firstOrNull { it.isNearby } ?: nodes.firstOrNull()
    }
}
