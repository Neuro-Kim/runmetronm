package com.neurokim.runmetronm.ui

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import com.neurokim.runmetronm.R
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.neurokim.runmetronm.MainViewModel
import com.neurokim.runmetronm.metronome.MAX_BPM
import com.neurokim.runmetronm.metronome.MIN_BPM
import com.neurokim.runmetronm.metronome.MetronomeToneProfile
import com.neurokim.runmetronm.metronome.MetronomeUiState
import com.neurokim.runmetronm.theme.FinishLine
import com.neurokim.runmetronm.theme.PaceMint
import com.neurokim.runmetronm.theme.PulseOrange
import com.neurokim.runmetronm.theme.SprintGold
import com.neurokim.runmetronm.theme.TrackBlack
import com.neurokim.runmetronm.theme.TrackCream
import com.neurokim.runmetronm.theme.TrackFog
import com.neurokim.runmetronm.theme.TrackLine
import com.neurokim.runmetronm.theme.TrackMist
import com.neurokim.runmetronm.theme.TrackPanel
import com.neurokim.runmetronm.theme.TrackSlate

@Composable
fun RunMetroScreen(
  viewModel: MainViewModel,
  modifier: Modifier = Modifier,
) {
  val state by viewModel.uiState.collectAsStateWithLifecycle()
  val scrollState = rememberScrollState()
  val quickPaces = remember { listOf(140, 150, 160, 170, 180, 190, 200, 210) }
  var bpmDraft by rememberSaveable { mutableIntStateOf(state.settings.bpm) }
  var volumeDraft by rememberSaveable { mutableFloatStateOf(state.settings.clickVolume) }

  val notificationPermissionLauncher =
    rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
      viewModel.refreshNotificationPermissionState()
    }

  LaunchedEffect(state.settings.bpm) {
    bpmDraft = state.settings.bpm
  }
  LaunchedEffect(state.settings.clickVolume) {
    volumeDraft = state.settings.clickVolume
  }

  LifecycleResumeEffect(Unit) {
    viewModel.refreshNotificationPermissionState()
    onPauseOrDispose { }
  }

  Scaffold(
    containerColor = TrackBlack,
    modifier = modifier.fillMaxSize(),
  ) { innerPadding ->
    Box(
      modifier =
        Modifier
          .fillMaxSize()
          .background(
            Brush.verticalGradient(
              colors = listOf(TrackBlack, TrackSlate, FinishLine),
            ),
          ),
    ) {
      Column(
        modifier =
          Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(innerPadding)
            .verticalScroll(scrollState)
            .padding(horizontal = 18.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
      ) {
        HeaderBlock()

        if (state.shouldPromptForNotifications) {
          NotificationPermissionCard(
            onRequestPermission = {
              if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
              }
            },
          )
        }

        HeroCard(
          state = state,
          onTogglePlayback = viewModel::togglePlayback,
          onStepDownFast = { viewModel.stepBpm(-5) },
          onStepDown = { viewModel.stepBpm(-1) },
          onStepUp = { viewModel.stepBpm(1) },
          onStepUpFast = { viewModel.stepBpm(5) },
        )

        RunnerCard(
          title = "BPM 범위",
          subtitle = "이제 120~220 BPM까지 넓게 조절할 수 있습니다.",
        ) {
          MetricRow(
            label = "현재 BPM",
            value = "$bpmDraft",
            valueColor = SprintGold,
          )
          Spacer(modifier = Modifier.height(10.dp))
          Slider(
            value = bpmDraft.toFloat(),
            onValueChange = { bpmDraft = it.toInt() },
            onValueChangeFinished = { viewModel.setBpm(bpmDraft) },
            valueRange = MIN_BPM.toFloat()..MAX_BPM.toFloat(),
            colors = runMetroSliderColors(),
          )
          Spacer(modifier = Modifier.height(8.dp))
          Text(
            text = "$MIN_BPM BPM      러닝 존      $MAX_BPM BPM",
            style = MaterialTheme.typography.labelLarge,
            color = TrackFog,
          )
          Spacer(modifier = Modifier.height(12.dp))
          FlowRowLike {
            quickPaces.forEach { pace ->
              ChoicePill(
                label = "$pace",
                selected = state.settings.bpm == pace,
                onClick = {
                  bpmDraft = pace
                  viewModel.setBpm(pace)
                },
              )
            }
          }
        }

        RunnerCard(
          title = "사운드 톤",
          subtitle = "기존보다 덜 귀아프게 낮은 대역을 포함한 프리셋을 고를 수 있게 했습니다.",
        ) {
          MetricRow(
            label = "선택된 톤",
            value = state.settings.toneProfile.label,
            valueColor = PaceMint,
          )
          Spacer(modifier = Modifier.height(10.dp))
          MetronomeToneProfile.entries.forEach { profile ->
            ToneProfileCard(
              profile = profile,
              selected = state.settings.toneProfile == profile,
              onClick = { viewModel.setToneProfile(profile) },
            )
            Spacer(modifier = Modifier.height(10.dp))
          }
          MetricRow(
            label = "클릭 볼륨",
            value = "${(volumeDraft * 100).toInt()}%",
            valueColor = SprintGold,
          )
          Spacer(modifier = Modifier.height(10.dp))
          Slider(
            value = volumeDraft,
            onValueChange = { volumeDraft = it },
            onValueChangeFinished = { viewModel.setClickVolume(volumeDraft) },
            valueRange = 0.15f..1f,
            colors = runMetroSliderColors(),
          )
          Spacer(modifier = Modifier.height(10.dp))
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
              Text(
                text = "첫 박 강조",
                style = MaterialTheme.typography.titleMedium,
                color = TrackCream,
              )
              Text(
                text = "첫 박만 조금 더 도드라지게 들립니다.",
                style = MaterialTheme.typography.bodyLarge,
                color = TrackMist,
              )
            }
            Switch(
              checked = state.settings.accentEnabled,
              onCheckedChange = viewModel::setAccentEnabled,
              colors =
                SwitchDefaults.colors(
                  checkedThumbColor = TrackBlack,
                  checkedTrackColor = PaceMint,
                  uncheckedThumbColor = TrackCream,
                  uncheckedTrackColor = FinishLine,
                  uncheckedBorderColor = TrackLine,
                ),
            )
          }
          Spacer(modifier = Modifier.height(14.dp))
          Text(
            text = "한 마디 박자",
            style = MaterialTheme.typography.titleMedium,
            color = TrackCream,
          )
          Spacer(modifier = Modifier.height(8.dp))
          FlowRowLike {
            listOf(2, 3, 4, 6).forEach { beats ->
              ChoicePill(
                label = "$beats 박",
                selected = state.settings.beatsPerBar == beats,
                onClick = { viewModel.setBeatsPerBar(beats) },
              )
            }
          }
        }
      }
    }
  }
}

@Composable
private fun HeaderBlock() {
  Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
      Icon(
        painter = painterResource(id = R.drawable.run_circle_24),
        contentDescription = null,
        tint = PaceMint,
        modifier = Modifier.size(20.dp),
      )
      Text(
        text = "RUN RHYTHM",
        style = MaterialTheme.typography.labelLarge,
        color = PaceMint,
      )
    }
    Text(
      text = "러닝용 메트로놈",
      style = MaterialTheme.typography.headlineMedium,
      color = TrackCream,
    )
    Text(
      text = "음악은 계속 듣고, 클릭만 안정적으로 얹는 고대비 러닝 모드입니다.",
      style = MaterialTheme.typography.bodyLarge,
      color = TrackFog,
    )
  }
}

@Composable
private fun NotificationPermissionCard(onRequestPermission: () -> Unit) {
  Surface(
    color = Color(0xFF312100),
    shape = RoundedCornerShape(28.dp),
    shadowElevation = 0.dp,
  ) {
    Column(
      modifier =
        Modifier
          .fillMaxWidth()
          .padding(20.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      Text(
        text = "알림 권한을 허용하면 백그라운드 제어가 더 안정적입니다.",
        style = MaterialTheme.typography.titleMedium,
        color = TrackCream,
      )
      Text(
        text = "Android 13 이상에서는 알림이 차단되면 고정 제어가 덜 보일 수 있습니다.",
        style = MaterialTheme.typography.bodyLarge,
        color = Color(0xFFFFE2A8),
      )
      Button(
        onClick = onRequestPermission,
        colors = ButtonDefaults.buttonColors(containerColor = SprintGold, contentColor = TrackBlack),
      ) {
        Text("알림 허용")
      }
    }
  }
}

@Composable
private fun HeroCard(
  state: MetronomeUiState,
  onTogglePlayback: () -> Unit,
  onStepDownFast: () -> Unit,
  onStepDown: () -> Unit,
  onStepUp: () -> Unit,
  onStepUpFast: () -> Unit,
) {
  Surface(
    color = TrackPanel,
    shape = RoundedCornerShape(34.dp),
    modifier =
      Modifier
        .fillMaxWidth()
        .border(1.5.dp, TrackLine, RoundedCornerShape(34.dp)),
  ) {
    Column(
      modifier = Modifier.padding(horizontal = 20.dp, vertical = 22.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
      Text(
        text = if (state.isPlaying) "RUNNING" else "READY",
        style = MaterialTheme.typography.labelLarge,
        color = SprintGold,
      )
      Text(
        text = state.settings.bpm.toString(),
        style = MaterialTheme.typography.displayLarge,
        color = TrackCream,
      )
      Text(
        text = "${state.settings.toneProfile.label} · ${if (state.isPlaying) "재생 중" else "대기 중"}",
        style = MaterialTheme.typography.titleMedium,
        color = TrackFog,
        textAlign = TextAlign.Center,
      )
      BeatIndicators(
        beatsPerBar = state.settings.beatsPerBar,
        activeBeat = state.activeBeat,
        isPlaying = state.isPlaying,
      )
      FlowRowLike(horizontalSpacing = 10.dp, verticalSpacing = 10.dp) {
        ActionPill(label = "-5", onClick = onStepDownFast)
        ActionPill(label = "-1", onClick = onStepDown)
        ActionPill(label = "+1", onClick = onStepUp)
        ActionPill(label = "+5", onClick = onStepUpFast)
      }
      Button(
        onClick = onTogglePlayback,
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(containerColor = PulseOrange, contentColor = TrackCream),
      ) {
        Text(if (state.isPlaying) "메트로놈 정지" else "메트로놈 시작")
      }
      Text(
        text = if (state.isPlaying) "다른 음악 앱과 함께 클릭 재생 중" else "원하는 톤과 BPM을 맞춘 뒤 바로 시작",
        style = MaterialTheme.typography.bodyLarge,
        color = TrackMist,
        textAlign = TextAlign.Center,
      )
    }
  }
}

@Composable
private fun RunnerCard(
  title: String,
  subtitle: String,
  content: @Composable ColumnScope.() -> Unit,
) {
  Surface(
    color = TrackPanel,
    shape = RoundedCornerShape(28.dp),
    modifier =
      Modifier
        .fillMaxWidth()
        .border(1.dp, TrackLine, RoundedCornerShape(28.dp)),
  ) {
    Column(
      modifier = Modifier.padding(20.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp),
      content = {
        Text(
          text = title,
          style = MaterialTheme.typography.titleMedium,
          color = TrackCream,
        )
        Text(
          text = subtitle,
          style = MaterialTheme.typography.bodyLarge,
          color = TrackFog,
        )
        Spacer(modifier = Modifier.height(6.dp))
        content()
      },
    )
  }
}

@Composable
private fun ToneProfileCard(
  profile: MetronomeToneProfile,
  selected: Boolean,
  onClick: () -> Unit,
) {
  val borderColor by animateColorAsState(if (selected) SprintGold else TrackLine, label = "toneBorder")
  val backgroundColor by animateColorAsState(if (selected) FinishLine else TrackSlate, label = "toneBackground")

  Column(
    modifier =
      Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(22.dp))
        .background(backgroundColor)
        .border(1.5.dp, borderColor, RoundedCornerShape(22.dp))
        .clickable(onClick = onClick)
        .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(4.dp),
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Text(
        text = profile.label,
        style = MaterialTheme.typography.titleMedium,
        color = TrackCream,
      )
      Text(
        text = if (selected) "선택됨" else "선택",
        style = MaterialTheme.typography.labelLarge,
        color = if (selected) SprintGold else TrackFog,
      )
    }
    Text(
      text = profile.description,
      style = MaterialTheme.typography.bodyLarge,
      color = TrackMist,
    )
  }
}

@Composable
private fun MetricRow(
  label: String,
  value: String,
  valueColor: Color = PaceMint,
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Text(
      text = label,
      style = MaterialTheme.typography.titleMedium,
      color = TrackCream,
    )
    Text(
      text = value,
      style = MaterialTheme.typography.labelLarge,
      color = valueColor,
    )
  }
}

@Composable
private fun BeatIndicators(
  beatsPerBar: Int,
  activeBeat: Int,
  isPlaying: Boolean,
) {
  Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
    repeat(beatsPerBar) { index ->
      val beatNumber = index + 1
      val color by animateColorAsState(
        targetValue =
          when {
            !isPlaying -> TrackLine
            beatNumber == activeBeat -> PulseOrange
            else -> Color(0xFF6A7A8B)
          },
        label = "beatColor",
      )

      Box(
        modifier =
          Modifier
            .width(if (beatNumber == activeBeat && isPlaying) 24.dp else 16.dp)
            .height(16.dp)
            .clip(CircleShape)
            .background(color),
      )
    }
  }
}

@Composable
private fun ChoicePill(
  label: String,
  selected: Boolean,
  onClick: () -> Unit,
) {
  val backgroundColor by animateColorAsState(if (selected) PulseOrange else FinishLine, label = "pillBackground")
  val borderColor by animateColorAsState(if (selected) SprintGold else TrackLine, label = "pillBorder")
  val textColor by animateColorAsState(if (selected) TrackCream else TrackFog, label = "pillText")

  Box(
    modifier =
      Modifier
        .clip(RoundedCornerShape(999.dp))
        .background(backgroundColor)
        .border(1.dp, borderColor, RoundedCornerShape(999.dp))
        .clickable(onClick = onClick)
        .padding(horizontal = 14.dp, vertical = 10.dp),
  ) {
    Text(
      text = label,
      style = MaterialTheme.typography.labelLarge,
      color = textColor,
    )
  }
}

@Composable
private fun ActionPill(
  label: String,
  onClick: () -> Unit,
) {
  Box(
    modifier =
      Modifier
        .clip(RoundedCornerShape(999.dp))
        .background(FinishLine)
        .border(1.dp, TrackLine, RoundedCornerShape(999.dp))
        .clickable(onClick = onClick)
        .padding(horizontal = 18.dp, vertical = 11.dp),
  ) {
    Text(
      text = label,
      style = MaterialTheme.typography.labelLarge,
      color = TrackCream,
    )
  }
}

@Composable
private fun FlowRowLike(
  horizontalSpacing: androidx.compose.ui.unit.Dp = 8.dp,
  verticalSpacing: androidx.compose.ui.unit.Dp = 8.dp,
  content: @Composable RowScope.() -> Unit,
) {
  Row(
    modifier =
      Modifier
        .fillMaxWidth()
        .horizontalScroll(rememberScrollState())
        .padding(vertical = verticalSpacing / 2),
    horizontalArrangement = Arrangement.spacedBy(horizontalSpacing),
    content = content,
  )
}

@Composable
private fun runMetroSliderColors() =
  SliderDefaults.colors(
    thumbColor = PulseOrange,
    activeTrackColor = PaceMint,
    inactiveTrackColor = TrackLine,
    activeTickColor = TrackBlack,
    inactiveTickColor = TrackLine,
  )
