package ai.emots.kishan_dynamic.ui.dialogs

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ai.emots.kishan_dynamic.ui.components.AppText
import ai.emots.kishan_dynamic.ui.components.AppleGlyph
import ai.emots.kishan_dynamic.ui.components.AppleIcon
import ai.emots.kishan_dynamic.ui.kit.AppButton
import ai.emots.kishan_dynamic.ui.kit.AppButtonStyle
import ai.emots.kishan_dynamic.ui.kit.AppSheet
import ai.emots.kishan_dynamic.ui.theme.AppTheme

enum class FeedbackType(val label: String) {
    BUG("Bug"),
    FEATURE("Idea"),
    GENERAL("Praise")
}

@Composable
fun FeedbackDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var selectedRating by remember { mutableIntStateOf(5) }
    var selectedCategory by remember { mutableStateOf(FeedbackType.FEATURE) }
    var feedbackComment by remember { mutableStateOf("") }

    AppSheet(
        onDismiss = onDismiss,
        title = "Send feedback",
        subtitle = "Tell us what to improve next."
    ) {
        AppText(
            text = "How would you rate the app?",
            style = AppTheme.typography.bodySmall,
            color = AppTheme.colors.textSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(AppTheme.spacing.md))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm, Alignment.CenterHorizontally)
        ) {
            for (star in 1..5) {
                val isSelected = star <= selectedRating
                val scale by animateFloatAsState(
                    targetValue = if (isSelected) 1.1f else 1f,
                    animationSpec = spring(dampingRatio = 0.5f),
                    label = "star_scale"
                )
                AppleIcon(
                    glyph = AppleGlyph.Star,
                    tint = if (isSelected) AppTheme.colors.gold else AppTheme.colors.disabled,
                    size = 32.dp,
                    modifier = Modifier
                        .scale(scale)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { selectedRating = star }
                )
            }
        }

        Spacer(modifier = Modifier.height(AppTheme.spacing.xl))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm)
        ) {
            FeedbackType.entries.forEach { category ->
                val isSelected = selectedCategory == category
                val shape = RoundedCornerShape(AppTheme.radius.md)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .clip(shape)
                        .background(
                            if (isSelected) AppTheme.colors.accent.copy(alpha = 0.14f)
                            else AppTheme.colors.surfaceVariant
                        )
                        .border(
                            0.5.dp,
                            if (isSelected) AppTheme.colors.accent.copy(alpha = 0.5f) else AppTheme.colors.border,
                            shape
                        )
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { selectedCategory = category },
                    contentAlignment = Alignment.Center
                ) {
                    AppText(
                        text = category.label,
                        style = AppTheme.typography.button,
                        color = if (isSelected) AppTheme.colors.accent else AppTheme.colors.textSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(AppTheme.spacing.lg))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(AppTheme.radius.lg))
                .background(AppTheme.colors.surfaceVariant)
                .border(0.5.dp, AppTheme.colors.border, RoundedCornerShape(AppTheme.radius.lg))
                .padding(AppTheme.spacing.lg)
        ) {
            if (feedbackComment.isEmpty()) {
                AppText(
                    text = "Add a note (optional)",
                    style = AppTheme.typography.bodySmall,
                    color = AppTheme.colors.textTertiary
                )
            }
            BasicTextField(
                value = feedbackComment,
                onValueChange = { feedbackComment = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 72.dp),
                textStyle = AppTheme.typography.bodySmall.copy(color = AppTheme.colors.textPrimary),
                cursorBrush = SolidColor(AppTheme.colors.accent)
            )
        }

        Spacer(modifier = Modifier.height(AppTheme.spacing.xl))

        AppButton(
            text = "Send feedback",
            glyph = AppleGlyph.Sparkles,
            onClick = {
                sendFeedbackEmail(context, selectedRating, selectedCategory, feedbackComment)
                onDismiss()
            }
        )
        Spacer(modifier = Modifier.height(AppTheme.spacing.sm))
        AppButton(
            text = "Cancel",
            style = AppButtonStyle.Secondary,
            onClick = onDismiss
        )
    }
}

private fun sendFeedbackEmail(
    context: Context,
    rating: Int,
    category: FeedbackType,
    comment: String
) {
    val subject = "[${category.label}] Dynamic Island feedback"
    val body = buildString {
        appendLine("Rating: $rating/5")
        appendLine("Category: ${category.label}")
        appendLine()
        appendLine("Comment:")
        appendLine(comment.ifBlank { "No additional comment provided." })
        appendLine()
        appendLine("--- Device ---")
        appendLine("Device: ${Build.MANUFACTURER} ${Build.MODEL}")
        appendLine("Android: ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})")
        appendLine("App: v1.0.0")
    }

    val intent = Intent(Intent.ACTION_SENDTO).apply {
        data = Uri.parse("mailto:")
        putExtra(Intent.EXTRA_EMAIL, arrayOf("developer@auroraisland.app"))
        putExtra(Intent.EXTRA_SUBJECT, subject)
        putExtra(Intent.EXTRA_TEXT, body)
    }

    try {
        context.startActivity(intent)
    } catch (e: Exception) {
        val fallback = Intent(Intent.ACTION_SEND).apply {
            type = "message/rfc822"
            putExtra(Intent.EXTRA_EMAIL, arrayOf("developer@auroraisland.app"))
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, body)
        }
        try {
            context.startActivity(Intent.createChooser(fallback, "Send feedback"))
        } catch (ignored: Exception) {
        }
    }
}
