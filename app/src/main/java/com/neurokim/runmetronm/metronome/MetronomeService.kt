package com.neurokim.runmetronm.metronome

import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import com.neurokim.runmetronm.appContainer

class MetronomeService : Service() {
  private val controller by lazy { applicationContext.appContainer.metronomeController }
  private lateinit var engine: MetronomeEngine
  private lateinit var notification: MetronomeNotification
  private var wakeLock: PowerManager.WakeLock? = null
  private var isForeground = false

  override fun onCreate() {
    super.onCreate()
    notification = MetronomeNotification(this)
    engine =
      MetronomeEngine(this) { beat ->
        controller.updateActiveBeat(beat)
      }
  }

  override fun onBind(intent: Intent?): IBinder? = null

  override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
    when (intent?.action) {
      ACTION_START -> startPlayback()
      ACTION_STOP -> stopPlayback()
      ACTION_REFRESH -> refreshPlayback()
      ACTION_STEP_UP -> {
        controller.stepBpm(5, refreshService = false)
        refreshPlayback()
      }
      ACTION_STEP_DOWN -> {
        controller.stepBpm(-5, refreshService = false)
        refreshPlayback()
      }
      else -> startPlayback()
    }
    return START_NOT_STICKY
  }

  override fun onDestroy() {
    stopPlayback(removeNotification = false)
    engine.release()
    super.onDestroy()
  }

  private fun startPlayback() {
    val settings = controller.currentSettings()
    notification.ensureChannel()
    val runningNotification = notification.buildRunning(settings)
    startForegroundCompat(runningNotification)
    acquireWakeLock()
    engine.start(settings)
    controller.updatePlayback(isPlaying = true)
    refreshNotification()
  }

  private fun refreshPlayback() {
    if (!isForeground) {
      startPlayback()
      return
    }

    val settings = controller.currentSettings()
    engine.update(settings)
    controller.updatePlayback(isPlaying = true, activeBeat = 1)
    refreshNotification()
  }

  private fun stopPlayback(removeNotification: Boolean = true) {
    engine.stop()
    releaseWakeLock()
    controller.updatePlayback(isPlaying = false, activeBeat = 1)

    if (isForeground) {
      stopForeground(STOP_FOREGROUND_REMOVE)
    }
    isForeground = false

    if (removeNotification) {
      stopSelf()
    }
  }

  private fun refreshNotification() {
    if (!isForeground) {
      return
    }

    val manager = getSystemService(android.app.NotificationManager::class.java)
    manager.notify(MetronomeNotification.NOTIFICATION_ID, notification.buildRunning(controller.currentSettings()))
  }

  private fun startForegroundCompat(runningNotification: android.app.Notification) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
      startForeground(
        MetronomeNotification.NOTIFICATION_ID,
        runningNotification,
        ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK,
      )
    } else {
      startForeground(MetronomeNotification.NOTIFICATION_ID, runningNotification)
    }
    isForeground = true
  }

  private fun acquireWakeLock() {
    if (wakeLock?.isHeld == true) {
      return
    }

    val manager = getSystemService(PowerManager::class.java)
    wakeLock =
      manager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "$packageName:runmetro").apply {
        setReferenceCounted(false)
        acquire()
      }
  }

  private fun releaseWakeLock() {
    wakeLock?.takeIf { it.isHeld }?.release()
    wakeLock = null
  }

  companion object {
    const val ACTION_START = "com.neurokim.runmetronm.action.START"
    const val ACTION_STOP = "com.neurokim.runmetronm.action.STOP"
    const val ACTION_REFRESH = "com.neurokim.runmetronm.action.REFRESH"
    const val ACTION_STEP_UP = "com.neurokim.runmetronm.action.STEP_UP"
    const val ACTION_STEP_DOWN = "com.neurokim.runmetronm.action.STEP_DOWN"
  }
}
