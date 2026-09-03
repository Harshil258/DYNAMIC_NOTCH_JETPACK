package ai.emots.kishan_dynamic.ui.kit

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
 * The stage that showcases a live Dynamic Island. Gives the island a calm,
 * device-like backdrop so it never floats awkwardly on the page.
 */
@Composable
fun AppStage(
    modifier: Modifier = Modifier,
    caption: String? = null,
    minHeight: Dp = 200.dp,
    content: @Composable BoxScope.() -> Unit
) {
    val shape = RoundedCornerShape(AppTheme.radius.card)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(
                Brush.verticalGradient(
                    listOf(
                        AppTheme.colors.surfaceVariant,
                        AppTheme.colors.surface
                    )
                )
            )
            .border(0.5.dp, AppTheme.colors.border, shape)
            .padding(vertical = AppTheme.spacing.xl, horizontal = AppTheme.spacing.md),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = minHeight),
            contentAlignment = Alignment.Center,
            content = content
        )
        if (caption != null) {
            Spacer(modifier = Modifier.height(AppTheme.spacing.lg))
            AppText(
                text = caption,
                style = AppTheme.typography.caption,
                color = AppTheme.colors.textTertiary
            )
        }
    }
}

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
