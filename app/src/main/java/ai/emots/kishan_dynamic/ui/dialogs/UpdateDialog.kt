package ai.emots.kishan_dynamic.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import ai.emots.kishan_dynamic.ui.components.AppText
import ai.emots.kishan_dynamic.ui.components.AppleGlyph
import ai.emots.kishan_dynamic.ui.kit.AppButton
import ai.emots.kishan_dynamic.ui.kit.AppButtonStyle
import ai.emots.kishan_dynamic.ui.kit.AppDialog
import ai.emots.kishan_dynamic.ui.theme.AppTheme
import ai.emots.kishan_dynamic.ui.theme.surfaceGradient

/** Blocking update prompt: cannot be dismissed. */
@Composable
fun ForceUpdateDialog(
    onUpdateClick: () -> Unit,
    versionCode: Int? = null,
    notes: List<String> = emptyList()
) {
    AppDialog(
        onDismiss = {},
        title = "Update required",
        subtitle = versionCode?.let { "Version $it is needed to continue." }
            ?: "A newer version is needed to continue.",
        glyph = AppleGlyph.Sparkles,
        accent = AppTheme.colors.error,
        dismissOnOutsideTap = false,
        showClose = false
    ) {
        AppText(
            text = notes.firstOrNull()
                ?: "This release includes important compatibility and reliability improvements. Please update to keep the island working.",
            style = AppTheme.typography.bodySmall,
            color = AppTheme.colors.textSecondary
        )

        Spacer(modifier = Modifier.height(AppTheme.spacing.xl))

        AppButton(
            text = "Update on Google Play",
            glyph = AppleGlyph.Sparkles,
            onClick = onUpdateClick
        )
    }
}

/** Optional update prompt with a changelog. */
@Composable
fun SoftUpdateDialog(
    onUpdateClick: () -> Unit,
    onDismiss: () -> Unit,
    versionCode: Int? = null,
    changes: List<String> = emptyList()
) {
    val visibleChanges = changes.ifEmpty {
        listOf(
        "Redesigned interface across every screen",
        "Smoother island expand and collapse",
        "Fixes for notification previews"
        )
    }

    AppDialog(
        onDismiss = onDismiss,
        title = "Update available",
        subtitle = versionCode?.let { "Version $it" } ?: "A new version is available",
        glyph = AppleGlyph.Sparkles,
        accent = AppTheme.colors.accent
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(AppTheme.radius.lg))
                .background(AppTheme.colors.surfaceGradient())
                .padding(AppTheme.spacing.lg),
            verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.md)
        ) {
            visibleChanges.forEach { change ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(AppTheme.colors.accent)
                    )
                    Spacer(modifier = Modifier.width(AppTheme.spacing.md))
                    AppText(
                        text = change,
                        style = AppTheme.typography.bodySmall,
                        color = AppTheme.colors.textPrimary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(AppTheme.spacing.xl))

        AppButton(
            text = "Update now",
            glyph = AppleGlyph.Sparkles,
            onClick = onUpdateClick
        )
        Spacer(modifier = Modifier.height(AppTheme.spacing.sm))
        AppButton(
            text = "Later",
            style = AppButtonStyle.Secondary,
            onClick = onDismiss
        )
    }
}
