package ai.emots.kishan_dynamic.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.widget.Toast
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ai.emots.kishan_dynamic.ui.components.AppText
import ai.emots.kishan_dynamic.ui.components.AppleGlyph
import ai.emots.kishan_dynamic.ui.components.AppleIcon
import ai.emots.kishan_dynamic.ui.kit.AppButton
import ai.emots.kishan_dynamic.ui.kit.AppButtonStyle
import ai.emots.kishan_dynamic.ui.kit.AppCard
import ai.emots.kishan_dynamic.ui.kit.AppFootnote
import ai.emots.kishan_dynamic.ui.kit.AppScreen
import ai.emots.kishan_dynamic.ui.kit.AppSectionSpacer
import ai.emots.kishan_dynamic.ui.kit.AppSectionTitle
import ai.emots.kishan_dynamic.ui.kit.AppTopBar
import ai.emots.kishan_dynamic.ui.theme.AppTheme

enum class FeedbackCategory(val label: String, val glyph: AppleGlyph) {
    BUG("Bug report", AppleGlyph.Info),
    FEATURE("Feature request", AppleGlyph.Sparkles),
    GENERAL("General feedback", AppleGlyph.Check),
    SUPPORT("Help & Support", AppleGlyph.Info)
}

private const val SUPPORT_EMAIL = "suryadevelopers07@gmail.com"

@Composable
fun FeedbackScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var selectedRating by remember { mutableIntStateOf(5) }
    var selectedCategory by remember { mutableStateOf(FeedbackCategory.FEATURE) }
    var feedbackText by remember { mutableStateOf("") }

    val isDark = AppTheme.isDark
    val textPrimary = if (isDark) Color.White else AppTheme.colors.textPrimary
    val textSecondary = if (isDark) Color(0xFF94A3B8) else AppTheme.colors.textSecondary

    AppScreen(
        topBar = {
            AppTopBar(
                title = "Send feedback",
                subtitle = "Help Surya Developer improve Aurora Dynamic Island",
                onBack = onBack
            )
        }
    ) {
        // Star Rating Card
        AppSectionTitle("Rating")
        AppCard(padding = AppTheme.spacing.lg) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AppText(
                    text = "How would you rate your experience?",
                    style = AppTheme.typography.body,
                    color = textPrimary
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (star in 1..5) {
                        val isSelected = star <= selectedRating
                        val scale by animateFloatAsState(
                            targetValue = if (isSelected) 1.15f else 1f,
                            animationSpec = spring(dampingRatio = 0.5f),
                            label = "star_$star"
                        )
                        Box(
                            modifier = Modifier
                                .scale(scale)
                                .clip(CircleShape)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) { selectedRating = star }
                                .padding(4.dp)
                        ) {
                            AppleIcon(
                                glyph = AppleGlyph.Star,
                                tint = if (isSelected) AppTheme.colors.gold else textSecondary.copy(alpha = 0.3f),
                                size = 32.dp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                val ratingDescription = when (selectedRating) {
                    1 -> "Needs improvement"
                    2 -> "Below average"
                    3 -> "Good"
                    4 -> "Very good"
                    5 -> "Excellent! 🎉"
                    else -> ""
                }
                AppText(
                    text = ratingDescription,
                    style = AppTheme.typography.caption.copy(fontWeight = FontWeight.SemiBold),
                    color = AppTheme.colors.gold
                )
            }
        }

        AppSectionSpacer()

        // Category Selection
        AppSectionTitle("Feedback category")
        AppCard(padding = AppTheme.spacing.md) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                FeedbackCategory.entries.forEach { category ->
                    val isSelected = selectedCategory == category
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(AppTheme.radius.md))
                            .background(
                                if (isSelected) {
                                    if (isDark) AppTheme.colors.primary.copy(alpha = 0.15f)
                                    else AppTheme.colors.primary.copy(alpha = 0.08f)
                                } else Color.Transparent
                            )
                            .border(
                                width = if (isSelected) 1.5.dp else 0.5.dp,
                                color = if (isSelected) AppTheme.colors.primary else AppTheme.colors.border,
                                shape = RoundedCornerShape(AppTheme.radius.md)
                            )
                            .clickable { selectedCategory = category }
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            AppleIcon(
                                glyph = category.glyph,
                                tint = if (isSelected) AppTheme.colors.primary else textSecondary,
                                size = 18.dp
                            )
                            AppText(
                                text = category.label,
                                style = AppTheme.typography.bodySmall.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                ),
                                color = if (isSelected) textPrimary else textSecondary
                            )
                        }

                        if (isSelected) {
                            AppleIcon(
                                glyph = AppleGlyph.Check,
                                tint = AppTheme.colors.primary,
                                size = 16.dp
                            )
                        }
                    }
                }
            }
        }

        AppSectionSpacer()

        // Detailed Feedback Message Input
        AppSectionTitle("Your message")
        AppCard(padding = AppTheme.spacing.md) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 120.dp)
                        .clip(RoundedCornerShape(AppTheme.radius.md))
                        .background(if (isDark) Color.White.copy(alpha = 0.04f) else Color.Black.copy(alpha = 0.03f))
                        .border(0.5.dp, AppTheme.colors.border, RoundedCornerShape(AppTheme.radius.md))
                        .padding(14.dp)
                ) {
                    if (feedbackText.isEmpty()) {
                        AppText(
                            text = "Please describe what happened or what you'd like to see added...",
                            style = AppTheme.typography.bodySmall,
                            color = textSecondary.copy(alpha = 0.6f)
                        )
                    }
                    BasicTextField(
                        value = feedbackText,
                        onValueChange = { feedbackText = it },
                        textStyle = TextStyle(
                            color = textPrimary,
                            fontSize = 14.sp
                        ),
                        cursorBrush = SolidColor(AppTheme.colors.primary),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    AppText(
                        text = "Device info will be attached automatically",
                        style = AppTheme.typography.caption,
                        color = textSecondary.copy(alpha = 0.7f)
                    )
                    AppText(
                        text = "${feedbackText.length}/1000",
                        style = AppTheme.typography.caption,
                        color = textSecondary
                    )
                }
            }
        }

        AppSectionSpacer()

        // Submit Action Button
        AppButton(
            text = "Submit feedback",
            glyph = AppleGlyph.Forward,
            style = AppButtonStyle.Primary,
            enabled = feedbackText.isNotBlank(),
            onClick = {
                val deviceInfo = """
                    
                    ---
                    App: Aurora Dynamic Island
                    Developer: Surya Developer
                    Version: 1.0.0
                    Device: ${Build.MANUFACTURER} ${Build.MODEL}
                    Android: ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})
                    Rating: $selectedRating/5
                    Category: ${selectedCategory.label}
                """.trimIndent()

                val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                    data = Uri.parse("mailto:")
                    putExtra(Intent.EXTRA_EMAIL, arrayOf(SUPPORT_EMAIL))
                    putExtra(Intent.EXTRA_SUBJECT, "[Feedback - ${selectedCategory.label}] Aurora Dynamic Island")
                    putExtra(Intent.EXTRA_TEXT, "$feedbackText\n$deviceInfo")
                }

                val chooser = Intent.createChooser(emailIntent, "Send Feedback")
                chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

                try {
                    context.startActivity(chooser)
                    Toast.makeText(context, "Opening email to submit feedback...", Toast.LENGTH_SHORT).show()
                } catch (_: Exception) {
                    Toast.makeText(context, "No email client found. Please write to $SUPPORT_EMAIL", Toast.LENGTH_LONG).show()
                }
            }
        )

        AppFootnote("We review all submissions directly at Surya Developer to continuously improve your experience.")
    }
}
