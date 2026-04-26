package com.neurokim.runmetronm

import android.app.Application

class RunMetroApp : Application() {
  lateinit var appContainer: AppContainer
    private set

  override fun onCreate() {
    super.onCreate()
    appContainer = AppContainer(this)
  }
}
