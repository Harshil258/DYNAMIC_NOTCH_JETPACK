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
import androidx.compose.ui.unit.dp
import ai.emots.kishan_dynamic.data.preferences.AuroraPreferences
import ai.emots.kishan_dynamic.data.premium.PremiumFeaturePolicy
import ai.emots.kishan_dynamic.ui.components.AppleGlyph
import ai.emots.kishan_dynamic.ui.components.DynamicIslandPill
import ai.emots.kishan_dynamic.ui.components.IslandDemoState
import ai.emots.kishan_dynamic.ui.kit.AppCard
import ai.emots.kishan_dynamic.ui.kit.AppFootnote
import ai.emots.kishan_dynamic.ui.kit.AppListCard
import ai.emots.kishan_dynamic.ui.kit.AppRowDivider
import ai.emots.kishan_dynamic.ui.kit.AppScreen
import ai.emots.kishan_dynamic.ui.kit.AppSectionSpacer
import ai.emots.kishan_dynamic.ui.kit.AppSectionTitle
import ai.emots.kishan_dynamic.ui.kit.AppSlider
import ai.emots.kishan_dynamic.ui.kit.AppStage
import ai.emots.kishan_dynamic.ui.kit.AppToggleRow
import ai.emots.kishan_dynamic.ui.kit.AppTopBar
import ai.emots.kishan_dynamic.ui.motion.appReveal
import ai.emots.kishan_dynamic.ui.theme.AppTheme
import kotlinx.coroutines.launch

@Composable
fun SoundSettingsScreen(
    onBack: () -> Unit,
    onNavigateToPremium: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val preferences = remember { AuroraPreferences(context) }
    val isProActive by preferences.isProActive.collectAsState(initial = false)
    val soundLocked = PremiumFeaturePolicy.isLocked(
        PremiumFeaturePolicy.SOUND_SETTINGS,
        isProActive
    )
    val showRingerMode by preferences.showRingerModeIndicator.collectAsState(initial = true)
    val showVolume by preferences.showVolumeIndicator.collectAsState(initial = true)
    val showMute by preferences.muteIndicatorEnabled.collectAsState(initial = true)
    val showVibrate by preferences.vibrateIndicatorEnabled.collectAsState(initial = true)
    val hapticEnabled by preferences.hapticFeedbackEnabled.collectAsState(initial = true)
    val pulseScale by preferences.pulseScale.collectAsState(initial = 1.15f)
    val pulseDuration by preferences.pulseDuration.collectAsState(initial = 450)
    var isExpandedPreview by remember { mutableStateOf(false) }

    AppScreen {
        AppTopBar(
            modifier = Modifier.appReveal(0),
            title = "Sound & haptics",
            subtitle = "Ringer, volume HUD, and pulse behaviour",
            onBack = onBack
        )

        AppStage(
            modifier = Modifier.appReveal(1),
            caption = if (showVolume) "Volume indicator preview" else "Sound indicators paused",
            minHeight = 150.dp
        ) {
            DynamicIslandPill(
                state = if (isExpandedPreview && showRingerMode) IslandDemoState.RingerModeExpanded
                else if (showVolume) IslandDemoState.MediaVolumeCompact
                else if (showRingerMode) IslandDemoState.SilentModeCompact
                else IslandDemoState.Minimal,
                onTap = { isExpandedPreview = !isExpandedPreview },
                mediaVolumeHudLevel = 0.64f,
                ringerMode = ai.emots.kishan_dynamic.data.model.RingerModeType.SILENT,
                pulseScale = pulseScale,
                pulseDurationMillis = pulseDuration
            )
        }

        AppSectionSpacer()

        AppSectionTitle("Indicators")
        AppListCard(modifier = Modifier.appReveal(2)) {
            AppToggleRow(
                title = "Ringer mode indicator",
                subtitle = "Show normal, silent, and vibrate changes",
                glyph = AppleGlyph.Bell,
                checked = showRingerMode,
                trailingLabel = if (soundLocked) "PRO" else null,
                onCheckedChange = {
                    if (soundLocked) onNavigateToPremium()
                    else scope.launch { preferences.setShowRingerModeIndicator(it) }
                }
            )
            AppRowDivider()
            AppToggleRow(
                title = "Mute indicator",
                subtitle = "Show the island when the phone becomes silent",
                glyph = AppleGlyph.BellSlash,
                checked = showMute,
                enabled = showRingerMode,
                trailingLabel = if (soundLocked) "PRO" else null,
                onCheckedChange = {
                    if (soundLocked) onNavigateToPremium()
                    else scope.launch { preferences.setMuteIndicatorEnabled(it) }
                }
            )
            AppRowDivider()
            AppToggleRow(
                title = "Vibrate indicator",
                subtitle = "Show the island when the phone enters vibrate mode",
                glyph = AppleGlyph.Phone,
                checked = showVibrate,
                enabled = showRingerMode,
                trailingLabel = if (soundLocked) "PRO" else null,
                onCheckedChange = {
                    if (soundLocked) onNavigateToPremium()
                    else scope.launch { preferences.setVibrateIndicatorEnabled(it) }
                }
            )
            AppRowDivider()
            AppToggleRow(
                title = "Volume HUD",
                subtitle = "Show live ringer and media volume levels",
                glyph = AppleGlyph.Speaker,
                checked = showVolume,
                trailingLabel = if (soundLocked) "PRO" else null,
                onCheckedChange = {
                    if (soundLocked) onNavigateToPremium()
                    else scope.launch { preferences.setShowVolumeIndicator(it) }
                }
            )
            AppRowDivider()
            AppToggleRow(
                title = "Haptic feedback",
                subtitle = "Respond to island taps, holds, and swipe dismissals",
                glyph = AppleGlyph.Waveform,
                checked = hapticEnabled,
                trailingLabel = if (soundLocked) "PRO" else null,
                onCheckedChange = {
                    if (soundLocked) onNavigateToPremium()
                    else scope.launch { preferences.setHapticFeedbackEnabled(it) }
                }
            )
        }

        AppSectionSpacer()

        AppSectionTitle("Pulse animation")
        AppCard(modifier = Modifier.appReveal(3)) {
            AppSlider(
                title = "Pulse duration",
                value = pulseDuration.toFloat(),
                valueRange = 100f..1000f,
                valueFormatter = { "${it.toInt()} ms" },
                enabled = !soundLocked,
                trailingLabel = if (soundLocked) "PRO" else null,
                onLockedInteraction = onNavigateToPremium,
                onValueChange = { value -> scope.launch { preferences.setPulseDuration(value.toInt()) } }
            )
            Spacer(modifier = Modifier.height(AppTheme.spacing.xl))
            AppSlider(
                title = "Pulse scale",
                value = pulseScale,
                valueRange = 1.0f..1.3f,
                valueFormatter = { "%.2fx".format(it) },
                enabled = !soundLocked,
                trailingLabel = if (soundLocked) "PRO" else null,
                onLockedInteraction = onNavigateToPremium,
                onValueChange = { value -> scope.launch { preferences.setPulseScale(value) } }
            )
        }

        AppFootnote("Sound events are observed locally. The island never changes your system volume by itself.")
    }
}
