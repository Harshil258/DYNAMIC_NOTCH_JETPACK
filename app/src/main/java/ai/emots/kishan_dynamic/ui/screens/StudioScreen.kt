package ai.emots.kishan_dynamic.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ai.emots.kishan_dynamic.ui.components.AppText
import ai.emots.kishan_dynamic.ui.components.AtmosphericBackground
import ai.emots.kishan_dynamic.ui.theme.AppTheme

data class EditorialExperienceItem(
    val id: String,
    val title: String,
    val description: String,
    val statusText: String,
    val onClick: () -> Unit
)

/**
 * Editorial Experiences screen redesigned from first principles.
 * Completely removes cards, colorful circle icons, and neon boxes.
 * Pure typography, intentional spacing, and calm system-level elegance.
 */
@Composable
fun StudioScreen(
    onNavigateDisplay: () -> Unit,
    onNavigateNotifications: () -> Unit,
    onNavigateMusic: () -> Unit,
    onNavigateCalls: () -> Unit,
    onNavigateBattery: () -> Unit,
    onNavigateQuickControl: () -> Unit
) {
    val experiences = remember {
        listOf(
            EditorialExperienceItem(
                id = "notifications",
                title = "Notifications & Alerts",
                description = "Expand incoming WhatsApp, Telegram & app heads-up banners into your island.",
                statusText = "Active",
                onClick = onNavigateNotifications
            ),
            EditorialExperienceItem(
                id = "music",
                title = "Music & Media Playback",
                description = "Interactive album art, live frequency visualizer & transport controls.",
                statusText = "Now Playing",
                onClick = onNavigateMusic
            ),
            EditorialExperienceItem(
                id = "calls",
                title = "Phone & Telephony",
                description = "Real-time caller HUD, ongoing call timer & call management.",
                statusText = "Enabled",
                onClick = onNavigateCalls
            ),
            EditorialExperienceItem(
                id = "battery",
                title = "Battery & Warp Charging",
                description = "High-speed charging animations and critical low-power alerts.",
                statusText = "65W Warp",
                onClick = onNavigateBattery
            ),
            EditorialExperienceItem(
                id = "quick_control",
                title = "Quick Shortcuts",
                description = "Instant system toggles, silent mode switch & utility controls.",
                statusText = "7 Active",
                onClick = onNavigateQuickControl
            ),
            EditorialExperienceItem(
                id = "position",
                title = "Notch Calibration",
                description = "Align the island cutout precisely with your device's camera hardware.",
                statusText = "Calibrated",
                onClick = onNavigateDisplay
            )
        )
    }

    AtmosphericBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = 120.dp)
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // =================================================================
            // EDITORIAL HEADER
            // =================================================================
            AppText(
                text = "Experiences",
                style = AppTheme.typography.h1,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(6.dp))

            AppText(
                text = "Custom behaviors and adaptive interactions for your hardware notch.",
                style = AppTheme.typography.bodySmall,
                color = Color(0xFF8E8E93)
            )

            Spacer(modifier = Modifier.height(36.dp))

            // =================================================================
            // EDITORIAL VERTICAL LIST (NO CARDS, NO BORDERS, NO NEON)
            // =================================================================
            experiences.forEachIndexed { index, item ->
                val interactionSource = remember { MutableInteractionSource() }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null,
                            onClick = item.onClick
                        )
                        .padding(vertical = 18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            AppText(
                                text = item.title,
                                style = AppTheme.typography.body,
                                fontWeight = FontWeight.Medium,
                                color = Color.White
                            )
                        }

                        AppText(
                            text = item.description,
                            style = AppTheme.typography.bodySmall,
                            color = Color(0xFF8E8E93),
                            lineHeight = 18.sp
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(start = 16.dp)
                    ) {
                        AppText(
                            text = item.statusText,
                            style = AppTheme.typography.caption,
                            color = Color(0xFF636366)
                        )
                        AppText(
                            text = "→",
                            style = AppTheme.typography.body,
                            color = Color(0xFF48484A)
                        )
                    }
                }

                if (index < experiences.size - 1) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(0.5.dp)
                            .background(Color(0x0EFFFFFF))
                    )
                }
            }
        }
    }
}
