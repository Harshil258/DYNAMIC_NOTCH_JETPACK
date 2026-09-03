package ai.emots.kishan_dynamic.ui.gallery

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.ColorLens
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.SmartButton
import androidx.compose.material.icons.rounded.TextFields
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.Widgets
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ai.emots.kishan_dynamic.ui.components.AtmosphericBackground
import ai.emots.kishan_dynamic.ui.components.AuroraBadge
import ai.emots.kishan_dynamic.ui.components.AuroraButton
import ai.emots.kishan_dynamic.ui.components.AuroraChip
import ai.emots.kishan_dynamic.ui.components.AuroraIconButton
import ai.emots.kishan_dynamic.ui.components.AuroraInput
import ai.emots.kishan_dynamic.ui.components.AuroraScreenHeader
import ai.emots.kishan_dynamic.ui.components.AuroraSectionHeader
import ai.emots.kishan_dynamic.ui.components.AuroraSegmentedControl
import ai.emots.kishan_dynamic.ui.components.AuroraSlider
import ai.emots.kishan_dynamic.ui.components.AuroraToggle
import ai.emots.kishan_dynamic.ui.components.ButtonVariant
import ai.emots.kishan_dynamic.ui.components.DynamicIslandPill
import ai.emots.kishan_dynamic.ui.components.GlassCard
import ai.emots.kishan_dynamic.ui.components.GlowCard
import ai.emots.kishan_dynamic.ui.components.IslandDemoState
import ai.emots.kishan_dynamic.ui.theme.AuroraIslandTheme
import ai.emots.kishan_dynamic.ui.theme.AuroraTheme

/**
 * Aurora Design System & Component Gallery.
 * Exhaustive visual catalogue of all tokens, widgets, and dynamic island morphing states.
 */
@Composable
fun ComponentGalleryScreen(
    isDarkTheme: Boolean = true,
    onToggleTheme: () -> Unit = {}
) {
    var islandState by remember { mutableStateOf(IslandDemoState.MusicCompact) }
    var toggleValue by remember { mutableStateOf(true) }
    var sliderValue by remember { mutableFloatStateOf(0.72f) }
    var selectedSegment by remember { mutableIntStateOf(0) }
    var inputValue by remember { mutableStateOf("Aurora Dynamic Notch") }

    AtmosphericBackground {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = AuroraTheme.spacing.screenGutter)
                .padding(bottom = AuroraTheme.spacing.epic),
            verticalArrangement = Arrangement.spacedBy(AuroraTheme.spacing.xl)
        ) {
            // Header with Theme Switcher Action
            AuroraScreenHeader(
                title = "Aurora Gallery",
                subtitle = "Design System & Component Showcase",
                trailingAction = {
                    AuroraIconButton(
                        icon = if (isDarkTheme) Icons.Rounded.LightMode else Icons.Rounded.DarkMode,
                        onClick = onToggleTheme
                    )
                }
            )

            // =================================================================
            // 1. INTERACTIVE ISLAND PLAYGROUND
            // =================================================================
            AuroraSectionHeader(
                title = "Morphing Dynamic Island",
                icon = Icons.Rounded.AutoAwesome,
                badgeText = "SPRING PHYSICS"
            )

            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "TAP ISLAND TO TOGGLE EXPANSION",
                        style = AuroraTheme.typography.labelSmall,
                        color = AuroraTheme.colors.textTertiary
                    )

                    Spacer(modifier = Modifier.height(AuroraTheme.spacing.lg))

                    // Centerpiece Live Dynamic Island
                    DynamicIslandPill(
                        state = islandState,
                        onTap = {
                            islandState = when (islandState) {
                                IslandDemoState.Minimal -> IslandDemoState.MusicCompact
                                IslandDemoState.MusicCompact -> IslandDemoState.MusicExpanded
                                IslandDemoState.MusicExpanded -> IslandDemoState.CallCompact
                                IslandDemoState.CallCompact -> IslandDemoState.CallExpanded
                                IslandDemoState.CallExpanded -> IslandDemoState.NotificationCompact
                                IslandDemoState.NotificationCompact -> IslandDemoState.ChargingCompact
                                IslandDemoState.ChargingCompact -> IslandDemoState.Minimal
                                else -> IslandDemoState.Minimal
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(AuroraTheme.spacing.xl))

                    // Horizontal State Simulation Chips
                    Text(
                        text = "SIMULATE STATE PRESETS",
                        style = AuroraTheme.typography.labelSmall,
                        color = AuroraTheme.colors.textTertiary,
                        modifier = Modifier.align(Alignment.Start)
                    )

                    Spacer(modifier = Modifier.height(AuroraTheme.spacing.sm))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(AuroraTheme.spacing.sm)
                    ) {
                        AuroraChip(
                            label = "Minimal",
                            selected = islandState == IslandDemoState.Minimal,
                            onClick = { islandState = IslandDemoState.Minimal }
                        )
                        AuroraChip(
                            label = "Music (Compact)",
                            selected = islandState == IslandDemoState.MusicCompact,
                            onClick = { islandState = IslandDemoState.MusicCompact }
                        )
                        AuroraChip(
                            label = "Music (Expanded)",
                            selected = islandState == IslandDemoState.MusicExpanded,
                            onClick = { islandState = IslandDemoState.MusicExpanded }
                        )
                        AuroraChip(
                            label = "Call (Split)",
                            selected = islandState == IslandDemoState.CallCompact,
                            onClick = { islandState = IslandDemoState.CallCompact }
                        )
                        AuroraChip(
                            label = "Call (Expanded)",
                            selected = islandState == IslandDemoState.CallExpanded,
                            onClick = { islandState = IslandDemoState.CallExpanded }
                        )
                        AuroraChip(
                            label = "Notification",
                            selected = islandState == IslandDemoState.NotificationCompact,
                            onClick = { islandState = IslandDemoState.NotificationCompact }
                        )
                        AuroraChip(
                            label = "Charging",
                            selected = islandState == IslandDemoState.ChargingCompact,
                            onClick = { islandState = IslandDemoState.ChargingCompact }
                        )
                    }
                }
            }

            // =================================================================
            // 2. COLOR PALETTE & TOKENS
            // =================================================================
            AuroraSectionHeader(
                title = "Semantic Color Tokens",
                icon = Icons.Rounded.Palette
            )

            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(AuroraTheme.spacing.md)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        ColorSwatch(label = "Primary", color = AuroraTheme.colors.primary)
                        ColorSwatch(label = "Lavender", color = AuroraTheme.colors.lavender)
                        ColorSwatch(label = "Aura Pink", color = AuroraTheme.colors.auraPink)
                        ColorSwatch(label = "Cyan", color = AuroraTheme.colors.cyan)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        ColorSwatch(label = "Success", color = AuroraTheme.colors.success)
                        ColorSwatch(label = "Warning", color = AuroraTheme.colors.warning)
                        ColorSwatch(label = "Error", color = AuroraTheme.colors.error)
                        ColorSwatch(label = "Text Sec", color = AuroraTheme.colors.textSecondary)
                    }
                }
            }

            // =================================================================
            // 3. FROSTED CARDS & ELEVATIONS
            // =================================================================
            AuroraSectionHeader(
                title = "Glassmorphic Cards",
                icon = Icons.Rounded.Widgets
            )

            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    Text(
                        text = "Standard Frosted Glass Card",
                        style = AuroraTheme.typography.titleMedium,
                        color = AuroraTheme.colors.textPrimary
                    )
                    Spacer(modifier = Modifier.height(AuroraTheme.spacing.xs))
                    Text(
                        text = "Semi-transparent white fill (7% alpha) with hairline specular highlight border and continuous squircle corners.",
                        style = AuroraTheme.typography.bodySmall,
                        color = AuroraTheme.colors.textSecondary
                    )
                }
            }

            GlowCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "✨ Glowing Ambient Card",
                            style = AuroraTheme.typography.titleMedium,
                            color = AuroraTheme.colors.primary
                        )
                        Spacer(modifier = Modifier.height(AuroraTheme.spacing.xs))
                        Text(
                            text = "Dynamic ambient shadow bloom with gradient active stroke.",
                            style = AuroraTheme.typography.bodySmall,
                            color = AuroraTheme.colors.textSecondary
                        )
                    }
                    AuroraBadge(text = "PRO")
                }
            }

            // =================================================================
            // 4. BUTTONS & ACTIONS
            // =================================================================
            AuroraSectionHeader(
                title = "Buttons & Touch Targets",
                icon = Icons.Rounded.SmartButton
            )

            Column(verticalArrangement = Arrangement.spacedBy(AuroraTheme.spacing.md)) {
                AuroraButton(
                    onClick = {},
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = Icons.Rounded.AutoAwesome
                ) {
                    Text(
                        text = "Primary Gradient Button",
                        style = AuroraTheme.typography.labelLarge,
                        color = Color.White
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(AuroraTheme.spacing.md)
                ) {
                    AuroraButton(
                        onClick = {},
                        variant = ButtonVariant.Secondary,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Secondary Glass",
                            style = AuroraTheme.typography.labelMedium,
                            color = AuroraTheme.colors.textPrimary
                        )
                    }

                    AuroraButton(
                        onClick = {},
                        variant = ButtonVariant.Ghost,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Ghost Action",
                            style = AuroraTheme.typography.labelMedium,
                            color = AuroraTheme.colors.primary
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(AuroraTheme.spacing.md)
                ) {
                    AuroraButton(
                        onClick = {},
                        variant = ButtonVariant.Success,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = "Grant All", style = AuroraTheme.typography.labelMedium, color = Color.White)
                    }
                    AuroraButton(
                        onClick = {},
                        variant = ButtonVariant.Danger,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = "Disconnect", style = AuroraTheme.typography.labelMedium, color = Color.White)
                    }
                }
            }

            // =================================================================
            // 5. CONTROLS, TOGGLES & SLIDERS
            // =================================================================
            AuroraSectionHeader(
                title = "Interactive Controls",
                icon = Icons.Rounded.Tune
            )

            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(AuroraTheme.spacing.lg)) {
                    // Segmented Control
                    AuroraSegmentedControl(
                        items = listOf("Geometry", "Audio", "Behaviors"),
                        selectedIndex = selectedSegment,
                        onItemSelected = { selectedSegment = it }
                    )

                    // Toggle Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Volume Pulse Scale",
                                style = AuroraTheme.typography.titleSmall,
                                color = AuroraTheme.colors.textPrimary
                            )
                            Text(
                                text = "Enlarge notch when hardware buttons pressed",
                                style = AuroraTheme.typography.bodySmall,
                                color = AuroraTheme.colors.textSecondary
                            )
                        }
                        AuroraToggle(
                            checked = toggleValue,
                            onCheckedChange = { toggleValue = it }
                        )
                    }

                    // Slider Row
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Display Duration",
                                style = AuroraTheme.typography.titleSmall,
                                color = AuroraTheme.colors.textPrimary
                            )
                            Text(
                                text = "${(sliderValue * 10).toInt()} seconds",
                                style = AuroraTheme.typography.monoNumbers,
                                color = AuroraTheme.colors.primary
                            )
                        }
                        AuroraSlider(
                            value = sliderValue,
                            onValueChange = { sliderValue = it }
                        )
                    }

                    // Input Field
                    AuroraInput(
                        value = inputValue,
                        onValueChange = { inputValue = it },
                        placeholder = "Enter custom notch title..."
                    )
                }
            }

            // =================================================================
            // 6. TYPOGRAPHY SCALE
            // =================================================================
            AuroraSectionHeader(
                title = "Typography Scale",
                icon = Icons.Rounded.TextFields
            )

            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(AuroraTheme.spacing.xs)) {
                    Text(text = "Display Large (34sp)", style = AuroraTheme.typography.displayLarge, color = AuroraTheme.colors.textPrimary)
                    Text(text = "Headline Large (24sp)", style = AuroraTheme.typography.headlineLarge, color = AuroraTheme.colors.textPrimary)
                    Text(text = "Title Large (16sp)", style = AuroraTheme.typography.titleLarge, color = AuroraTheme.colors.textPrimary)
                    Text(text = "Body Medium (14sp) • Clean & readable body hierarchy", style = AuroraTheme.typography.bodyMedium, color = AuroraTheme.colors.textSecondary)
                    Text(text = "LABEL SMALL (10sp) • ALL CAPS METRICS", style = AuroraTheme.typography.labelSmall, color = AuroraTheme.colors.textTertiary)
                    Text(text = "MONO NUMBERS: 12:45 • 85% • 120Hz", style = AuroraTheme.typography.monoNumbers, color = AuroraTheme.colors.cyan)
                }
            }
        }
    }
}

@Composable
private fun ColorSwatch(label: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(color)
                .border(width = 1.dp, color = Color(0x33FFFFFF), shape = CircleShape)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = AuroraTheme.typography.labelSmall,
            color = AuroraTheme.colors.textSecondary
        )
    }
}

// =============================================================================
// PREVIEWS
// =============================================================================

@Preview(name = "Gallery Screen - Dark Mode", showBackground = true)
@Composable
private fun GalleryScreenDarkPreview() {
    AuroraIslandTheme(darkTheme = true) {
        ComponentGalleryScreen(isDarkTheme = true)
    }
}

@Preview(name = "Gallery Screen - Light Mode", showBackground = true)
@Composable
private fun GalleryScreenLightPreview() {
    AuroraIslandTheme(darkTheme = false) {
        ComponentGalleryScreen(isDarkTheme = false)
    }
}
