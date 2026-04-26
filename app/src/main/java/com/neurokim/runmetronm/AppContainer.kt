package com.neurokim.runmetronm

import android.content.Context
import com.neurokim.runmetronm.data.MetronomePreferencesRepository
import com.neurokim.runmetronm.metronome.MetronomeController

class AppContainer(context: Context) {
  val preferencesRepository = MetronomePreferencesRepository(context)
  val metronomeController = MetronomeController(context, preferencesRepository)
}

val Context.appContainer: AppContainer
  get() = (applicationContext as RunMetroApp).appContainer
