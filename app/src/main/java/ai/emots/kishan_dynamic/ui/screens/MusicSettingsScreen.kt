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
import ai.emots.kishan_dynamic.ui.theme.AppTheme
import kotlinx.coroutines.launch
import androidx.compose.ui.unit.dp

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
    var showScrubber by remember { mutableStateOf(true) }

    val waveformStyles = listOf("Neon", "Aurora", "Studio", "Minimal")
    val selectedWaveformIndex = waveformStyles
        .indexOfFirst { savedWaveform.contains(it, ignoreCase = true) }
        .coerceAtLeast(0)

    AppScreen {
        AppTopBar(
            title = "Music & media",
            subtitle = "Album art, visualiser and controls",
            onBack = onBack
        )

        AppStage(
            caption = if (isExpandedPreview) "Tap the island to collapse" else "Tap the island to expand",
            minHeight = 200.dp
        ) {
            DynamicIslandPill(
                state = if (isExpandedPreview) IslandDemoState.MusicExpanded
                else IslandDemoState.MusicCompact,
                onTap = { isExpandedPreview = !isExpandedPreview }
            )
        }

        AppSectionSpacer()

        AppSectionTitle("Playback")
        AppListCard {
            AppToggleRow(
                title = "Show island for music",
                subtitle = "Display album art whenever audio is playing",
                glyph = AppleGlyph.Music,
                checked = isMusicEnabled,
                onCheckedChange = { isMusicEnabled = it }
            )
            AppRowDivider()
            AppToggleRow(
                title = "Scrubbable progress bar",
                subtitle = "Drag the timeline to seek through a track",
                glyph = AppleGlyph.Timer,
                checked = showScrubber,
                enabled = isMusicEnabled,
                onCheckedChange = { showScrubber = it }
            )
        }

        AppSectionSpacer()

        AppSectionTitle("Visualiser")
        AppCard {
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
