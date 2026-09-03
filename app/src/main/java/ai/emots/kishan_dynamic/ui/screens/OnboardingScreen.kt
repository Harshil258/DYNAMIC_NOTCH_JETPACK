package ai.emots.kishan_dynamic.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ai.emots.kishan_dynamic.ui.components.AppText
import ai.emots.kishan_dynamic.ui.components.AppleGlyph
import ai.emots.kishan_dynamic.ui.components.DynamicIslandPill
import ai.emots.kishan_dynamic.ui.components.IslandDemoState
import ai.emots.kishan_dynamic.ui.kit.AppBackground
import ai.emots.kishan_dynamic.ui.kit.AppButton
import ai.emots.kishan_dynamic.ui.kit.AppButtonStyle
import ai.emots.kishan_dynamic.ui.theme.AppTheme

private data class OnboardingSlide(
    val eyebrow: String,
    val title: String,
    val subtitle: String,
    val pillState: IslandDemoState
)

@Composable
fun OnboardingScreen(
    onComplete: () -> Unit = {},
    onSkip: () -> Unit = {}
) {
    var currentPage by remember { mutableIntStateOf(0) }

    val slides = remember {
        listOf(
            OnboardingSlide(
                eyebrow = "Alerts",
                title = "A living notch",
                subtitle = "Your camera cutout becomes a hub for notifications, timers and alerts.",
                pillState = IslandDemoState.NotificationCompact
            ),
            OnboardingSlide(
                eyebrow = "Media",
                title = "Music at a glance",
                subtitle = "Album art, a live visualiser and playback controls, right at the top of your screen.",
                pillState = IslandDemoState.MusicCompact
            ),
            OnboardingSlide(
                eyebrow = "Controls",
                title = "Shortcuts on tap",
                subtitle = "Volume, torch, Wi-Fi and more without ever leaving the app you're in.",
                pillState = IslandDemoState.MusicExpanded
            )
        )
    }

    val slide = slides[currentPage]
    val isLast = currentPage == slides.lastIndex

    AppBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 640.dp)
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = AppTheme.layout.screenGutter)
                .padding(vertical = AppTheme.spacing.lg),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top bar: brand + skip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm)
                ) {
                    Box(
                        modifier = Modifier
                            .size(width = 18.dp, height = 10.dp)
                            .clip(RoundedCornerShape(AppTheme.radius.pill))
                            .background(AppTheme.colors.textPrimary)
                    )
                    AppText(
                        text = "Dynamic Island",
                        style = AppTheme.typography.bodySmall,
                        color = AppTheme.colors.textSecondary
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(AppTheme.radius.pill))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            enabled = !isLast,
                            onClick = onSkip
                        )
                        .padding(horizontal = AppTheme.spacing.md, vertical = AppTheme.spacing.sm)
                ) {
                    AppText(
                        text = if (isLast) "" else "Skip",
                        style = AppTheme.typography.button,
                        color = AppTheme.colors.textSecondary
                    )
                }
            }

            // Stage
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                DynamicIslandPill(
                    state = slide.pillState,
                    onTap = { currentPage = (currentPage + 1) % slides.size }
                )

                Spacer(modifier = Modifier.height(AppTheme.spacing.xxxl + AppTheme.spacing.lg))

                AnimatedContent(
                    targetState = slide,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "slide_copy"
                ) { current ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(AppTheme.colors.accent.copy(alpha = 0.14f))
                                .padding(
                                    horizontal = AppTheme.spacing.md,
                                    vertical = AppTheme.spacing.xs
                                )
                        ) {
                            AppText(
                                text = current.eyebrow,
                                style = AppTheme.typography.caption,
                                color = AppTheme.colors.accent
                            )
                        }

                        Spacer(modifier = Modifier.height(AppTheme.spacing.lg))

                        AppText(
                            text = current.title,
                            style = AppTheme.typography.h1,
                            color = AppTheme.colors.textPrimary,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(AppTheme.spacing.sm))

                        AppText(
                            text = current.subtitle,
                            style = AppTheme.typography.body,
                            color = AppTheme.colors.textSecondary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = AppTheme.spacing.lg)
                        )
                    }
                }
            }

            // Pagination + CTA
            Row(
                horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs + 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                slides.indices.forEach { index ->
                    val isSelected = index == currentPage
                    val dotWidth by animateDpAsState(
                        targetValue = if (isSelected) 24.dp else 8.dp,
                        animationSpec = spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessMediumLow),
                        label = "dot_width"
                    )
                    Box(
                        modifier = Modifier
                            .size(width = dotWidth, height = 8.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) AppTheme.colors.textPrimary
                                else AppTheme.colors.surfaceElevated
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(AppTheme.spacing.xl))

            AppButton(
                text = if (isLast) "Get started" else "Continue",
                glyph = if (isLast) AppleGlyph.Sparkles else AppleGlyph.ChevronRight,
                style = AppButtonStyle.Primary,
                onClick = {
                    if (isLast) onComplete() else currentPage++
                }
            )
        }
    }
}
