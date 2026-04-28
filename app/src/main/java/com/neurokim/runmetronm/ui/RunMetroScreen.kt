package com.neurokim.runmetronm.ui

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.neurokim.runmetronm.MainViewModel
import com.neurokim.runmetronm.metronome.MAX_BPM
import com.neurokim.runmetronm.metronome.MIN_BPM
import com.neurokim.runmetronm.metronome.MetronomeToneProfile
import com.neurokim.runmetronm.theme.Neon
import com.neurokim.runmetronm.theme.PureBlack
import com.neurokim.runmetronm.theme.SurfaceLowest
import com.neurokim.runmetronm.theme.Zinc300
import com.neurokim.runmetronm.theme.Zinc400
import com.neurokim.runmetronm.theme.Zinc500
import com.neurokim.runmetronm.theme.Zinc700
import com.neurokim.runmetronm.theme.Zinc800
import com.neurokim.runmetronm.theme.Zinc900

@Composable
fun RunMetroScreen(
  viewModel: MainViewModel,
  modifier: Modifier = Modifier,
) {
  val state by viewModel.uiState.collectAsStateWithLifecycle()
  val scrollState = rememberScrollState()
  var bpmDraft by rememberSaveable { mutableIntStateOf(state.settings.bpm) }
  var volumeDraft by rememberSaveable { mutableFloatStateOf(state.settings.clickVolume) }

  val notificationPermissionLauncher =
    rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
      viewModel.refreshNotificationPermissionState()
    }

  LaunchedEffect(state.settings.bpm) { bpmDraft = state.settings.bpm }
  LaunchedEffect(state.settings.clickVolume) { volumeDraft = state.settings.clickVolume }

  LifecycleResumeEffect(Unit) {
    viewModel.refreshNotificationPermissionState()
    onPauseOrDispose { }
  }

  Column(
    modifier =
      modifier
        .fillMaxSize()
        .background(PureBlack),
  ) {
    PulseTopBar()
    Column(
      modifier =
        Modifier
          .fillMaxSize()
          .verticalScroll(scrollState)
          .padding(horizontal = 24.dp, vertical = 32.dp),
      verticalArrangement = Arrangement.spacedBy(40.dp),
    ) {
      if (state.shouldPromptForNotifications) {
        NotificationPermissionCard(
          onRequestPermission = {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
              notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
          },
        )
      }

      BpmDisplay(bpm = state.settings.bpm)

      StepControlGrid(
        onStepDownFast = { viewModel.stepBpm(-5) },
        onStepDown = { viewModel.stepBpm(-1) },
        onStepUp = { viewModel.stepBpm(1) },
        onStepUpFast = { viewModel.stepBpm(5) },
      )

      BpmRangeSlider(
        value = bpmDraft,
        onValueChange = { bpmDraft = it },
        onValueChangeFinished = { viewModel.setBpm(bpmDraft) },
      )

      StartStopButton(
        isPlaying = state.isPlaying,
        onToggle = viewModel::togglePlayback,
      )

      ToneSelector(
        selected = state.settings.toneProfile,
        onSelect = viewModel::setToneProfile,
      )

      VolumeCard(
        value = volumeDraft,
        onValueChange = { volumeDraft = it },
        onValueChangeFinished = { viewModel.setClickVolume(volumeDraft) },
      )

      AccentCard(
        enabled = state.settings.accentEnabled,
        onToggle = viewModel::setAccentEnabled,
      )

      BeatsPerBarSelector(
        selected = state.settings.beatsPerBar,
        onSelect = viewModel::setBeatsPerBar,
      )
    }
  }
}

@Composable
private fun PulseTopBar() {
  Column(
    modifier =
      Modifier
        .fillMaxWidth()
        .background(PureBlack)
        .statusBarsPadding(),
  ) {
    Box(
      modifier = Modifier.fillMaxWidth().height(64.dp),
      contentAlignment = Alignment.Center,
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        Icon(
          imageVector = Icons.Filled.Speed,
          contentDescription = null,
          tint = Neon,
          modifier = Modifier.size(28.dp),
        )
        Text(
          text = "PULSE_RUN",
          color = Neon,
          fontWeight = FontWeight.Black,
          fontStyle = FontStyle.Italic,
          fontSize = 26.sp,
          letterSpacing = (-0.04).em,
        )
      }
    }
    Box(modifier = Modifier.fillMaxWidth().height(2.dp).background(Neon))
  }
}

@Composable
private fun BpmDisplay(bpm: Int) {
  Column(
    modifier =
      Modifier
        .fillMaxWidth()
        .background(Zinc900)
        .border(2.dp, Neon)
        .padding(vertical = 32.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    LabelCaps(text = "CURRENT CADENCE", color = Neon)
    Text(
      text = bpm.toString(),
      color = Neon,
      fontSize = 120.sp,
      fontWeight = FontWeight.Black,
      letterSpacing = (-4.8).sp,
      lineHeight = 110.sp,
    )
    Text(
      text = "BPM",
      color = Color.White,
      fontSize = 32.sp,
      fontWeight = FontWeight.Bold,
      lineHeight = 36.sp,
    )
  }
}

@Composable
private fun StepControlGrid(
  onStepDownFast: () -> Unit,
  onStepDown: () -> Unit,
  onStepUp: () -> Unit,
  onStepUpFast: () -> Unit,
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    StepButton(label = "-5", modifier = Modifier.weight(1f), onClick = onStepDownFast)
    StepButton(label = "-1", modifier = Modifier.weight(1f), onClick = onStepDown)
    StepButton(label = "+1", modifier = Modifier.weight(1f), onClick = onStepUp)
    StepButton(label = "+5", modifier = Modifier.weight(1f), onClick = onStepUpFast)
  }
}

@Composable
private fun StepButton(
  label: String,
  modifier: Modifier,
  onClick: () -> Unit,
) {
  Box(
    modifier =
      modifier
        .height(56.dp)
        .border(2.dp, Zinc700)
        .clickable(onClick = onClick),
    contentAlignment = Alignment.Center,
  ) {
    Text(
      text = label,
      color = Zinc300,
      fontSize = 28.sp,
      fontWeight = FontWeight.Bold,
    )
  }
}

@Composable
private fun BpmRangeSlider(
  value: Int,
  onValueChange: (Int) -> Unit,
  onValueChangeFinished: () -> Unit,
) {
  Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
    Row(
      modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
    ) {
      LabelCaps(text = "RANGE (BPM)", color = Zinc500)
      LabelCaps(text = "$MIN_BPM — $MAX_BPM", color = Zinc300)
    }
    Slider(
      value = value.toFloat(),
      onValueChange = { onValueChange(it.toInt()) },
      onValueChangeFinished = onValueChangeFinished,
      valueRange = MIN_BPM.toFloat()..MAX_BPM.toFloat(),
      colors = neonSliderColors(),
    )
  }
}

@Composable
private fun StartStopButton(
  isPlaying: Boolean,
  onToggle: () -> Unit,
) {
  Box(
    modifier = Modifier.fillMaxWidth(),
    contentAlignment = Alignment.Center,
  ) {
    Box(
      modifier =
        Modifier
          .size(192.dp)
          .clip(CircleShape)
          .background(PureBlack)
          .border(4.dp, Neon, CircleShape)
          .clickable(onClick = onToggle),
      contentAlignment = Alignment.Center,
    ) {
      Box(
        modifier =
          Modifier
            .size(160.dp)
            .clip(CircleShape)
            .background(Neon),
        contentAlignment = Alignment.Center,
      ) {
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
          Icon(
            imageVector = if (isPlaying) Icons.Filled.Stop else Icons.Filled.PlayArrow,
            contentDescription = if (isPlaying) "Stop" else "Start",
            tint = PureBlack,
            modifier = Modifier.size(48.dp),
          )
          Text(
            text = if (isPlaying) "STOP" else "START",
            color = PureBlack,
            fontSize = 28.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = (-1.2).sp,
          )
        }
      }
    }
  }
}

@Composable
private fun ToneSelector(
  selected: MetronomeToneProfile,
  onSelect: (MetronomeToneProfile) -> Unit,
) {
  Column(
    modifier =
      Modifier
        .fillMaxWidth()
        .border(1.dp, Zinc800)
        .background(SurfaceLowest)
        .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    LabelCaps(text = "SOUND TONE", color = Neon)
    Row(
      modifier =
        Modifier
          .fillMaxWidth()
          .height(IntrinsicSize.Min)
          .border(2.dp, Zinc800),
    ) {
      MetronomeToneProfile.entries.forEachIndexed { index, profile ->
        if (index > 0) SegmentDivider()
        SegmentButton(
          label = profile.label.uppercase(),
          selected = profile == selected,
          onClick = { onSelect(profile) },
          modifier = Modifier.weight(1f),
        )
      }
    }
  }
}

@Composable
private fun VolumeCard(
  value: Float,
  onValueChange: (Float) -> Unit,
  onValueChangeFinished: () -> Unit,
) {
  Column(
    modifier =
      Modifier
        .fillMaxWidth()
        .border(1.dp, Zinc800)
        .background(SurfaceLowest)
        .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      LabelCaps(text = "VOLUME", color = Neon)
      Icon(
        imageVector = Icons.AutoMirrored.Filled.VolumeUp,
        contentDescription = null,
        tint = Zinc400,
        modifier = Modifier.size(20.dp),
      )
    }
    Slider(
      value = value,
      onValueChange = onValueChange,
      onValueChangeFinished = onValueChangeFinished,
      valueRange = 0.15f..1f,
      colors = neonSliderColors(),
    )
  }
}

@Composable
private fun AccentCard(
  enabled: Boolean,
  onToggle: (Boolean) -> Unit,
) {
  Row(
    modifier =
      Modifier
        .fillMaxWidth()
        .border(1.dp, Zinc800)
        .background(SurfaceLowest)
        .padding(16.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    LabelCaps(text = "1ST BEAT ACCENT", color = Neon)
    NeonSwitch(checked = enabled, onCheckedChange = onToggle)
  }
}

@Composable
private fun BeatsPerBarSelector(
  selected: Int,
  onSelect: (Int) -> Unit,
) {
  Column(
    modifier =
      Modifier
        .fillMaxWidth()
        .border(1.dp, Zinc800)
        .background(SurfaceLowest)
        .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    LabelCaps(text = "BEATS PER BAR", color = Neon)
    Row(
      modifier =
        Modifier
          .fillMaxWidth()
          .height(IntrinsicSize.Min)
          .border(2.dp, Zinc800),
    ) {
      val options = listOf(2, 3, 4, 6)
      options.forEachIndexed { index, beats ->
        if (index > 0) SegmentDivider()
        SegmentButton(
          label = "$beats",
          selected = beats == selected,
          onClick = { onSelect(beats) },
          modifier = Modifier.weight(1f),
        )
      }
    }
  }
}

@Composable
private fun NotificationPermissionCard(onRequestPermission: () -> Unit) {
  Column(
    modifier =
      Modifier
        .fillMaxWidth()
        .border(2.dp, Neon)
        .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    LabelCaps(text = "NOTIFICATION REQUIRED", color = Neon)
    Text(
      text = "백그라운드 제어를 위해 알림을 허용해 주세요.",
      color = Zinc300,
      fontSize = 14.sp,
    )
    Button(
      onClick = onRequestPermission,
      colors =
        ButtonDefaults.buttonColors(
          containerColor = Neon,
          contentColor = PureBlack,
        ),
      shape = RectangleShape,
    ) {
      Text(text = "ALLOW", fontWeight = FontWeight.Black, letterSpacing = 1.sp)
    }
  }
}

@Composable
private fun RowScope.SegmentButton(
  label: String,
  selected: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Box(
    modifier =
      modifier
        .background(if (selected) Neon else Color.Transparent)
        .clickable(onClick = onClick)
        .padding(vertical = 12.dp),
    contentAlignment = Alignment.Center,
  ) {
    Text(
      text = label,
      color = if (selected) PureBlack else Zinc400,
      fontSize = 12.sp,
      fontWeight = FontWeight.SemiBold,
      letterSpacing = 1.2.sp,
    )
  }
}

@Composable
private fun SegmentDivider() {
  Box(
    modifier =
      Modifier
        .width(2.dp)
        .fillMaxHeight()
        .background(Zinc800),
  )
}

@Composable
private fun NeonSwitch(
  checked: Boolean,
  onCheckedChange: (Boolean) -> Unit,
) {
  Box(
    modifier =
      Modifier
        .size(width = 64.dp, height = 32.dp)
        .border(2.dp, Zinc700)
        .background(Zinc800)
        .clickable { onCheckedChange(!checked) }
        .padding(horizontal = 4.dp),
    contentAlignment = if (checked) Alignment.CenterEnd else Alignment.CenterStart,
  ) {
    Box(
      modifier =
        Modifier
          .size(width = 24.dp, height = 16.dp)
          .background(if (checked) Neon else Zinc400),
    )
  }
}

@Composable
private fun LabelCaps(
  text: String,
  color: Color = Neon,
) {
  Text(
    text = text,
    color = color,
    fontSize = 14.sp,
    fontWeight = FontWeight.SemiBold,
    letterSpacing = 1.4.sp,
    lineHeight = 16.sp,
  )
}

@Composable
private fun neonSliderColors() =
  SliderDefaults.colors(
    thumbColor = Neon,
    activeTrackColor = Neon,
    inactiveTrackColor = Zinc800,
    activeTickColor = PureBlack,
    inactiveTickColor = Zinc800,
  )
