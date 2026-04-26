package com.neurokim.runmetronm

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.neurokim.runmetronm.theme.RunMetroTheme
import com.neurokim.runmetronm.ui.RunMetroScreen

class MainActivity : ComponentActivity() {
  private val viewModel: MainViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    enableEdgeToEdge()
    setContent {
      RunMetroTheme {
        RunMetroScreen(
          viewModel = viewModel,
        )
      }
    }
  }
}
