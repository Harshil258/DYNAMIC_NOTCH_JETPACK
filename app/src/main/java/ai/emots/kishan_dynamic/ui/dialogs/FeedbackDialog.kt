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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.BugReport
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarBorder
import androidx.compose.material.icons.rounded.ThumbUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import ai.emots.kishan_dynamic.ui.components.AuroraBottomSheet
import ai.emots.kishan_dynamic.ui.components.AuroraButton
import ai.emots.kishan_dynamic.ui.components.AuroraIconButton
import ai.emots.kishan_dynamic.ui.components.GlassCard
import ai.emots.kishan_dynamic.ui.theme.AuroraIslandTheme
import ai.emots.kishan_dynamic.ui.theme.AuroraTheme

enum class FeedbackType(val label: String, val icon: ImageVector) {
    BUG("Bug Report", Icons.Rounded.BugReport),
    FEATURE("Feature Request", Icons.Rounded.Lightbulb),
    GENERAL("General Praise", Icons.Rounded.ThumbUp)
}

/**
 * Modern Frosted Glass Feedback Dialog with interactive star rating, categories, and email dispatch.
 */
@Composable
fun FeedbackDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var selectedRating by remember { mutableIntStateOf(5) }
    var selectedCategory by remember { mutableStateOf(FeedbackType.FEATURE) }
    var feedbackComment by remember { mutableStateOf("") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AuroraTheme.spacing.md),
            contentAlignment = Alignment.BottomCenter
        ) {
            AuroraBottomSheet {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(AuroraTheme.spacing.md)
                ) {
                    // Header Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Send Feedback",
                            style = AuroraTheme.typography.titleMedium,
                            color = AuroraTheme.colors.textPrimary
                        )
                        AuroraIconButton(
                            icon = Icons.Rounded.Close,
                            onClick = onDismiss,
                            size = 32.dp
                        )
                    }

                    // 5-Star Rating Row
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "How would you rate Aurora Island?",
                            style = AuroraTheme.typography.bodySmall,
                            color = AuroraTheme.colors.textSecondary
                        )

                        Spacer(modifier = Modifier.height(AuroraTheme.spacing.sm))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(AuroraTheme.spacing.sm),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            for (star in 1..5) {
                                val isSelected = star <= selectedRating
                                val starScale by animateFloatAsState(
                                    targetValue = if (isSelected) 1.15f else 1.0f,
                                    animationSpec = spring(dampingRatio = 0.5f),
                                    label = "star_scale"
                                )
                                Icon(
                                    imageVector = if (isSelected) Icons.Rounded.Star else Icons.Rounded.StarBorder,
                                    contentDescription = "Rate $star",
                                    tint = if (isSelected) AuroraTheme.colors.warning else AuroraTheme.colors.textTertiary,
                                    modifier = Modifier
                                        .size(36.dp)
                                        .scale(starScale)
                                        .clickable { selectedRating = star }
                                )
                            }
                        }
                    }

                    // 3 Category Chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(AuroraTheme.spacing.xs)
                    ) {
                        FeedbackType.values().forEach { category ->
                            val isSelected = selectedCategory == category
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(42.dp)
                                    .clip(AuroraTheme.shapes.cardSmall)
                                    .background(
                                        if (isSelected) AuroraTheme.colors.primary.copy(alpha = 0.22f)
                                        else AuroraTheme.colors.glassSurfaceSubtle
                                    )
                                    .border(
                                        width = if (isSelected) AuroraTheme.elevation.activeBorder else AuroraTheme.elevation.hairlineBorder,
                                        brush = if (isSelected) AuroraTheme.colors.brandGradientBrush else AuroraTheme.colors.glassBorderBrush,
                                        shape = AuroraTheme.shapes.cardSmall
                                    )
                                    .clickable { selectedCategory = category },
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = category.icon,
                                        contentDescription = null,
                                        tint = if (isSelected) AuroraTheme.colors.primary else AuroraTheme.colors.textSecondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = category.label.split(" ").first(),
                                        style = AuroraTheme.typography.labelSmall,
                                        color = if (isSelected) AuroraTheme.colors.textPrimary else AuroraTheme.colors.textSecondary
                                    )
                                }
                            }
                        }
                    }

                    // Comment Input Box
                    GlassCard(modifier = Modifier.fillMaxWidth(), contentPadding = AuroraTheme.spacing.sm) {
                        Column {
                            BasicTextField(
                                value = feedbackComment,
                                onValueChange = { if (it.length <= 500) feedbackComment = it },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(80.dp),
                                textStyle = AuroraTheme.typography.bodySmall.copy(color = AuroraTheme.colors.textPrimary),
                                cursorBrush = SolidColor(AuroraTheme.colors.primary),
                                decorationBox = { innerTextField ->
                                    if (feedbackComment.isEmpty()) {
                                        Text(
                                            text = "Tell us your thoughts, ideas, or report an issue...",
                                            style = AuroraTheme.typography.bodySmall,
                                            color = AuroraTheme.colors.textTertiary
                                        )
                                    }
                                    innerTextField()
                                }
                            )
                            Text(
                                text = "${feedbackComment.length}/500",
                                style = AuroraTheme.typography.labelSmall,
                                color = AuroraTheme.colors.textTertiary,
                                modifier = Modifier.align(Alignment.End)
                            )
                        }
                    }

                    // Submit Button
                    AuroraButton(
                        onClick = {
                            sendFeedbackEmail(context, selectedRating, selectedCategory, feedbackComment)
                            onDismiss()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = Icons.AutoMirrored.Rounded.Send
                    ) {
                        Text(
                            text = "Submit Feedback",
                            style = AuroraTheme.typography.labelLarge,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

private fun sendFeedbackEmail(
    context: Context,
    rating: Int,
    category: FeedbackType,
    comment: String
) {
    val subject = "[${category.label}] Aurora Island User Feedback"
    val body = buildString {
        appendLine("Rating: ${"⭐".repeat(rating)} ($rating/5)")
        appendLine("Category: ${category.label}")
        appendLine()
        appendLine("Comment:")
        appendLine(comment.ifBlank { "No additional comment provided." })
        appendLine()
        appendLine("--- Device Diagnostics ---")
        appendLine("Device: ${Build.MANUFACTURER} ${Build.MODEL}")
        appendLine("Android: ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})")
        appendLine("App: Aurora Island v1.0.0")
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
            context.startActivity(Intent.createChooser(fallback, "Send Feedback"))
        } catch (ignored: Exception) {}
    }
}

// =============================================================================
// PREVIEWS
// =============================================================================

@Preview(name = "Feedback Dialog - Dark", showBackground = true, backgroundColor = 0xFF0B0B12)
@Composable
private fun FeedbackDialogDarkPreview() {
    AuroraIslandTheme(darkTheme = true) {
        FeedbackDialog(onDismiss = {})
    }
}
