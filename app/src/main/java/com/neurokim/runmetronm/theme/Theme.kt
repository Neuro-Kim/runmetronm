package com.neurokim.runmetronm.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme =
  darkColorScheme(
    primary = PulseOrange,
    onPrimary = TrackCream,
    secondary = PaceMint,
    onSecondary = TrackBlack,
    tertiary = SprintGold,
    background = TrackBlack,
    onBackground = TrackCream,
    surface = TrackPanel,
    onSurface = TrackCream,
    surfaceVariant = FinishLine,
    onSurfaceVariant = TrackFog,
    outline = TrackLine,
  )

private val LightColorScheme =
  lightColorScheme(
    primary = PulseOrangeDark,
    onPrimary = TrackCream,
    secondary = FinishLine,
    onSecondary = TrackCream,
    tertiary = SprintGold,
    background = TrackCream,
    onBackground = TrackBlack,
    surface = TrackSand,
    onSurface = TrackBlack,
    surfaceVariant = ColorTokens.LightSurface,
    onSurfaceVariant = FinishLine,
    outline = TrackLine,
  )

@Composable
fun RunMetroTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  content: @Composable () -> Unit,
) {
  MaterialTheme(
    colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
    typography = RunMetroTypography,
    content = content,
  )
}

private object ColorTokens {
  val LightSurface = Color(0xFFFFFBF7)
}
