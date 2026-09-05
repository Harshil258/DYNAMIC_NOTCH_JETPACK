package ai.emots.kishan_dynamic.ui.screens

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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ai.emots.kishan_dynamic.ui.components.AppText
import ai.emots.kishan_dynamic.ui.components.AppleGlyph
import ai.emots.kishan_dynamic.ui.components.AppleIcon
import ai.emots.kishan_dynamic.data.preferences.AuroraPreferences
import ai.emots.kishan_dynamic.data.premium.PremiumFeaturePolicy
import ai.emots.kishan_dynamic.ui.kit.AppCard
import ai.emots.kishan_dynamic.ui.kit.AppFootnote
import ai.emots.kishan_dynamic.ui.kit.AppListCard
import ai.emots.kishan_dynamic.ui.kit.AppRowDivider
import ai.emots.kishan_dynamic.ui.kit.AppScreen
import ai.emots.kishan_dynamic.ui.kit.AppSectionSpacer
import ai.emots.kishan_dynamic.ui.kit.AppSectionTitle
import ai.emots.kishan_dynamic.ui.kit.AppStatusPill
import ai.emots.kishan_dynamic.ui.kit.AppTopBar
import ai.emots.kishan_dynamic.ui.motion.appReveal
import ai.emots.kishan_dynamic.ui.theme.AppTheme
import ai.emots.kishan_dynamic.ui.theme.AppThemeMode

@Composable
fun ThemeSettingsScreen(
    themeMode: AppThemeMode,
    onThemeModeChange: (AppThemeMode) -> Unit,
    onBack: () -> Unit,
    onNavigateToPremium: () -> Unit
) {
    val context = LocalContext.current
    val preferences = remember(context) { AuroraPreferences(context) }
    val isProActive by preferences.isProActive.collectAsState(initial = false)
    val themeLocked = PremiumFeaturePolicy.isLocked(
        PremiumFeaturePolicy.THEME_SETTINGS,
        isProActive
    )
    AppScreen {
        AppTopBar(
            modifier = Modifier.appReveal(0),
            title = "Theme",
            subtitle = "Choose the atmosphere for every screen",
            onBack = onBack
        )

        AppSectionSpacer()
        AppSectionTitle("Preview")
        AppCard(modifier = Modifier.appReveal(1)) {
            ThemePreview(themeMode = themeMode)
        }

        AppSectionSpacer()
        AppSectionTitle("Appearance")
        AppListCard(modifier = Modifier.appReveal(2)) {
            AppThemeMode.entries.forEachIndexed { index, option ->
                ThemeOptionRow(
                    mode = option,
                    selected = option == themeMode,
                    locked = themeLocked,
                    onClick = {
                        if (themeLocked) onNavigateToPremium() else onThemeModeChange(option)
                    }
                )
                if (index < AppThemeMode.entries.lastIndex) AppRowDivider()
            }
        }
        AppFootnote("System follows your device appearance. Light and Dark stay fixed until you change them.")
    }
}

@Composable
private fun ThemePreview(themeMode: AppThemeMode) {
    val isDark = when (themeMode) {
        AppThemeMode.Dark -> true
        AppThemeMode.Light -> false
        AppThemeMode.System -> AppTheme.isDark
    }
    val background = if (isDark) Color(0xFF070A12) else Color(0xFFF6F8FC)
    val surface = if (isDark) Color(0xFF151A29) else Color.White
    val primary = if (isDark) Color(0xFF9ABEFF) else Color(0xFF2D6CDF)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(AppTheme.radius.lg))
            .background(
                Brush.verticalGradient(
                    listOf(
                        background,
                        if (isDark) Color(0xFF111C3C) else Color(0xFFDCEBFF)
                    )
                )
            )
            .padding(AppTheme.spacing.lg),
        verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.md)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                AppleIcon(glyph = AppleGlyph.Notch, tint = Color.White, size = 20.dp)
            }
            AppText(
                text = themeMode.label,
                style = AppTheme.typography.caption,
                color = if (isDark) Color.White.copy(alpha = 0.72f) else Color(0xFF1B2435)
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clip(RoundedCornerShape(AppTheme.radius.md))
                .background(surface)
                .border(0.5.dp, primary.copy(alpha = 0.45f), RoundedCornerShape(AppTheme.radius.md))
                .padding(horizontal = AppTheme.spacing.md),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(primary)
                )
                Spacer(modifier = Modifier.size(AppTheme.spacing.sm))
                AppText(
                    text = "Every screen follows the same tokens",
                    style = AppTheme.typography.bodySmall,
                    color = if (isDark) Color.White else Color(0xFF192236),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun ThemeOptionRow(
    mode: AppThemeMode,
    selected: Boolean,
    locked: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(AppTheme.radius.md))
            .clickable(onClick = onClick)
            .padding(horizontal = AppTheme.layout.cardPadding, vertical = AppTheme.spacing.md),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val glyph = when (mode) {
            AppThemeMode.System -> AppleGlyph.Settings
            AppThemeMode.Light -> AppleGlyph.Sparkles
            AppThemeMode.Dark -> AppleGlyph.Moon
        }
        AppleIcon(
            glyph = glyph,
            tint = if (selected) AppTheme.colors.accent else AppTheme.colors.textSecondary,
            size = 20.dp
        )
        Spacer(modifier = Modifier.size(AppTheme.spacing.md))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AppText(
                    text = mode.label,
                    style = AppTheme.typography.body,
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                    color = AppTheme.colors.textPrimary
                )
                if (locked) {
                    AppStatusPill(
                        text = "PRO",
                        color = AppTheme.colors.gold,
                        showDot = false,
                        modifier = Modifier.padding(start = AppTheme.spacing.sm)
                    )
                }
            }
            AppText(
                text = when (mode) {
                    AppThemeMode.System -> "Follow device settings"
                    AppThemeMode.Light -> "Bright surfaces and soft contrast"
                    AppThemeMode.Dark -> "Deep surfaces with luminous accents"
                },
                style = AppTheme.typography.caption,
                color = AppTheme.colors.textSecondary
            )
        }
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .background(if (selected) AppTheme.colors.accent else Color.Transparent)
                .border(
                    1.dp,
                    if (selected) AppTheme.colors.accent else AppTheme.colors.border,
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (selected) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(AppTheme.colors.onAccent)
                )
            }
        }
    }
}
