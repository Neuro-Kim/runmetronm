package com.neurokim.runmetronm.metronome

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.neurokim.runmetronm.MainActivity
import com.neurokim.runmetronm.R

class MetronomeNotification(private val context: Context) {
  fun ensureChannel() {
    val manager = context.getSystemService(NotificationManager::class.java)
    if (manager.getNotificationChannel(CHANNEL_ID) != null) {
      return
    }

    val channel =
      NotificationChannel(
        CHANNEL_ID,
        context.getString(R.string.notification_channel_name),
        NotificationManager.IMPORTANCE_LOW,
      ).apply {
        description = context.getString(R.string.notification_channel_description)
        setShowBadge(false)
      }

    manager.createNotificationChannel(channel)
  }

  fun buildRunning(settings: MetronomeSettings): Notification {
    val title = "RunMetro · ${settings.bpm} BPM"
    val text =
      buildString {
        append(settings.toneProfile.label)
        append(" · ")
        append("${settings.beatsPerBar}/4 ")
        append(if (settings.accentEnabled) "· 강세 ON" else "· 강세 OFF")
      }

    return NotificationCompat.Builder(context, CHANNEL_ID)
      .setSmallIcon(R.drawable.ic_notification)
      .setContentTitle(title)
      .setContentText(text)
      .setSubText("음악과 함께 재생")
      .setContentIntent(activityPendingIntent())
      .setOnlyAlertOnce(true)
      .setSilent(true)
      .setOngoing(true)
      .setCategory(NotificationCompat.CATEGORY_SERVICE)
      .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
      .addAction(
        android.R.drawable.ic_media_rew,
        context.getString(R.string.notification_action_slower),
        servicePendingIntent(MetronomeService.ACTION_STEP_DOWN),
      )
      .addAction(
        android.R.drawable.ic_media_ff,
        context.getString(R.string.notification_action_faster),
        servicePendingIntent(MetronomeService.ACTION_STEP_UP),
      )
      .addAction(
        android.R.drawable.ic_media_pause,
        context.getString(R.string.notification_action_stop),
        servicePendingIntent(MetronomeService.ACTION_STOP),
      )
      .build()
  }

  private fun activityPendingIntent(): PendingIntent =
    PendingIntent.getActivity(
      context,
      0,
      Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
      },
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )

  private fun servicePendingIntent(action: String): PendingIntent =
    PendingIntent.getService(
      context,
      action.hashCode(),
      Intent(context, MetronomeService::class.java).setAction(action),
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )

  companion object {
    const val CHANNEL_ID = "runmetro_playback"
    const val NOTIFICATION_ID = 7_801
  }
}
