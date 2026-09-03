package ai.emots.kishan_dynamic.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ai.emots.kishan_dynamic.ui.components.AppText
import ai.emots.kishan_dynamic.ui.components.AppleGlyph
import ai.emots.kishan_dynamic.ui.components.AppleIcon
import ai.emots.kishan_dynamic.ui.dialogs.PermissionExplanationSheet
import ai.emots.kishan_dynamic.ui.kit.AppButton
import ai.emots.kishan_dynamic.ui.kit.AppButtonStyle
import ai.emots.kishan_dynamic.ui.kit.AppCard
import ai.emots.kishan_dynamic.ui.kit.AppFootnote
import ai.emots.kishan_dynamic.ui.kit.AppGlyphBadge
import ai.emots.kishan_dynamic.ui.kit.AppProgressBar
import ai.emots.kishan_dynamic.ui.kit.AppScreen
import ai.emots.kishan_dynamic.ui.kit.AppSectionSpacer
import ai.emots.kishan_dynamic.ui.kit.AppSectionTitle
import ai.emots.kishan_dynamic.ui.kit.AppStatusPill
import ai.emots.kishan_dynamic.ui.kit.AppTopBar
import ai.emots.kishan_dynamic.ui.theme.AppTheme

data class PermissionItemData(
    val id: String,
    val title: String,
    val description: String,
    val glyph: AppleGlyph,
    val isGranted: Boolean,
    val isRequired: Boolean = true,
    val privacyDetail: String
)

@Composable
fun PermissionScreen(
    onContinue: () -> Unit = {},
    onBack: () -> Unit = {}
) {
    var accessibilityGranted by remember { mutableStateOf(false) }
    var notificationGranted by remember { mutableStateOf(false) }
    var callGranted by remember { mutableStateOf(false) }
    var batteryGranted by remember { mutableStateOf(false) }
    var explanationPermission by remember { mutableStateOf<PermissionItemData?>(null) }

    val permissions = remember(accessibilityGranted, notificationGranted, callGranted, batteryGranted) {
        listOf(
            PermissionItemData(
                id = "accessibility",
                title = "Screen overlay",
                description = "Draws the island above the status bar and camera cutout.",
                glyph = AppleGlyph.Sparkles,
                isGranted = accessibilityGranted,
                isRequired = true,
                privacyDetail = "Only used to draw the island overlay. Keystrokes and screen contents are never read or logged."
            ),
            PermissionItemData(
                id = "notification",
                title = "Notification access",
                description = "Shows messages, media and alerts inside the island.",
                glyph = AppleGlyph.Bell,
                isGranted = notificationGranted,
                isRequired = true,
                privacyDetail = "Notifications are read locally to render the banner. Nothing is transmitted off your phone."
            ),
            PermissionItemData(
                id = "telecom",
                title = "Phone state",
                description = "Displays caller name, live duration and call actions.",
                glyph = AppleGlyph.Phone,
                isGranted = callGranted,
                isRequired = false,
                privacyDetail = "Detects active calls to present the call HUD. No audio is ever recorded."
            ),
            PermissionItemData(
                id = "battery",
                title = "Background activity",
                description = "Stops battery savers from closing the island service.",
                glyph = AppleGlyph.Battery,
                isGranted = batteryGranted,
                isRequired = false,
                privacyDetail = "Keeps the overlay alive so the island doesn't disappear inside heavy apps."
            )
        )
    }

    fun grant(id: String, value: Boolean) {
        when (id) {
            "accessibility" -> accessibilityGranted = value
            "notification" -> notificationGranted = value
            "telecom" -> callGranted = value
            "battery" -> batteryGranted = value
        }
    }

    val requiredPermissions = permissions.filter { it.isRequired }
    val optionalPermissions = permissions.filterNot { it.isRequired }
    val grantedCount = permissions.count { it.isGranted }
    val canProceed = requiredPermissions.all { it.isGranted }

    AppScreen {
        AppTopBar(
            title = "Permissions",
            subtitle = "What the island needs to work",
            onBack = onBack
        )

        AppCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    AppText(
                        text = "$grantedCount of ${permissions.size} granted",
                        style = AppTheme.typography.h3,
                        color = AppTheme.colors.textPrimary
                    )
                    Spacer(modifier = Modifier.height(AppTheme.spacing.xs))
                    AppText(
                        text = if (canProceed) {
                            "Everything required is in place."
                        } else {
                            "Grant the required items below to activate the island."
                        },
                        style = AppTheme.typography.bodySmall,
                        color = AppTheme.colors.textSecondary
                    )
                }
                Spacer(modifier = Modifier.width(AppTheme.spacing.md))
                AppStatusPill(
                    text = if (canProceed) "Ready" else "Incomplete",
                    color = if (canProceed) AppTheme.colors.success else AppTheme.colors.warning
                )
            }

            Spacer(modifier = Modifier.height(AppTheme.spacing.lg))
            AppProgressBar(
                progress = grantedCount.toFloat() / permissions.size.toFloat(),
                color = if (canProceed) AppTheme.colors.success else AppTheme.colors.accent
            )
        }

        AppSectionSpacer()

        AppSectionTitle("Required")
        Column(verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm)) {
            requiredPermissions.forEach { item ->
                PermissionCard(
                    item = item,
                    onToggle = { grant(item.id, !item.isGranted) },
                    onInfo = { explanationPermission = item }
                )
            }
        }

        AppSectionSpacer()

        AppSectionTitle("Recommended")
        Column(verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm)) {
            optionalPermissions.forEach { item ->
                PermissionCard(
                    item = item,
                    onToggle = { grant(item.id, !item.isGranted) },
                    onInfo = { explanationPermission = item }
                )
            }
        }

        AppSectionSpacer()

        AppButton(
            text = if (canProceed) "Continue" else "Grant required permissions",
            glyph = if (canProceed) AppleGlyph.Check else AppleGlyph.Shield,
            style = if (canProceed) AppButtonStyle.Primary else AppButtonStyle.Secondary,
            enabled = canProceed,
            onClick = onContinue
        )
        AppFootnote("Everything is processed on your device. No data ever leaves your phone.")
    }

    explanationPermission?.let { permission ->
        PermissionExplanationSheet(
            permission = permission,
            onDismiss = { explanationPermission = null },
            onGrantClick = {
                grant(permission.id, true)
                explanationPermission = null
            }
        )
    }
}

@Composable
private fun PermissionCard(
    item: PermissionItemData,
    onToggle: () -> Unit,
    onInfo: () -> Unit
) {
    val accent = if (item.isGranted) AppTheme.colors.success else AppTheme.colors.accent

    AppCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AppGlyphBadge(glyph = item.glyph, tint = accent, size = 42.dp)
            Spacer(modifier = Modifier.width(AppTheme.spacing.md))

            Column(modifier = Modifier.weight(1f)) {
                AppText(
                    text = item.title,
                    style = AppTheme.typography.body,
                    fontWeight = FontWeight.SemiBold,
                    color = AppTheme.colors.textPrimary
                )
                Spacer(modifier = Modifier.height(AppTheme.spacing.xxs))
                AppText(
                    text = item.description,
                    style = AppTheme.typography.bodySmall,
                    color = AppTheme.colors.textSecondary
                )
            }

            Spacer(modifier = Modifier.width(AppTheme.spacing.sm))

            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(AppTheme.colors.surfaceElevated)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onInfo
                    ),
                contentAlignment = Alignment.Center
            ) {
                AppText(
                    text = "i",
                    style = AppTheme.typography.body,
                    fontWeight = FontWeight.Bold,
                    color = AppTheme.colors.textSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(AppTheme.spacing.md))

        if (item.isGranted) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(CircleShape)
                    .background(AppTheme.colors.success.copy(alpha = 0.14f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onToggle
                    )
                    .padding(vertical = AppTheme.spacing.md),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                AppleIcon(glyph = AppleGlyph.Check, tint = AppTheme.colors.success, size = 14.dp)
                Spacer(modifier = Modifier.width(AppTheme.spacing.sm))
                AppText(
                    text = "Granted",
                    style = AppTheme.typography.button,
                    color = AppTheme.colors.success
                )
            }
        } else {
            AppButton(
                text = "Grant access",
                style = AppButtonStyle.Tonal,
                onClick = onToggle
            )
        }
    }
}
