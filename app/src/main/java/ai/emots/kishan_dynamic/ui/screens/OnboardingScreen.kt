package ai.emots.kishan_dynamic.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowForward
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ai.emots.kishan_dynamic.ui.components.AuroraButton
import ai.emots.kishan_dynamic.ui.components.DynamicIslandPill
import ai.emots.kishan_dynamic.ui.components.GlassCard
import ai.emots.kishan_dynamic.ui.components.IslandDemoState
import ai.emots.kishan_dynamic.ui.theme.AuroraIslandTheme
import ai.emots.kishan_dynamic.ui.theme.AuroraMotion
import ai.emots.kishan_dynamic.ui.theme.AuroraTheme

data class OnboardingSlideData(
    val title: String,
    val subtitle: String,
    val pillState: IslandDemoState,
    val featurePill: String
)

/**
 * Cinematic Story Onboarding with a top morphing island mock that fluidly synchronizes with slide progress.
 */
@Composable
fun OnboardingScreen(
    onComplete: () -> Unit = {},
    onSkip: () -> Unit = {}
) {
    var currentPage by remember { mutableIntStateOf(0) }

    val slides = remember {
        listOf(
            OnboardingSlideData(
                title = "Fluid Notification Island",
                subtitle = "Turn your camera notch into a responsive, shape-shifting hub for notifications, timers, and alerts.",
                pillState = IslandDemoState.NotificationCompact,
                featurePill = "INTELLIGENT ALERTS"
            ),
            OnboardingSlideData(
                title = "Live Media & Equalizer",
                subtitle = "Control your favorite music streaming apps with real-time waveform spectrums directly anchored to your display.",
                pillState = IslandDemoState.MusicCompact,
                featurePill = "AUDIO VISUALIZER"
            ),
            OnboardingSlideData(
                title = "Universal Quick Controls",
                subtitle = "Instant access to volume sliders, flashlight, favorite apps, and speed dial with a single swipe gesture.",
                pillState = IslandDemoState.MusicExpanded,
                featurePill = "INSTANT COMMAND"
            )
        )
    }

    val currentSlide = slides[currentPage]

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AuroraTheme.colors.rootBackgroundBrush)
    ) {
        // Atmospheric Ambient Background Blobs
        Box(
            modifier = Modifier
                .size(320.dp)
                .align(Alignment.TopCenter)
                .blur(AuroraTheme.elevation.glowBlurProminent)
                .background(AuroraTheme.colors.backgroundRadialBloom.copy(alpha = 0.5f))
        )
        Box(
            modifier = Modifier
                .size(260.dp)
                .align(Alignment.BottomEnd)
                .blur(AuroraTheme.elevation.glowBlurProminent)
                .background(AuroraTheme.colors.primary.copy(alpha = 0.25f))
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = AuroraTheme.spacing.screenGutter)
                .padding(vertical = AuroraTheme.spacing.md),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Action Bar with Skip Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(AuroraTheme.spacing.xs)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(AuroraTheme.colors.primary)
                    )
                    Text(
                        text = "AURORA ISLAND",
                        style = AuroraTheme.typography.labelSmall,
                        color = AuroraTheme.colors.textSecondary
                    )
                }

                if (currentPage < slides.size - 1) {
                    AuroraButton(
                        onClick = onSkip,
                        variant = ai.emots.kishan_dynamic.ui.components.ButtonVariant.Ghost,
                        shape = AuroraTheme.shapes.buttonSmall,
                        modifier = Modifier.height(36.dp)
                    ) {
                        Text(
                            text = "Skip",
                            style = AuroraTheme.typography.labelMedium,
                            color = AuroraTheme.colors.textSecondary
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.size(36.dp))
                }
            }

            Spacer(modifier = Modifier.height(AuroraTheme.spacing.xl))

            // Stage Area: Realistic simulated camera bezel with live morphing island
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                // Bezel Punch Hole Mock
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(Color.Black)
                )

                Spacer(modifier = Modifier.height(AuroraTheme.spacing.sm))

                // Morphing Island responding to current slide
                DynamicIslandPill(
                    state = currentSlide.pillState,
                    onTap = {
                        currentPage = (currentPage + 1) % slides.size
                    }
                )

                Spacer(modifier = Modifier.height(AuroraTheme.spacing.huge))

                // Feature Pill
                Box(
                    modifier = Modifier
                        .clip(AuroraTheme.shapes.pill)
                        .background(AuroraTheme.colors.primary.copy(alpha = 0.15f))
                        .padding(horizontal = AuroraTheme.spacing.md, vertical = AuroraTheme.spacing.xxs)
                ) {
                    Text(
                        text = currentSlide.featurePill,
                        style = AuroraTheme.typography.labelSmall,
                        color = AuroraTheme.colors.primary
                    )
                }

                Spacer(modifier = Modifier.height(AuroraTheme.spacing.md))

                // Animated Slide Copy (Title & Description)
                AnimatedContent(
                    targetState = currentSlide,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "slide_copy"
                ) { slide ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(horizontal = AuroraTheme.spacing.md)
                    ) {
                        Text(
                            text = slide.title,
                            style = AuroraTheme.typography.headlineLarge,
                            color = AuroraTheme.colors.textPrimary,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(AuroraTheme.spacing.sm))
                        Text(
                            text = slide.subtitle,
                            style = AuroraTheme.typography.bodyMedium,
                            color = AuroraTheme.colors.textSecondary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // Bottom Navigation Area: Pagination & CTA
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(AuroraTheme.spacing.lg)
            ) {
                // Animated Pill Pagination Dots
                Row(
                    horizontalArrangement = Arrangement.spacedBy(AuroraTheme.spacing.xs),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    slides.indices.forEach { index ->
                        val isSelected = index == currentPage
                        val dotWidth by animateDpAsState(
                            targetValue = if (isSelected) 28.dp else 8.dp,
                            animationSpec = AuroraMotion.ExpandSpringDp,
                            label = "dot_width"
                        )
                        Box(
                            modifier = Modifier
                                .size(width = dotWidth, height = 8.dp)
                                .clip(AuroraTheme.shapes.pill)
                                .background(
                                    if (isSelected) AuroraTheme.colors.primary
                                    else AuroraTheme.colors.controlTrack
                                )
                        )
                    }
                }

                // Continue / Launch Button
                AuroraButton(
                    onClick = {
                        if (currentPage < slides.size - 1) {
                            currentPage++
                        } else {
                            onComplete()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = if (currentPage == slides.size - 1) Icons.Rounded.AutoAwesome else Icons.Rounded.ArrowForward
                ) {
                    Text(
                        text = if (currentPage == slides.size - 1) "Launch Aurora Island" else "Continue",
                        style = AuroraTheme.typography.labelLarge,
                        color = Color.White
                    )
                }
            }
        }
    }
}

// =============================================================================
// PREVIEWS
// =============================================================================

@Preview(name = "Onboarding - Dark", showBackground = true, backgroundColor = 0xFF0B0B12)
@Composable
private fun OnboardingDarkPreview() {
    AuroraIslandTheme(darkTheme = true) {
        OnboardingScreen()
    }
}

@Preview(name = "Onboarding - Light", showBackground = true, backgroundColor = 0xFFEDE8F8)
@Composable
private fun OnboardingLightPreview() {
    AuroraIslandTheme(darkTheme = false) {
        OnboardingScreen()
    }
}
