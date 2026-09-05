package ai.emots.kishan_dynamic.ui.kit

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import ai.emots.kishan_dynamic.ui.components.AppText
import ai.emots.kishan_dynamic.ui.components.AppleGlyph
import ai.emots.kishan_dynamic.ui.theme.AppTheme
import ai.emots.kishan_dynamic.ui.theme.surfaceGradient

/**
 * Centre modal used for short, focused decisions.
 * Consistent width, radius, header and dismiss affordance everywhere.
 */
@Composable
fun AppDialog(
    onDismiss: () -> Unit,
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    glyph: AppleGlyph? = null,
    accent: Color? = null,
    dismissOnOutsideTap: Boolean = true,
    showClose: Boolean = true,
    content: @Composable ColumnScope.() -> Unit
) {
    Dialog(
        onDismissRequest = { if (dismissOnOutsideTap) onDismiss() },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = dismissOnOutsideTap,
            dismissOnClickOutside = dismissOnOutsideTap
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(AppTheme.colors.scrim)
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(AppTheme.spacing.xl),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = modifier
                    .widthIn(max = 420.dp)
                    .fillMaxWidth()
                    .shadow(
                        elevation = AppTheme.elevation.dialog,
                        shape = RoundedCornerShape(AppTheme.radius.card),
                        ambientColor = AppTheme.colors.shadow.copy(alpha = 0.50f),
                        spotColor = AppTheme.colors.shadowStrong.copy(alpha = 0.60f)
                    )
                    .clip(RoundedCornerShape(AppTheme.radius.card))
                    .background(
                        brush = AppTheme.colors.surfaceGradient(),
                        shape = RoundedCornerShape(AppTheme.radius.card)
                    )
                    .border(
                        0.5.dp,
                        AppTheme.colors.border,
                        RoundedCornerShape(AppTheme.radius.card)
                    )
                    .verticalScroll(rememberScrollState())
                    .padding(AppTheme.spacing.xxl)
            ) {
                DialogHeader(
                    title = title,
                    subtitle = subtitle,
                    glyph = glyph,
                    accent = accent,
                    onClose = if (showClose) onDismiss else null
                )
                Spacer(modifier = Modifier.height(AppTheme.spacing.xl))
                content()
            }
        }
    }
}

/**
 * Bottom sheet used for longer, scrollable content such as lists and paywalls.
 */
@Composable
fun AppSheet(
    onDismiss: () -> Unit,
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    glyph: AppleGlyph? = null,
    accent: Color? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(AppTheme.colors.scrim)
                .statusBarsPadding(),
            contentAlignment = Alignment.BottomCenter
        ) {
            Column(
                modifier = modifier
                    .widthIn(max = 520.dp)
                    .fillMaxWidth()
                    .shadow(
                        elevation = AppTheme.elevation.sheet,
                        shape = RoundedCornerShape(
                            topStart = AppTheme.radius.card + 6.dp,
                            topEnd = AppTheme.radius.card + 6.dp
                        ),
                        ambientColor = AppTheme.colors.shadow.copy(alpha = 0.38f),
                        spotColor = AppTheme.colors.shadowStrong.copy(alpha = 0.50f)
                    )
                    .clip(
                        RoundedCornerShape(
                            topStart = AppTheme.radius.card + 6.dp,
                            topEnd = AppTheme.radius.card + 6.dp
                        )
                    )
                    .background(
                        brush = AppTheme.colors.surfaceGradient(),
                        shape = RoundedCornerShape(
                            topStart = AppTheme.radius.card + 6.dp,
                            topEnd = AppTheme.radius.card + 6.dp
                        )
                    )
                    .navigationBarsPadding()
                    .padding(
                        start = AppTheme.spacing.xxl,
                        end = AppTheme.spacing.xxl,
                        top = AppTheme.spacing.md,
                        bottom = AppTheme.spacing.xxl
                    )
            ) {
                // Grabber
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .size(width = 36.dp, height = 4.dp)
                        .clip(CircleShape)
                        .background(AppTheme.colors.textTertiary.copy(alpha = 0.5f))
                )

                Spacer(modifier = Modifier.height(AppTheme.spacing.lg))

                DialogHeader(
                    title = title,
                    subtitle = subtitle,
                    glyph = glyph,
                    accent = accent,
                    onClose = onDismiss
                )

                Spacer(modifier = Modifier.height(AppTheme.spacing.xl))

                Column(
                    modifier = Modifier
                        .heightIn(max = 520.dp)
                        .verticalScroll(rememberScrollState()),
                    content = content
                )
            }
        }
    }
}

@Composable
private fun DialogHeader(
    title: String,
    subtitle: String?,
    glyph: AppleGlyph?,
    accent: Color?,
    onClose: (() -> Unit)?
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (glyph != null) {
            AppGlyphBadge(
                glyph = glyph,
                tint = accent ?: AppTheme.colors.accent,
                size = 42.dp
            )
            Spacer(modifier = Modifier.width(AppTheme.spacing.md))
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.xxs)
        ) {
            AppText(
                text = title,
                style = AppTheme.typography.h2,
                color = AppTheme.colors.textPrimary
            )
            if (subtitle != null) {
                AppText(
                    text = subtitle,
                    style = AppTheme.typography.bodySmall,
                    color = AppTheme.colors.textSecondary
                )
            }
        }

        if (onClose != null) {
            Spacer(modifier = Modifier.width(AppTheme.spacing.sm))
            AppIconButton(
                glyph = AppleGlyph.Close,
                onClick = onClose,
                contentDescription = "Close",
                tint = AppTheme.colors.textSecondary
            )
        }
    }
}
