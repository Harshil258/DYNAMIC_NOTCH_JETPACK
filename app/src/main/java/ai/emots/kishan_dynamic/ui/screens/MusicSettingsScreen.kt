package ai.emots.kishan_dynamic.ui.screens

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import ai.emots.kishan_dynamic.data.preferences.AuroraPreferences
import ai.emots.kishan_dynamic.data.premium.PremiumFeaturePolicy
import ai.emots.kishan_dynamic.ui.components.AppText
import ai.emots.kishan_dynamic.ui.components.AppleGlyph
import ai.emots.kishan_dynamic.ui.components.DynamicIslandPill
import ai.emots.kishan_dynamic.ui.components.IslandDemoState
import ai.emots.kishan_dynamic.ui.kit.AppButton
import ai.emots.kishan_dynamic.ui.kit.AppCard
import ai.emots.kishan_dynamic.ui.kit.AppFootnote
import ai.emots.kishan_dynamic.ui.kit.AppListCard
import ai.emots.kishan_dynamic.ui.kit.AppRowDivider
import ai.emots.kishan_dynamic.ui.kit.AppScreen
import ai.emots.kishan_dynamic.ui.kit.AppSectionSpacer
import ai.emots.kishan_dynamic.ui.kit.AppSectionTitle
import ai.emots.kishan_dynamic.ui.kit.AppSegmented
import ai.emots.kishan_dynamic.ui.kit.AppStage
import ai.emots.kishan_dynamic.ui.kit.AppToggleRow
import ai.emots.kishan_dynamic.ui.kit.AppTopBar
import ai.emots.kishan_dynamic.ui.motion.appReveal
import ai.emots.kishan_dynamic.ui.theme.AppTheme
import kotlinx.coroutines.launch
import androidx.compose.ui.unit.dp

@Composable
fun MusicSettingsScreen(
    onBack: () -> Unit,
    onNavigateToPremium: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val preferences = remember { AuroraPreferences(context) }

    val savedWaveform by preferences.waveformStyle.collectAsState(initial = "Neon Cyan")
    val musicIslandEnabled by preferences.musicIslandEnabled.collectAsState(initial = true)
    val musicScrubberEnabled by preferences.musicScrubberEnabled.collectAsState(initial = true)
    val compactMusicControls by preferences.compactMusicControls.collectAsState(initial = false)
    val isProActive by preferences.isProActive.collectAsState(initial = false)
    val compactMusicControlsEnabled = PremiumFeaturePolicy.compactMusicControlsEnabled(
        isProActive = isProActive,
        requested = compactMusicControls
    )

    var isExpandedPreview by remember { mutableStateOf(true) }

    val waveformStyles = listOf("Neon", "Aurora", "Studio", "Minimal")
    val selectedWaveformIndex = waveformStyles
        .indexOfFirst { savedWaveform.contains(it, ignoreCase = true) }
        .coerceAtLeast(0)

    AppScreen {
        AppTopBar(
            modifier = Modifier.appReveal(0),
            title = "Music & media",
            subtitle = "Album art, visualiser and controls",
            onBack = onBack
        )

        AppStage(
            modifier = Modifier.appReveal(1),
            caption = if (compactMusicControlsEnabled) "Compact controls enabled"
            else if (isExpandedPreview) "Tap the island to collapse" else "Tap the island to expand",
            minHeight = 200.dp
        ) {
            DynamicIslandPill(
                state = if (isExpandedPreview && !compactMusicControlsEnabled) IslandDemoState.MusicExpanded
                else IslandDemoState.MusicCompact,
                onTap = {
                    if (!compactMusicControlsEnabled) isExpandedPreview = !isExpandedPreview
                }
            )
        }

        AppSectionSpacer()

        AppSectionTitle("Playback")
        AppListCard(modifier = Modifier.appReveal(2)) {
            AppToggleRow(
                title = "Show island for music",
                subtitle = "Display album art whenever audio is playing",
                glyph = AppleGlyph.Music,
                checked = musicIslandEnabled,
                onCheckedChange = { scope.launch { preferences.setMusicIslandEnabled(it) } }
            )
            AppRowDivider()
            AppToggleRow(
                title = "Scrubbable progress bar",
                subtitle = "Drag the timeline to seek through a track",
                glyph = AppleGlyph.Timer,
                checked = musicScrubberEnabled,
                enabled = musicIslandEnabled,
                onCheckedChange = { scope.launch { preferences.setMusicScrubberEnabled(it) } }
            )
            AppRowDivider()
            AppToggleRow(
                title = "Compact music controls",
                subtitle = if (isProActive) {
                    "Keep playback in the smaller island presentation"
                } else {
                    "Pro · Keep playback in the smaller island presentation"
                },
                glyph = AppleGlyph.Expand,
                checked = compactMusicControlsEnabled,
                enabled = musicIslandEnabled,
                trailingLabel = if (isProActive) null else "PRO",
                onCheckedChange = {
                    if (isProActive) {
                        scope.launch { preferences.setCompactMusicControls(it) }
                    } else {
                        onNavigateToPremium()
                    }
                }
            )
        }

        AppSectionSpacer()

        AppSectionTitle("Visualiser")
        AppCard(modifier = Modifier.appReveal(3)) {
            AppText(
                text = "Choose how the equaliser bars look while music plays.",
                style = AppTheme.typography.bodySmall,
                color = AppTheme.colors.textSecondary
            )
            Spacer(modifier = Modifier.height(AppTheme.spacing.md))
            AppSegmented(
                options = waveformStyles,
                selectedIndex = selectedWaveformIndex,
                onOptionSelected = { index ->
                    scope.launch { preferences.setWaveformStyle(waveformStyles[index]) }
                }
            )
        }

        AppSectionSpacer()

        AppButton(
            text = "Preview media playback",
            glyph = AppleGlyph.Play,
            onClick = { isExpandedPreview = true }
        )
        AppFootnote("Media info comes from the app that currently owns audio focus.")
    }
}
