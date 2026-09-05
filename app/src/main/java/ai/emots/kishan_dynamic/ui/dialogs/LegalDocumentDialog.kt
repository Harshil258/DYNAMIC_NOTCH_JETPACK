package ai.emots.kishan_dynamic.ui.dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ai.emots.kishan_dynamic.ui.components.AppText
import ai.emots.kishan_dynamic.ui.components.AppleGlyph
import ai.emots.kishan_dynamic.ui.kit.AppButton
import ai.emots.kishan_dynamic.ui.kit.AppButtonStyle
import ai.emots.kishan_dynamic.ui.kit.AppDialog
import ai.emots.kishan_dynamic.ui.theme.AppTheme

@Composable
fun LegalDocumentDialog(
    title: String,
    paragraphs: List<String>,
    onDismiss: () -> Unit
) {
    AppDialog(
        onDismiss = onDismiss,
        title = title,
        subtitle = "Aurora Dynamic Island · v1.0",
        glyph = AppleGlyph.Shield,
        accent = AppTheme.colors.info
    ) {
        Column {
            paragraphs.forEachIndexed { index, paragraph ->
                AppText(
                    text = paragraph,
                    style = AppTheme.typography.bodySmall,
                    color = AppTheme.colors.textSecondary
                )
                if (index < paragraphs.lastIndex) {
                    Spacer(modifier = Modifier.height(AppTheme.spacing.md))
                }
            }
        }
        Spacer(modifier = Modifier.height(AppTheme.spacing.xl))
        AppButton(
            text = "Done",
            glyph = AppleGlyph.Check,
            style = AppButtonStyle.Secondary,
            onClick = onDismiss
        )
    }
}
