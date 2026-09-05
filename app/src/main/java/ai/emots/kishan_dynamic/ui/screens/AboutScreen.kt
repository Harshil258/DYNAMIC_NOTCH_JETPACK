package ai.emots.kishan_dynamic.ui.screens

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import ai.emots.kishan_dynamic.data.preferences.AuroraPreferences
import ai.emots.kishan_dynamic.ui.components.AppText
import ai.emots.kishan_dynamic.ui.components.AppleGlyph
import ai.emots.kishan_dynamic.ui.dialogs.FeedbackDialog
import ai.emots.kishan_dynamic.ui.kit.AppCard
import ai.emots.kishan_dynamic.ui.kit.AppFootnote
import ai.emots.kishan_dynamic.ui.kit.AppGlyphBadge
import ai.emots.kishan_dynamic.ui.kit.AppInfoRow
import ai.emots.kishan_dynamic.ui.kit.AppListCard
import ai.emots.kishan_dynamic.ui.kit.AppNavRow
import ai.emots.kishan_dynamic.ui.kit.AppRowDivider
import ai.emots.kishan_dynamic.ui.kit.AppScreen
import ai.emots.kishan_dynamic.ui.kit.AppSectionSpacer
import ai.emots.kishan_dynamic.ui.kit.AppSectionTitle
import ai.emots.kishan_dynamic.ui.kit.AppStatusPill
import ai.emots.kishan_dynamic.ui.kit.AppTopBar
import ai.emots.kishan_dynamic.ui.theme.AppTheme
import ai.emots.kishan_dynamic.ui.theme.accentGradient

/**
 * App-owned About surface inspired by the reference diagnostics screen.
 * It deliberately exposes facts from the running device rather than static
 * marketing copy, while keeping the visual language inside our shared kit.
 */
@Composable
fun AboutScreen(
    onBack: () -> Unit,
    onRate: () -> Unit = {},
    onShare: () -> Unit = {}
) {
    val context = LocalContext.current
    val preferences = remember { AuroraPreferences(context) }
    val isPro by preferences.isProActive.collectAsState(initial = false)
    var showFeedback by remember { mutableStateOf(false) }
    val packageInfo = remember(context) {
        runCatching { context.packageManager.getPackageInfo(context.packageName, 0) }.getOrNull()
    }
    val versionName = packageInfo?.versionName ?: "—"
    val versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        packageInfo?.longVersionCode?.toString() ?: "—"
    } else {
        @Suppress("DEPRECATION")
        packageInfo?.versionCode?.toString() ?: "—"
    }

    AppScreen {
        AppTopBar(
            title = "About Aurora",
            subtitle = "Your island, your device, your control",
            onBack = onBack
        )

        AppCard {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(AppTheme.radius.lg))
                    .background(AppTheme.colors.accentGradient())
                    .padding(AppTheme.spacing.lg),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AppGlyphBadge(
                    glyph = AppleGlyph.Notch,
                    tint = AppTheme.colors.onAccent,
                    size = 54.dp
                )
                Spacer(modifier = Modifier.width(AppTheme.spacing.md))
                Column(modifier = Modifier.weight(1f)) {
                    AppText(
                        text = "Aurora Dynamic Island",
                        style = AppTheme.typography.h3,
                        color = AppTheme.colors.onAccent
                    )
                    Spacer(modifier = Modifier.height(AppTheme.spacing.xxs))
                    AppText(
                        text = "Premium notification experiences, built locally.",
                        style = AppTheme.typography.bodySmall,
                        color = AppTheme.colors.onAccent.copy(alpha = 0.82f)
                    )
                }
                AppStatusPill(
                    text = if (isPro) "Pro" else "Free",
                    color = AppTheme.colors.onAccent,
                    showDot = false
                )
            }
        }

        AppSectionSpacer()
        AppSectionTitle("Diagnostics")
        AppListCard {
            AppInfoRow(
                title = "Device",
                value = "${Build.MANUFACTURER.replaceFirstChar { it.uppercase() }} ${Build.MODEL}"
            )
            AppRowDivider()
            AppInfoRow(
                title = "Android",
                value = "${Build.VERSION.RELEASE} · API ${Build.VERSION.SDK_INT}"
            )
            AppRowDivider()
            AppInfoRow(title = "App version", value = "$versionName ($versionCode)")
            AppRowDivider()
            AppInfoRow(
                title = "Theme",
                value = if (AppTheme.isDark) "Dark" else "Light"
            )
        }

        AppSectionSpacer()
        AppSectionTitle("Support")
        AppListCard {
            AppNavRow(
                title = "Rate the app",
                subtitle = "Help us improve Aurora",
                glyph = AppleGlyph.Star,
                accent = AppTheme.colors.gold,
                onClick = onRate
            )
            AppRowDivider()
            AppNavRow(
                title = "Send feedback",
                subtitle = "Share an idea or report a problem",
                glyph = AppleGlyph.Sparkles,
                accent = AppTheme.colors.info,
                onClick = { showFeedback = true }
            )
            AppRowDivider()
            AppNavRow(
                title = "Share Aurora",
                subtitle = "Invite someone to try the island",
                glyph = AppleGlyph.Link,
                accent = AppTheme.colors.accent,
                onClick = onShare
            )
        }

        AppFootnote("Notifications, caller details, and device signals stay on this device unless you explicitly use a platform share or feedback action.")
    }

    if (showFeedback) {
        FeedbackDialog(onDismiss = { showFeedback = false })
    }
}

fun Context.shareAuroraApp() {
    runCatching {
        startActivity(
            Intent.createChooser(
                Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_SUBJECT, "Aurora Dynamic Island")
                    putExtra(
                        Intent.EXTRA_TEXT,
                        "Try Aurora Dynamic Island: https://play.google.com/store/apps/details?id=$packageName"
                    )
                },
                "Share Aurora"
            )
        )
    }
}
