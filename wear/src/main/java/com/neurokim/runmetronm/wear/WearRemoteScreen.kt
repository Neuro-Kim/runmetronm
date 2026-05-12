package com.neurokim.runmetronm.wear

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.wear.compose.foundation.pager.VerticalPager
import androidx.wear.compose.foundation.pager.rememberPagerState
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

private val TONE_LABELS = listOf("소프트 우드", "로우 펄스", "클리어 벨")

@Composable
fun RunMetroWearApp() {
  MaterialTheme {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val messenger = remember(context) { WearRemoteMessenger(context.applicationContext) }
    var currentBpm by remember { mutableStateOf<Int?>(null) }
    var isPlaying by remember { mutableStateOf(false) }
    var currentVolume by remember { mutableStateOf<Float?>(null) }
    var currentToneOrdinal by remember { mutableStateOf<Int?>(null) }

    DisposableEffect(messenger) {
      val listener = messenger.addStateSyncListener { state ->
        currentBpm = state.bpm
        isPlaying = state.isPlaying
        currentVolume = state.clickVolume
        currentToneOrdinal = state.toneOrdinal
      }
      scope.launch { messenger.requestState() }
      onDispose { messenger.removeStateSyncListener(listener) }
    }

    val pagerState = rememberPagerState(pageCount = { 2 })
    VerticalPager(
      state = pagerState,
      modifier = Modifier.fillMaxSize().background(Color.Black),
    ) { page ->
      when (page) {
        0 ->
          BpmPage(
            currentBpm = currentBpm,
            isPlaying = isPlaying,
            scope = scope,
            messenger = messenger,
            onTogglePlaying = { isPlaying = !isPlaying },
          )
        else ->
          SettingsPage(
            currentVolume = currentVolume,
            currentToneOrdinal = currentToneOrdinal,
            scope = scope,
            messenger = messenger,
          )
      }
    }
  }
}

@Composable
private fun BpmPage(
  currentBpm: Int?,
  isPlaying: Boolean,
  scope: CoroutineScope,
  messenger: WearRemoteMessenger,
  onTogglePlaying: () -> Unit,
) {
  val context = LocalContext.current
  val haptics = LocalHapticFeedback.current
  Column(modifier = Modifier.fillMaxSize()) {
    Box(
      modifier =
        Modifier
          .fillMaxWidth()
          .weight(1f)
          .clickable {
            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
            onTogglePlaying()
            scope.launch { messenger.sendToggle() }
          },
      contentAlignment = Alignment.Center,
    ) {
      Text(
        text = currentBpm?.toString() ?: "—",
        fontSize = 72.sp,
        fontWeight = FontWeight.Black,
        letterSpacing = (-2.5).sp,
        color = if (isPlaying) Color(0xFFCCFF00) else Color.White,
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
          containerColor = Color(0xFF18181B),
          contentColor = Color.White,
          borderColor = Color(0xFF3F3F46),
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
          containerColor = Color(0xFFCCFF00),
          contentColor = Color.Black,
          borderColor = Color(0xFFCCFF00),
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

@Composable
private fun SettingsPage(
  currentVolume: Float?,
  currentToneOrdinal: Int?,
  scope: CoroutineScope,
  messenger: WearRemoteMessenger,
) {
  val context = LocalContext.current
  val haptics = LocalHapticFeedback.current
  Column(modifier = Modifier.fillMaxSize()) {
    AdjusterSection(
      label = "VOL",
      value = currentVolume?.let { "${(it * 100).roundToInt()}%" } ?: "—",
      modifier = Modifier.weight(1f),
      onMinus = {
        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        scope.launch {
          val sent = messenger.sendVolumeDelta(-1)
          if (!sent) {
            Toast.makeText(context, "폰을 연결해 주세요", Toast.LENGTH_SHORT).show()
          }
        }
      },
      onPlus = {
        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        scope.launch {
          val sent = messenger.sendVolumeDelta(1)
          if (!sent) {
            Toast.makeText(context, "폰을 연결해 주세요", Toast.LENGTH_SHORT).show()
          }
        }
      },
    )
    AdjusterSection(
      label = "TONE",
      value = currentToneOrdinal?.let { TONE_LABELS.getOrNull(it) } ?: "—",
      modifier = Modifier.weight(1f),
      onMinus = {
        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        scope.launch {
          val sent = messenger.sendToneCycle(-1)
          if (!sent) {
            Toast.makeText(context, "폰을 연결해 주세요", Toast.LENGTH_SHORT).show()
          }
        }
      },
      onPlus = {
        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        scope.launch {
          val sent = messenger.sendToneCycle(1)
          if (!sent) {
            Toast.makeText(context, "폰을 연결해 주세요", Toast.LENGTH_SHORT).show()
          }
        }
      },
    )
  }
}

@Composable
private fun AdjusterSection(
  label: String,
  value: String,
  modifier: Modifier = Modifier,
  onMinus: () -> Unit,
  onPlus: () -> Unit,
) {
  Column(
    modifier = modifier.fillMaxWidth().padding(horizontal = 14.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically),
  ) {
    Text(
      text = label,
      fontSize = 11.sp,
      fontWeight = FontWeight.Bold,
      letterSpacing = 1.5.sp,
      color = Color(0xFF71717A),
    )
    Text(
      text = value,
      fontSize = 22.sp,
      fontWeight = FontWeight.Black,
      color = Color.White,
    )
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
    ) {
      WearRemoteButton(
        symbol = "-",
        containerColor = Color(0xFF18181B),
        contentColor = Color.White,
        borderColor = Color(0xFF3F3F46),
        diameter = 52.dp,
        fontSize = 28.sp,
        onClick = onMinus,
      )
      WearRemoteButton(
        symbol = "+",
        containerColor = Color(0xFFCCFF00),
        contentColor = Color.Black,
        borderColor = Color(0xFFCCFF00),
        diameter = 52.dp,
        fontSize = 28.sp,
        onClick = onPlus,
      )
    }
  }
}

@Composable
private fun WearRemoteButton(
  symbol: String,
  containerColor: Color,
  contentColor: Color,
  borderColor: Color,
  onClick: () -> Unit,
  diameter: androidx.compose.ui.unit.Dp = 78.dp,
  fontSize: androidx.compose.ui.unit.TextUnit = 38.sp,
) {
  Button(
    onClick = onClick,
    modifier = Modifier.size(diameter).border(2.dp, borderColor, CircleShape),
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
        fontSize = fontSize,
        fontWeight = FontWeight.Black,
      )
    }
  }
}
