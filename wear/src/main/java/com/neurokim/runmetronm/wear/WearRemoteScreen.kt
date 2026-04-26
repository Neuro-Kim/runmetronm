package com.neurokim.runmetronm.wear

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import kotlinx.coroutines.launch

@Composable
fun RunMetroWearApp() {
  MaterialTheme {
    val context = LocalContext.current
    val haptics = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    val messenger = remember(context) { WearRemoteMessenger(context.applicationContext) }
    var currentBpm by remember { mutableStateOf<Int?>(null) }
    var isPlaying by remember { mutableStateOf(false) }

    DisposableEffect(messenger) {
      val listener = messenger.addStateSyncListener { bpm, playing ->
        currentBpm = bpm
        isPlaying = playing
      }
      scope.launch { messenger.requestState() }
      onDispose { messenger.removeStateSyncListener(listener) }
    }

    Column(
      modifier =
        Modifier
          .fillMaxSize()
          .background(Color(0xFF05070A)),
    ) {
      Box(
        modifier =
          Modifier
            .fillMaxWidth()
            .weight(1f)
            .clickable {
              haptics.performHapticFeedback(HapticFeedbackType.LongPress)
              isPlaying = !isPlaying
              scope.launch { messenger.sendToggle() }
            },
        contentAlignment = Alignment.Center,
      ) {
        Text(
          text = currentBpm?.toString() ?: "—",
          fontSize = 72.sp,
          fontWeight = FontWeight.Bold,
          color = if (isPlaying) Color(0xFFFF7A3D) else Color.White,
        )
      }
      Box(
        modifier = Modifier.fillMaxWidth().weight(1f),
        contentAlignment = Alignment.Center,
      ) {
        Row(
          modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp),
          horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
        ) {
          WearRemoteButton(
            symbol = "-",
            containerColor = Color(0xFF2A3440),
            contentColor = Color.White,
            onClick = {
              haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
              scope.launch {
                val sent = messenger.sendBpmDelta(-1)
                if (!sent) {
                  Toast.makeText(context, "폰을 연결해 주세요", Toast.LENGTH_SHORT).show()
                }
              }
            },
          )
          WearRemoteButton(
            symbol = "+",
            containerColor = Color(0xFFFF7A3D),
            contentColor = Color.White,
            onClick = {
              haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
              scope.launch {
                val sent = messenger.sendBpmDelta(1)
                if (!sent) {
                  Toast.makeText(context, "폰을 연결해 주세요", Toast.LENGTH_SHORT).show()
                }
              }
            },
          )
        }
      }
    }
  }
}

@Composable
private fun WearRemoteButton(
  symbol: String,
  containerColor: Color,
  contentColor: Color,
  onClick: () -> Unit,
) {
  Button(
    onClick = onClick,
    modifier = Modifier.size(78.dp),
    shape = CircleShape,
    colors =
      ButtonDefaults.buttonColors(
        containerColor = containerColor,
        contentColor = contentColor,
      ),
    contentPadding = PaddingValues(0.dp),
  ) {
    Box(
      modifier = Modifier.fillMaxSize(),
      contentAlignment = Alignment.Center,
    ) {
      Text(
        text = symbol,
        fontSize = 38.sp,
        fontWeight = FontWeight.Bold,
      )
    }
  }
}
