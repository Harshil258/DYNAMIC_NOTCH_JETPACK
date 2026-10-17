package ai.emots.kishan_dynamic.ui.dialogs

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ai.emots.kishan_dynamic.ui.components.AppText
import ai.emots.kishan_dynamic.ui.components.AppleGlyph
import ai.emots.kishan_dynamic.ui.kit.AppButton
import ai.emots.kishan_dynamic.ui.kit.AppButtonStyle
import ai.emots.kishan_dynamic.ui.kit.AppDialog
import ai.emots.kishan_dynamic.ui.theme.AppTheme

import ai.emots.kishan_dynamic.data.premium.NativeAdConfiguration
import ai.emots.kishan_dynamic.ui.components.NativeAdCard
import ai.emots.kishan_dynamic.ui.components.NativeAdSize

/** A calm confirmation for leaving the root app surface, supporting optional exit native ad. */
@Composable
fun ExitConfirmationDialog(
    onDismiss: () -> Unit,
    onExit: () -> Unit,
    nativeAdConfiguration: NativeAdConfiguration? = null,
    isProActive: Boolean = false,
    canRequestAds: Boolean = false,
    isExitNativeEnabled: Boolean = false
) {
    AppDialog(
        onDismiss = onDismiss,
        title = "Leave Dynamic Island?",
        subtitle = "The overlay service can continue running in the background.",
        glyph = AppleGlyph.Close,
        accent = AppTheme.colors.warning
    ) {
        AppText(
            text = "Your island settings and active integrations will stay saved.",
            style = AppTheme.typography.body,
            color = AppTheme.colors.textSecondary
        )
        if (isExitNativeEnabled && !isProActive && nativeAdConfiguration != null) {
            Spacer(modifier = Modifier.height(AppTheme.spacing.md))
            NativeAdCard(
                configuration = nativeAdConfiguration,
                isProActive = isProActive,
                canRequestAds = canRequestAds,
                placement = "exit_dialog",
                size = NativeAdSize.Medium
            )
        }
        Spacer(modifier = Modifier.height(AppTheme.spacing.xl))
        AppButton(
            text = "Stay in app",
            glyph = AppleGlyph.Undo,
            style = AppButtonStyle.Secondary,
            onClick = onDismiss
        )
        Spacer(modifier = Modifier.height(AppTheme.spacing.sm))
        AppButton(
            text = "Exit",
            glyph = AppleGlyph.Close,
            style = AppButtonStyle.Destructive,
            onClick = onExit
        )
    }
}
