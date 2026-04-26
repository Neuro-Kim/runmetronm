package com.neurokim.runmetronm.wear

import android.content.Context
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class WearRemoteMessenger(private val context: Context) {
  suspend fun sendBpmDelta(delta: Int): Boolean =
    withContext(Dispatchers.IO) {
      val targetNode = nearbyNode() ?: return@withContext false
      runCatching {
        Tasks.await(
          Wearable
            .getMessageClient(context)
            .sendMessage(
              targetNode.id,
              WearRemoteProtocol.BPM_ADJUST_PATH,
              WearRemoteProtocol.encodeDelta(delta),
            ),
        )
      }.isSuccess
    }

  suspend fun sendToggle() =
    withContext(Dispatchers.IO) {
      val targetNode = nearbyNode() ?: return@withContext
      runCatching {
        Tasks.await(
          Wearable.getMessageClient(context).sendMessage(
            targetNode.id,
            WearRemoteProtocol.TOGGLE_PATH,
            ByteArray(0),
          ),
        )
      }
    }

  suspend fun requestState() =
    withContext(Dispatchers.IO) {
      val targetNode = nearbyNode() ?: return@withContext
      runCatching {
        Tasks.await(
          Wearable.getMessageClient(context).sendMessage(
            targetNode.id,
            WearRemoteProtocol.BPM_REQUEST_PATH,
            ByteArray(0),
          ),
        )
      }
    }

  fun addStateSyncListener(onState: (bpm: Int, isPlaying: Boolean) -> Unit): MessageClient.OnMessageReceivedListener {
    val listener = MessageClient.OnMessageReceivedListener { event ->
      if (event.path == WearRemoteProtocol.STATE_SYNC_PATH) {
        WearRemoteProtocol.decodeState(event.data)?.let { (bpm, isPlaying) -> onState(bpm, isPlaying) }
      }
    }
    Wearable.getMessageClient(context).addListener(listener)
    return listener
  }

  fun removeStateSyncListener(listener: MessageClient.OnMessageReceivedListener) {
    Wearable.getMessageClient(context).removeListener(listener)
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
