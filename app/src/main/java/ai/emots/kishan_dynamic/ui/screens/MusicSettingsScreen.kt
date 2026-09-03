package ai.emots.kishan_dynamic.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ai.emots.kishan_dynamic.data.preferences.AuroraPreferences
import ai.emots.kishan_dynamic.ui.components.AppleButton
import ai.emots.kishan_dynamic.ui.components.AppleButtonStyle
import ai.emots.kishan_dynamic.ui.components.AppleGlyph
import ai.emots.kishan_dynamic.ui.components.AppleSegmentedControl
import ai.emots.kishan_dynamic.ui.components.AtmosphericBackground
import ai.emots.kishan_dynamic.ui.components.DynamicIslandPill
import ai.emots.kishan_dynamic.ui.components.IslandDemoState
import ai.emots.kishan_dynamic.ui.components.LuxuryCard
import ai.emots.kishan_dynamic.ui.components.LuxurySwitch
import ai.emots.kishan_dynamic.ui.components.LuxuryTopBar
import ai.emots.kishan_dynamic.ui.theme.AppTheme
import ai.emots.kishan_dynamic.ui.theme.AuroraTokens
import kotlinx.coroutines.launch

@Composable
fun MusicSettingsScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val preferences = remember { AuroraPreferences(context) }

    val savedWaveform by preferences.waveformStyle.collectAsState(initial = "Neon Cyan")
    var isExpandedPreview by remember { mutableStateOf(true) }
    var isMusicEnabled by remember { mutableStateOf(true) }

    val waveformStyles = listOf("Neon Cyan", "Aurora", "Studio", "Minimal")
    val selectedWaveformIndex = waveformStyles.indexOfFirst { savedWaveform.contains(it) }.coerceAtLeast(0)

    AtmosphericBackground {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .widthIn(max = 760.dp)
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = AppTheme.layout.screenGutter)
                .padding(bottom = AppTheme.spacing.xxxl),
            verticalArrangement = Arrangement.spacedBy(AuroraTokens.Spacing.sectionGap)
        ) {
            // Top Bar
            LuxuryTopBar(
                title = "Music & Media Player",
                subtitle = "Album disc, waveforms & track controls",
                onBack = onBack
            )

            // Live Island Media Preview
            LuxuryCard(padding = 20.dp) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    DynamicIslandPill(
                        state = if (isExpandedPreview) IslandDemoState.MusicExpanded else IslandDemoState.MusicCompact,
                        onTap = { isExpandedPreview = !isExpandedPreview }
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = if (isExpandedPreview) "Tap island to collapse to compact disc" else "Tap island to expand full music player",
                        fontSize = 11.5.sp,
                        color = AuroraTokens.TextColor.secondary
                    )
                }
            }

            // Toggles Card
            LuxuryCard {
                Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                            Text(
                                text = "Show Island for Music",
                                fontSize = 14.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = AuroraTokens.TextColor.primary
                            )
                            Text(
                                text = "Automatically display spinning album art when songs play on Spotify, YouTube, etc.",
                                fontSize = 11.5.sp,
                                color = AuroraTokens.TextColor.secondary
                            )
                        }

                        LuxurySwitch(
                            checked = isMusicEnabled,
                            onCheckedChange = { isMusicEnabled = it }
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                            Text(
                                text = "Show Scrubbable Progress Bar",
                                fontSize = 14.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = AuroraTokens.TextColor.primary
                            )
                            Text(
                                text = "Drag along the island timeline to fast-forward or rewind songs",
                                fontSize = 11.5.sp,
                                color = AuroraTokens.TextColor.secondary
                            )
                        }

                        LuxurySwitch(
                            checked = isExpandedPreview,
                            onCheckedChange = { isExpandedPreview = it }
                        )
                    }
                }
            }

            // Visualizer Style Palette with Apple Segmented Control
            LuxuryCard {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Audio Waveform Visualizer Style",
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AuroraTokens.TextColor.primary
                    )
                    Text(
                        text = "Choose the look of the glowing dancing equalizer bars",
                        fontSize = 11.5.sp,
                        color = AuroraTokens.TextColor.secondary
                    )

                    AppleSegmentedControl(
                        options = waveformStyles,
                        selectedIndex = selectedWaveformIndex,
                        onOptionSelected = { idx ->
                            scope.launch { preferences.setWaveformStyle(waveformStyles[idx]) }
                        }
                    )
                }
            }

            // Simulate Music Playback Apple Button
            AppleButton(
                text = "Test Spotify Media Playback",
                onClick = { isExpandedPreview = true },
                glyph = AppleGlyph.Play,
                style = AppleButtonStyle.PRIMARY,
                paddingVertical = 15.dp
            )
        }
    }
}
