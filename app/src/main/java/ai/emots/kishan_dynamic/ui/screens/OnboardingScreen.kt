package ai.emots.kishan_dynamic.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ai.emots.kishan_dynamic.data.analytics.AnalyticsEventNames
import ai.emots.kishan_dynamic.data.analytics.AnalyticsParamNames
import ai.emots.kishan_dynamic.data.analytics.AnalyticsTracker
import ai.emots.kishan_dynamic.data.analytics.FirebaseAnalyticsTracker
import ai.emots.kishan_dynamic.ui.components.AppText
import ai.emots.kishan_dynamic.ui.components.AppleGlyph
import ai.emots.kishan_dynamic.ui.components.DynamicIslandPill
import ai.emots.kishan_dynamic.ui.components.IslandDemoState
import ai.emots.kishan_dynamic.ui.kit.AppBackground
import ai.emots.kishan_dynamic.ui.kit.AppButton
import ai.emots.kishan_dynamic.ui.kit.AppButtonStyle
import ai.emots.kishan_dynamic.ui.theme.AppTheme
import ai.emots.kishan_dynamic.ui.theme.auroraActionGradient
import kotlinx.coroutines.launch

private data class OnboardingSlide(
    val eyebrow: String,
    val title: String,
    val subtitle: String,
    val pillState: IslandDemoState
)

@Composable
fun OnboardingScreen(
    onComplete: () -> Unit = {},
    onSkip: () -> Unit = {},
    analyticsTracker: AnalyticsTracker = FirebaseAnalyticsTracker(LocalContext.current)
) {
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

    val pagerState = rememberPagerState(initialPage = 0) { slides.size }
    val coroutineScope = rememberCoroutineScope()
    val isLast = pagerState.currentPage == slides.lastIndex

    BackHandler(enabled = pagerState.currentPage > 0) {
        coroutineScope.launch {
            pagerState.animateScrollToPage(pagerState.currentPage - 1)
        }
    }

    LaunchedEffect(Unit) {
        analyticsTracker.logEvent(AnalyticsEventNames.ONBOARDING_STARTED)
    }

    LaunchedEffect(pagerState.currentPage) {
        analyticsTracker.logEvent(
            AnalyticsEventNames.ONBOARDING_STEP_VIEWED,
            mapOf(
                AnalyticsParamNames.STEP_INDEX to pagerState.currentPage,
                AnalyticsParamNames.STEP_NAME to slides[pagerState.currentPage].eyebrow
            )
        )
    }

    AppBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 640.dp)
                .align(Alignment.TopCenter)
                .safeDrawingPadding()
                .padding(horizontal = AppTheme.layout.screenGutter)
                .padding(top = AppTheme.spacing.md, bottom = AppTheme.spacing.lg),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top bar: brand identity + skip button
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
                            .size(width = 20.dp, height = 10.dp)
                            .clip(RoundedCornerShape(AppTheme.radius.pill))
                            .background(AppTheme.colors.auroraActionGradient())
                    )
                    AppText(
                        text = "Aurora Dynamic Island",
                        style = AppTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = AppTheme.colors.textPrimary
                    )
                }

                Box(
                    modifier = Modifier
                        .heightIn(min = 48.dp)
                        .clip(RoundedCornerShape(AppTheme.radius.pill))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            enabled = !isLast,
                            onClick = {
                                analyticsTracker.logEvent(
                                    AnalyticsEventNames.ONBOARDING_COMPLETED,
                                    mapOf("skipped" to true, AnalyticsParamNames.STEP_INDEX to pagerState.currentPage)
                                )
                                onSkip()
                            }
                        )
                        .padding(horizontal = AppTheme.spacing.md, vertical = AppTheme.spacing.sm),
                    contentAlignment = Alignment.Center
                ) {
                    AppText(
                        text = if (isLast) "" else "Skip",
                        style = AppTheme.typography.button,
                        color = AppTheme.colors.textSecondary
                    )
                }
            }

            // Genuinely swipe-enabled horizontal pager with natural gesture handling
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                userScrollEnabled = true
            ) { page ->
                val slide = slides[page]
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = AppTheme.spacing.md)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    DynamicIslandPill(
                        state = slide.pillState,
                        onTap = {
                            coroutineScope.launch {
                                val next = (page + 1) % slides.size
                                pagerState.animateScrollToPage(next)
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(AppTheme.spacing.xl))

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
                            text = slide.eyebrow,
                            style = AppTheme.typography.caption,
                            color = AppTheme.colors.accent
                        )
                    }

                    Spacer(modifier = Modifier.height(AppTheme.spacing.md))

                    AppText(
                        text = slide.title,
                        style = AppTheme.typography.h1,
                        color = AppTheme.colors.textPrimary,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(AppTheme.spacing.sm))

                    AppText(
                        text = slide.subtitle,
                        style = AppTheme.typography.body,
                        color = AppTheme.colors.textSecondary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = AppTheme.spacing.md)
                    )
                }
            }

            // Pagination dots synchronized with pagerState and clickable
            Row(
                horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs + 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = AppTheme.spacing.md)
            ) {
                slides.indices.forEach { index ->
                    val isSelected = index == pagerState.currentPage
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
                                if (isSelected) AppTheme.colors.auroraCyan
                                else AppTheme.colors.surfaceElevated
                            )
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(index)
                                }
                            }
                    )
                }
            }

            Spacer(modifier = Modifier.height(AppTheme.spacing.xs))

            AppButton(
                text = if (isLast) "Get started" else "Continue",
                glyph = if (isLast) AppleGlyph.Sparkles else AppleGlyph.ChevronRight,
                style = if (isLast) AppButtonStyle.AuroraPro else AppButtonStyle.Primary,
                onClick = {
                    analyticsTracker.logEvent(
                        AnalyticsEventNames.ONBOARDING_STEP_COMPLETED,
                        mapOf(
                            AnalyticsParamNames.STEP_INDEX to pagerState.currentPage,
                            AnalyticsParamNames.STEP_NAME to slides[pagerState.currentPage].eyebrow
                        )
                    )
                    if (isLast) {
                        analyticsTracker.logEvent(
                            AnalyticsEventNames.ONBOARDING_COMPLETED,
                            mapOf("skipped" to false, AnalyticsParamNames.STEP_INDEX to pagerState.currentPage)
                        )
                        onComplete()
                    } else {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    }
                }
            )
        }
    }
}
