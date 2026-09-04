package ai.emots.kishan_dynamic.ui.kit

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ai.emots.kishan_dynamic.ui.components.AppText
import ai.emots.kishan_dynamic.ui.components.AppleGlyph
import ai.emots.kishan_dynamic.ui.components.AppleIcon
import ai.emots.kishan_dynamic.ui.theme.AppTheme

// =============================================================================
// APP KIT — one single, consistent visual language for every screen.
//
// Rules enforced by this file:
//  * Every colour comes from AppTheme.colors (works in light AND dark).
//  * Every screen has the same background, gutters, insets and rhythm.
//  * Content is grouped into cards; rows inside a card share one divider style.
//  * Nothing is ever hidden behind the status bar or the navigation dock.
// =============================================================================

/** Static, cheap, calm background: base colour + one soft accent bloom. */
@Composable
fun AppBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val colors = AppTheme.colors
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val bloom = Brush.radialGradient(
                colors = listOf(
                    colors.accent.copy(alpha = 0.10f),
                    Color.Transparent
                ),
                center = Offset(size.width * 0.5f, size.height * 0.02f),
                radius = size.width * 0.95f
            )
            drawRect(brush = bloom, size = Size(size.width, size.height))
        }
        content()
    }
}

/**
 * The single scaffold every screen uses.
 *
 * @param bottomInset extra bottom space, e.g. for the floating navigation dock.
 */
@Composable
fun AppScreen(
    modifier: Modifier = Modifier,
    bottomInset: Dp = 0.dp,
    horizontalGutter: Dp? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val gutter = horizontalGutter ?: AppTheme.layout.screenGutter
    AppBackground(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 720.dp)
                .align(Alignment.TopCenter)
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = gutter)
                .padding(bottom = bottomInset + AppTheme.spacing.xxxl),
            content = content
        )
    }
}

/** Large editorial title used by the four root tabs. */
@Composable
fun AppLargeTitle(
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier,
    trailing: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = AppTheme.spacing.xxl, bottom = AppTheme.spacing.xxl),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            AppText(
                text = title,
                style = AppTheme.typography.h1,
                color = AppTheme.colors.textPrimary
            )
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(AppTheme.spacing.xs))
                AppText(
                    text = subtitle,
                    style = AppTheme.typography.bodySmall,
                    color = AppTheme.colors.textSecondary
                )
            }
        }
        if (trailing != null) {
            Spacer(modifier = Modifier.size(AppTheme.spacing.md))
            trailing()
        }
    }
}

/** Compact top bar with a real 44dp touch target back button, used by sub-screens. */
@Composable
fun AppTopBar(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    trailing: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = AppTheme.spacing.md, bottom = AppTheme.spacing.xl),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AppIconButton(
            glyph = AppleGlyph.ChevronLeft,
            onClick = onBack,
            contentDescription = "Back"
        )
        Spacer(modifier = Modifier.size(AppTheme.spacing.md))
        Column(modifier = Modifier.weight(1f)) {
            AppText(
                text = title,
                style = AppTheme.typography.h2,
                color = AppTheme.colors.textPrimary,
                maxLines = 2
            )
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(AppTheme.spacing.xxs))
                AppText(
                    text = subtitle,
                    style = AppTheme.typography.bodySmall,
                    color = AppTheme.colors.textSecondary,
                    maxLines = 2
                )
            }
        }
        if (trailing != null) {
            Spacer(modifier = Modifier.size(AppTheme.spacing.md))
            trailing()
        }
    }
}

@Composable
fun AppIconButton(
    glyph: AppleGlyph,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    tint: Color? = null
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.90f else 1f,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = Spring.StiffnessMedium),
        label = "icon_button_scale"
    )
    Box(
        modifier = modifier
            .size(44.dp)
            .scale(scale)
            .clip(CircleShape)
            .background(AppTheme.colors.surface)
            .border(0.5.dp, AppTheme.colors.border, CircleShape)
            .clickable(interactionSource = interaction, indication = null, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        AppleIcon(
            glyph = glyph,
            tint = tint ?: AppTheme.colors.textPrimary,
            size = 17.dp
        )
    }
}

/** Small uppercase label that opens a group of cards. */
@Composable
fun AppSectionTitle(
    text: String,
    modifier: Modifier = Modifier
) {
    AppText(
        text = text.uppercase(),
        style = AppTheme.typography.caption,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 1.1.sp,
        color = AppTheme.colors.textTertiary,
        modifier = modifier.padding(
            start = AppTheme.spacing.xs,
            bottom = AppTheme.spacing.sm
        )
    )
}

/** Consistent vertical rhythm between sections. */
@Composable
fun AppSectionSpacer(modifier: Modifier = Modifier) {
    Spacer(modifier = modifier.height(AppTheme.layout.sectionGap + AppTheme.spacing.sm))
}

/** The one and only surface container in the app. */
@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    padding: Dp? = null,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(AppTheme.radius.card)
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed && onClick != null) 0.99f else 1f,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessMedium),
        label = "card_scale"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .scale(scale)
            .clip(shape)
            .background(AppTheme.colors.surface)
            .border(0.5.dp, AppTheme.colors.border, shape)
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = interaction,
                        indication = null,
                        onClick = onClick
                    )
                } else Modifier
            )
            .padding(padding ?: AppTheme.layout.cardPadding),
        content = content
    )
}

/** Card whose children are rows separated by inset hairlines (iOS grouped list). */
@Composable
fun AppListCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(AppTheme.radius.card)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(AppTheme.colors.surface)
            .border(0.5.dp, AppTheme.colors.border, shape)
            .padding(vertical = AppTheme.spacing.xs),
        content = content
    )
}

@Composable
fun AppRowDivider(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = AppTheme.layout.cardPadding)
            .height(0.5.dp)
            .background(AppTheme.colors.border)
    )
}

/**
 * Navigational row: title, optional subtitle, optional value, chevron.
 * Guaranteed 56dp minimum height so touch targets are always correct.
 */
@Composable
fun AppNavRow(
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    value: String? = null,
    glyph: AppleGlyph? = null,
    accent: Color? = null
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(AppTheme.radius.md))
            .background(
                if (pressed) AppTheme.colors.surfaceVariant else Color.Transparent
            )
            .clickable(interactionSource = interaction, indication = null, onClick = onClick)
            .heightIn(min = 56.dp)
            .padding(
                horizontal = AppTheme.layout.cardPadding,
                vertical = AppTheme.spacing.md
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (glyph != null) {
            AppGlyphBadge(glyph = glyph, tint = accent ?: AppTheme.colors.textSecondary)
            Spacer(modifier = Modifier.size(AppTheme.spacing.md))
        }
        Column(modifier = Modifier.weight(1f)) {
            AppText(
                text = title,
                style = AppTheme.typography.body,
                fontWeight = FontWeight.Medium,
                color = AppTheme.colors.textPrimary
            )
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(AppTheme.spacing.xxs))
                AppText(
                    text = subtitle,
                    style = AppTheme.typography.bodySmall,
                    color = AppTheme.colors.textSecondary
                )
            }
        }
        if (value != null) {
            AppText(
                text = value,
                style = AppTheme.typography.bodySmall,
                color = AppTheme.colors.textTertiary,
                modifier = Modifier.padding(start = AppTheme.spacing.sm)
            )
        }
        Spacer(modifier = Modifier.size(AppTheme.spacing.sm))
        AppleIcon(
            glyph = AppleGlyph.ChevronRight,
            tint = AppTheme.colors.textTertiary,
            size = 13.dp
        )
    }
}

/** Row with a title, explanation and a trailing switch. */
@Composable
fun AppToggleRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    glyph: AppleGlyph? = null,
    enabled: Boolean = true
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .padding(
                horizontal = AppTheme.layout.cardPadding,
                vertical = AppTheme.spacing.md
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (glyph != null) {
            AppGlyphBadge(glyph = glyph, tint = AppTheme.colors.textSecondary)
            Spacer(modifier = Modifier.size(AppTheme.spacing.md))
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(end = AppTheme.spacing.md)
        ) {
            AppText(
                text = title,
                style = AppTheme.typography.body,
                fontWeight = FontWeight.Medium,
                color = if (enabled) AppTheme.colors.textPrimary else AppTheme.colors.disabled
            )
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(AppTheme.spacing.xxs))
                AppText(
                    text = subtitle,
                    style = AppTheme.typography.bodySmall,
                    color = AppTheme.colors.textSecondary
                )
            }
        }
        AppSwitch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled
        )
    }
}

/** Read-only key/value row. */
@Composable
fun AppInfoRow(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: Color? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .padding(
                horizontal = AppTheme.layout.cardPadding,
                vertical = AppTheme.spacing.sm
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AppText(
            text = title,
            style = AppTheme.typography.body,
            color = AppTheme.colors.textSecondary,
            modifier = Modifier.weight(1f)
        )
        AppText(
            text = value,
            style = AppTheme.typography.body,
            fontWeight = FontWeight.Medium,
            color = valueColor ?: AppTheme.colors.textPrimary
        )
    }
}

/** Rounded square icon container used by rows and tiles. */
@Composable
fun AppGlyphBadge(
    glyph: AppleGlyph,
    modifier: Modifier = Modifier,
    tint: Color? = null,
    size: Dp = 34.dp
) {
    val resolved = tint ?: AppTheme.colors.textSecondary
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(size / 3f))
            .background(resolved.copy(alpha = 0.14f)),
        contentAlignment = Alignment.Center
    ) {
        AppleIcon(glyph = glyph, tint = resolved, size = size * 0.48f)
    }
}

/** Status pill: a dot plus a short label. */
@Composable
fun AppStatusPill(
    text: String,
    color: Color,
    modifier: Modifier = Modifier,
    showDot: Boolean = true
) {
    Row(
        modifier = modifier
            .clip(CircleShape)
            .background(color.copy(alpha = 0.14f))
            .padding(horizontal = AppTheme.spacing.md, vertical = AppTheme.spacing.xs + 1.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs + 2.dp)
    ) {
        if (showDot) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(color)
            )
        }
        AppText(
            text = text,
            style = AppTheme.typography.caption,
            fontWeight = FontWeight.Medium,
            color = color,
            maxLines = 1
        )
    }
}

/**
 * The stage that showcases a live Dynamic Island.
 *
 * Designed as an authentic full-bleed Apple iOS 17 device viewport:
 * Spans full width (edge-to-edge), styled with an authentic multi-layered
 * iOS 17 wallpaper (midnight blue, twilight sapphire, and ambient violet bloom),
 * framing the jet-black Dynamic Island with crisp contrast.
 *
 * Height smoothly and dynamically adapts to the active island presentation
 * (116dp for compact/idle, 156dp for 96dp capsules, 198dp for 144dp activities,
 * and 238dp for full sheets), completely eliminating empty black dead space.
 */
@Composable
fun AppStage(
    modifier: Modifier = Modifier,
    caption: String? = null,
    targetHeight: Dp = 116.dp,
    minHeight: Dp? = null,
    showStatusBar: Boolean = true,
    content: @Composable BoxScope.() -> Unit
) {
    val effectiveTargetHeight = minHeight ?: targetHeight
    val animatedStageHeight by animateDpAsState(
        targetValue = effectiveTargetHeight,
        animationSpec = spring(
            dampingRatio = 0.82f,
            stiffness = 380f
        ),
        label = "stage_height"
    )

    val stageShape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(animatedStageHeight)
                .shadow(
                    elevation = 14.dp,
                    shape = stageShape,
                    ambientColor = Color.Black.copy(alpha = 0.40f),
                    spotColor = Color.Black.copy(alpha = 0.60f)
                )
                .clip(stageShape)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFF0A0C1A),
                            Color(0xFF121736),
                            Color(0xFF1B163B),
                            Color(0xFF090A14)
                        )
                    )
                )
                .border(
                    width = 1.dp,
                    brush = Brush.verticalGradient(
                        listOf(
                            Color.White.copy(alpha = 0.16f),
                            Color.White.copy(alpha = 0.04f)
                        )
                    ),
                    shape = stageShape
                ),
            contentAlignment = Alignment.TopCenter
        ) {
            // Authentic Apple iOS 17 wallpaper bloom
            Canvas(modifier = Modifier.fillMaxSize()) {
                // Sapphire / Indigo atmospheric glow (left)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF2E5BFF).copy(alpha = 0.38f),
                            Color.Transparent
                        ),
                        center = Offset(size.width * 0.22f, size.height * 0.25f),
                        radius = size.width * 0.65f
                    )
                )
                // Magenta / Violet silk bloom (right)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFB328FF).copy(alpha = 0.32f),
                            Color.Transparent
                        ),
                        center = Offset(size.width * 0.82f, size.height * 0.38f),
                        radius = size.width * 0.70f
                    )
                )
                // Soft top specular rim
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.08f),
                            Color.Transparent
                        ),
                        startY = 0f,
                        endY = size.height * 0.45f
                    )
                )
            }

            // Authentic Apple iOS Status Bar framing the island (fades out when expanded)
            val statusBarAlpha by animateFloatAsState(
                targetValue = if (showStatusBar) 1f else 0f,
                animationSpec = tween(180),
                label = "status_bar_alpha"
            )
            if (statusBarAlpha > 0.01f) {
                IosStatusBar(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 14.dp)
                        .graphicsLayer { alpha = statusBarAlpha }
                )
            }

            // The island sits 11dp below the top edge of the display, exactly as on iPhone
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 11.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                content()
            }
        }

        if (caption != null) {
            Spacer(modifier = Modifier.height(AppTheme.spacing.sm))
            AppText(
                text = caption,
                style = AppTheme.typography.caption,
                color = AppTheme.colors.textTertiary
            )
        }
    }
}

/**
 * Authentic Apple iOS Status Bar: "9:41" on the left, Signal bars + Battery on the right.
 * Omit carrier text ("5G") during active island presentation to match iOS 17 HIG.
 */
@Composable
fun IosStatusBar(
    modifier: Modifier = Modifier,
    time: String = "9:41"
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Left: Time (9:41 in SF Pro bold weight)
        AppText(
            text = time,
            style = AppTheme.typography.islandSubtitle.copy(fontSize = 14.sp),
            fontWeight = FontWeight.Bold,
            color = Color.White.copy(alpha = 0.92f)
        )

        // Right: Signal Bars + iOS Battery
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 4 Signal Bars
            SignalBars(modifier = Modifier.size(width = 17.dp, height = 11.5.dp))

            // iOS Battery Glyph
            IosStatusBattery(modifier = Modifier.size(width = 24.dp, height = 11.5.dp))
        }
    }
}

@Composable
private fun SignalBars(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val barWidth = size.width / 7f
        val gap = barWidth
        val heights = listOf(0.35f, 0.55f, 0.78f, 1.0f)
        for (i in 0..3) {
            val barHeight = size.height * heights[i]
            val x = i * (barWidth + gap)
            val y = size.height - barHeight
            drawRoundRect(
                color = Color.White.copy(alpha = 0.9f),
                topLeft = Offset(x, y),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(1.5.dp.toPx(), 1.5.dp.toPx())
            )
        }
    }
}

@Composable
private fun IosStatusBattery(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val stroke = 1.2.dp.toPx()
        val bodyWidth = size.width - 2.5.dp.toPx()
        val bodyHeight = size.height
        val corner = 3.dp.toPx()

        // Shell
        drawRoundRect(
            color = Color.White.copy(alpha = 0.35f),
            topLeft = Offset(0f, 0f),
            size = Size(bodyWidth, bodyHeight),
            cornerRadius = CornerRadius(corner, corner),
            style = Stroke(width = stroke)
        )

        // Inner solid fill
        val innerPad = stroke + 1.2.dp.toPx()
        val innerWidth = (bodyWidth - innerPad * 2) * 0.92f
        val innerHeight = bodyHeight - innerPad * 2
        drawRoundRect(
            color = Color.White.copy(alpha = 0.95f),
            topLeft = Offset(innerPad, innerPad),
            size = Size(innerWidth, innerHeight),
            cornerRadius = CornerRadius(1.5.dp.toPx(), 1.5.dp.toPx())
        )

        // Positive Terminal Nub
        val nubWidth = 1.5.dp.toPx()
        val nubHeight = bodyHeight * 0.40f
        drawRoundRect(
            color = Color.White.copy(alpha = 0.45f),
            topLeft = Offset(bodyWidth + 1.dp.toPx(), (bodyHeight - nubHeight) / 2f),
            size = Size(nubWidth, nubHeight),
            cornerRadius = CornerRadius(1.dp.toPx(), 1.dp.toPx())
        )
    }
}

/** Wallpaper tint used behind the island inside [AppStage]. */
private val StageBloom = Color(0xFF4C6FFF)

/** Short explanatory paragraph shown under a card group. */
@Composable
fun AppFootnote(
    text: String,
    modifier: Modifier = Modifier
) {
    AppText(
        text = text,
        style = AppTheme.typography.caption,
        color = AppTheme.colors.textTertiary,
        modifier = modifier.padding(
            start = AppTheme.spacing.xs,
            end = AppTheme.spacing.xs,
            top = AppTheme.spacing.sm
        )
    )
}
