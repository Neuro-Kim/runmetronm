package com.neurokim.runmetronm.metronome

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Handler
import android.os.HandlerThread
import android.os.SystemClock
import com.neurokim.runmetronm.R

class MetronomeEngine(
  context: Context,
  private val onBeat: (Int) -> Unit,
) {
  private val appContext = context.applicationContext
  private val workerThread = HandlerThread("RunMetroEngine").apply { start() }
  private val handler = Handler(workerThread.looper)
  private val soundPool =
    SoundPool.Builder()
      .setMaxStreams(2)
      .setAudioAttributes(
        AudioAttributes.Builder()
          .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
          .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
          .build(),
      ).build()

  private val sampleIds =
    toneResources.values
      .flatMap { listOf(it.accentResId, it.regularResId) }
      .distinct()
      .associateWith { resId -> soundPool.load(appContext, resId, 1) }

  @Volatile private var settings = MetronomeSettings()
  @Volatile private var isPlaying = false
  @Volatile private var beatInBar = 1
  @Volatile private var nextTickAtUptimeMs = 0L
  @Volatile private var samplesReady = false
  @Volatile private var loadedSamples = 0
  @Volatile private var pendingStart: MetronomeSettings? = null

  private val tickRunnable =
    object : Runnable {
      override fun run() {
        if (!isPlaying || !samplesReady) {
          return
        }

        val currentBeat = beatInBar
        playTick(currentBeat)
        onBeat(currentBeat)

        beatInBar = if (currentBeat >= settings.beatsPerBar) 1 else currentBeat + 1
        nextTickAtUptimeMs += settings.millisPerBeat
        scheduleNext()
      }
    }

  init {
    soundPool.setOnLoadCompleteListener { _, _, status ->
      if (status != 0) {
        return@setOnLoadCompleteListener
      }

      loadedSamples += 1
      if (loadedSamples >= sampleIds.size) {
        samplesReady = true
        pendingStart?.let {
          pendingStart = null
          start(it)
        }
      }
    }
  }

  fun start(nextSettings: MetronomeSettings) {
    settings = nextSettings.sanitized()
    beatInBar = 1
    isPlaying = true
    handler.removeCallbacks(tickRunnable)

    if (!samplesReady) {
      pendingStart = settings
      return
    }

    nextTickAtUptimeMs = SystemClock.uptimeMillis()
    handler.post(tickRunnable)
  }

  fun update(nextSettings: MetronomeSettings) {
    settings = nextSettings.sanitized()
    if (isPlaying) {
      start(settings)
    }
  }

  fun stop() {
    isPlaying = false
    beatInBar = 1
    pendingStart = null
    handler.removeCallbacks(tickRunnable)
  }

  fun release() {
    stop()
    soundPool.release()
    workerThread.quitSafely()
  }

  private fun scheduleNext() {
    if (!isPlaying) {
      return
    }

    handler.removeCallbacks(tickRunnable)
    val target = nextTickAtUptimeMs
    if (target <= SystemClock.uptimeMillis()) {
      handler.post(tickRunnable)
    } else {
      handler.postAtTime(tickRunnable, target)
    }
  }

  private fun playTick(currentBeat: Int) {
    val sampleSet = toneResources.getValue(settings.toneProfile)
    val soundId =
      if (settings.accentEnabled && currentBeat == 1) {
        sampleIds.getValue(sampleSet.accentResId)
      } else {
        sampleIds.getValue(sampleSet.regularResId)
      }

    val volume =
      if (settings.accentEnabled && currentBeat == 1) {
        settings.clickVolume
      } else {
        (settings.clickVolume * 0.88f).coerceAtLeast(MIN_VOLUME)
      }

    soundPool.play(soundId, volume, volume, 1, 0, 1f)
  }

  private data class ToneResources(
    val accentResId: Int,
    val regularResId: Int,
  )

  private companion object {
    val toneResources =
      mapOf(
        MetronomeToneProfile.SOFT_WOOD to
          ToneResources(
            accentResId = R.raw.tone_soft_wood_primary,
            regularResId = R.raw.tone_soft_wood_secondary,
          ),
        MetronomeToneProfile.LOW_PULSE to
          ToneResources(
            accentResId = R.raw.tone_low_pulse_primary,
            regularResId = R.raw.tone_low_pulse_secondary,
          ),
        MetronomeToneProfile.CLEAR_BELL to
          ToneResources(
            accentResId = R.raw.tone_clear_bell_primary,
            regularResId = R.raw.tone_clear_bell_secondary,
          ),
      )
  }
}
