package ai.emots.kishan_dynamic.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ai.emots.kishan_dynamic.ui.components.AppleButton
import ai.emots.kishan_dynamic.ui.components.AppleButtonStyle
import ai.emots.kishan_dynamic.ui.components.AppleGlyph
import ai.emots.kishan_dynamic.ui.components.AppleIcon
import ai.emots.kishan_dynamic.ui.components.AtmosphericBackground
import ai.emots.kishan_dynamic.ui.components.LuxuryCard
import ai.emots.kishan_dynamic.ui.components.LuxuryTopBar
import ai.emots.kishan_dynamic.ui.dialogs.PermissionExplanationSheet
import ai.emots.kishan_dynamic.ui.theme.AuroraTokens

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
    // Interactive mock permission states
    var accessibilityGranted by remember { mutableStateOf(true) }
    var notificationGranted by remember { mutableStateOf(false) }
    var callGranted by remember { mutableStateOf(true) }
    var batteryGranted by remember { mutableStateOf(false) }
    var explanationPermission by remember { mutableStateOf<PermissionItemData?>(null) }

    val permissionList = remember(accessibilityGranted, notificationGranted, callGranted, batteryGranted) {
        listOf(
            PermissionItemData(
                id = "accessibility",
                title = "Accessibility & Notch Overlay",
                description = "Draws fluid dynamic island cards over the status bar and camera hole.",
                glyph = AppleGlyph.Sparkles,
                isGranted = accessibilityGranted,
                isRequired = true,
                privacyDetail = "Only used to draw the dynamic island overlay at the top of your screen. No keystrokes, personal screen contents, or inputs are ever read or logged."
            ),
            PermissionItemData(
                id = "notification",
                title = "Notifications & Live Activities",
                description = "Displays WhatsApp, Spotify, timers, and alerts seamlessly inside the island.",
                glyph = AppleGlyph.Bell,
                isGranted = notificationGranted,
                isRequired = true,
                privacyDetail = "Reads incoming notifications locally on device solely to render the heads-up banner. Zero notification data is ever transmitted off your phone."
            ),
            PermissionItemData(
                id = "telecom",
                title = "Phone State & Caller HUD",
                description = "Shows incoming caller name, live call duration, and quick mute shortcuts.",
                glyph = AppleGlyph.Phone,
                isGranted = callGranted,
                isRequired = true,
                privacyDetail = "Detects active phone calls locally to present the call HUD and duration counter. No audio is recorded."
            ),
            PermissionItemData(
                id = "battery",
                title = "Background Battery Guardian",
                description = "Prevents aggressive Android OEM battery cleaners from killing the island service.",
                glyph = AppleGlyph.Battery,
                isGranted = batteryGranted,
                isRequired = false,
                privacyDetail = "Keeps the overlay alive in memory so the island doesn't disappear when opening heavy apps."
            )
        )
    }

    val grantedCount = permissionList.count { it.isGranted }
    val totalCount = permissionList.size
    val progress = grantedCount.toFloat() / totalCount.toFloat()
    val canProceed = accessibilityGranted && notificationGranted

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = spring(dampingRatio = 0.75f, stiffness = Spring.StiffnessMedium),
        label = "perm_progress"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "shield_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    AtmosphericBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = AuroraTokens.Spacing.screenGutter)
                .padding(bottom = 60.dp),
            verticalArrangement = Arrangement.spacedBy(AuroraTokens.Spacing.sectionGap)
        ) {
            // Header
            LuxuryTopBar(
                title = "System Shield",
                subtitle = "Security, notch overlay & permissions",
                onBack = onBack
            )

            // =================================================================
            // HERO SHIELD STATUS STAGE
            // =================================================================
            LuxuryCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(
                        modifier = Modifier.weight(1f).padding(end = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Status Badge Pill
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(
                                    if (canProceed) AuroraTokens.Palette.success.copy(alpha = 0.2f)
                                    else AuroraTokens.Palette.primary.copy(alpha = 0.25f)
                                )
                                .border(
                                    1.dp,
                                    if (canProceed) AuroraTokens.Palette.success.copy(alpha = 0.5f)
                                    else AuroraTokens.Palette.primary.copy(alpha = 0.4f),
                                    CircleShape
                                )
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                AppleIcon(
                                    glyph = if (canProceed) AppleGlyph.Check else AppleGlyph.Shield,
                                    tint = if (canProceed) AuroraTokens.Palette.success else AuroraTokens.Palette.primaryLight,
                                    size = 12.dp
                                )
                                Text(
                                    text = if (canProceed) "SYSTEM SECURE" else "ACTIVATION IN PROGRESS",
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (canProceed) AuroraTokens.Palette.success else AuroraTokens.Palette.primaryLight,
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }

                        Text(
                            text = "$grantedCount of $totalCount Armed",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        Text(
                            text = if (canProceed) "Core capabilities satisfied. Ready to engage island overlay."
                            else "Grant required permissions below to draw fluid island over your notch.",
                            fontSize = 12.sp,
                            color = AuroraTokens.TextColor.secondary,
                            lineHeight = 16.sp
                        )
                    }

                    // Circular Progress Dial
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .scale(pulseScale),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val strokeWidth = 6.dp.toPx()
                            val arcSize = Size(size.width - strokeWidth, size.height - strokeWidth)
                            val topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f)

                            // Track
                            drawArc(
                                color = Color(0xFF221C35),
                                startAngle = -90f,
                                sweepAngle = 360f,
                                useCenter = false,
                                topLeft = topLeft,
                                size = arcSize,
                                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                            )

                            // Active Gradient Sweep
                            drawArc(
                                brush = Brush.sweepGradient(
                                    listOf(
                                        AuroraTokens.Palette.primary,
                                        AuroraTokens.Palette.secondary,
                                        AuroraTokens.Palette.success
                                    )
                                ),
                                startAngle = -90f,
                                sweepAngle = 360f * animatedProgress,
                                useCenter = false,
                                topLeft = topLeft,
                                size = arcSize,
                                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${(animatedProgress * 100).toInt()}%",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            // =================================================================
            // ON-DEVICE PRIVACY GUARANTEE BANNER
            // =================================================================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF131122))
                    .border(1.dp, Color(0x18FFFFFF), RoundedCornerShape(14.dp))
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(AuroraTokens.Palette.success.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    AppleIcon(glyph = AppleGlyph.Shield, tint = AuroraTokens.Palette.success, size = 15.dp)
                }

                Column {
                    Text(
                        text = "100% On-Device Processing",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                    Text(
                        text = "No personal data, notifications, or keystrokes ever leave your device.",
                        fontSize = 11.sp,
                        color = AuroraTokens.TextColor.secondary
                    )
                }
            }

            // =================================================================
            // PERMISSION CARDS LIST
            // =================================================================
            Text(
                text = "System Capabilities",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                permissionList.forEach { item ->
                    LuxuryCard {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Glyph Disc
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (item.isGranted) AuroraTokens.Palette.success.copy(alpha = 0.2f)
                                        else AuroraTokens.Palette.primary.copy(alpha = 0.2f)
                                    )
                                    .border(
                                        1.dp,
                                        if (item.isGranted) AuroraTokens.Palette.success.copy(alpha = 0.4f)
                                        else AuroraTokens.Palette.primary.copy(alpha = 0.3f),
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                AppleIcon(
                                    glyph = item.glyph,
                                    tint = if (item.isGranted) AuroraTokens.Palette.success else AuroraTokens.Palette.primaryLight,
                                    size = 22.dp
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            // Details
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = item.title,
                                        fontSize = 14.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )

                                    if (item.isRequired) {
                                        Box(
                                            modifier = Modifier
                                                .clip(CircleShape)
                                                .background(AuroraTokens.Palette.primary.copy(alpha = 0.25f))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "REQ",
                                                fontSize = 8.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = AuroraTokens.Palette.primaryLight
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(2.dp))

                                Text(
                                    text = item.description,
                                    fontSize = 11.5.sp,
                                    color = AuroraTokens.TextColor.secondary,
                                    lineHeight = 15.sp
                                )
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            // Action: Info Disc + Status/Grant Button
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Info Button
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF201B30))
                                        .border(1.dp, Color(0x18FFFFFF), CircleShape)
                                        .clickable { explanationPermission = item },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "i",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = AuroraTokens.TextColor.secondary
                                    )
                                }

                                // Status Pill or Grant Button
                                if (item.isGranted) {
                                    Box(
                                        modifier = Modifier
                                            .clip(CircleShape)
                                            .background(AuroraTokens.Palette.success.copy(alpha = 0.2f))
                                            .border(1.dp, AuroraTokens.Palette.success.copy(alpha = 0.4f), CircleShape)
                                            .clickable {
                                                when (item.id) {
                                                    "accessibility" -> accessibilityGranted = false
                                                    "notification" -> notificationGranted = false
                                                    "telecom" -> callGranted = false
                                                    "battery" -> batteryGranted = false
                                                }
                                            }
                                            .padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            AppleIcon(glyph = AppleGlyph.Check, tint = AuroraTokens.Palette.success, size = 12.dp)
                                            Text(
                                                text = "Active",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = AuroraTokens.Palette.success
                                            )
                                        }
                                    }
                                } else {
                                    AppleButton(
                                        text = "Grant",
                                        onClick = {
                                            when (item.id) {
                                                "accessibility" -> accessibilityGranted = true
                                                "notification" -> notificationGranted = true
                                                "telecom" -> callGranted = true
                                                "battery" -> batteryGranted = true
                                            }
                                        },
                                        glyph = AppleGlyph.Sparkles,
                                        style = AppleButtonStyle.PRIMARY,
                                        paddingVertical = 6.dp,
                                        paddingHorizontal = 12.dp,
                                        fontSize = 12.sp,
                                        fillMaxWidth = false
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // =================================================================
            // MASTER ACTION PILL
            // =================================================================
            AppleButton(
                text = if (canProceed) "Engage Dynamic Island" else "Grant Required Permissions",
                onClick = onContinue,
                glyph = if (canProceed) AppleGlyph.Sparkles else AppleGlyph.Shield,
                style = if (canProceed) AppleButtonStyle.PRIMARY else AppleButtonStyle.SECONDARY,
                paddingVertical = 16.dp
            )
        }

        // Contextual Privacy Explanation Modal
        explanationPermission?.let { permission ->
            PermissionExplanationSheet(
                permission = permission,
                onDismiss = { explanationPermission = null },
                onGrantClick = {
                    when (permission.id) {
                        "accessibility" -> accessibilityGranted = true
                        "notification" -> notificationGranted = true
                        "telecom" -> callGranted = true
                        "battery" -> batteryGranted = true
                    }
                    explanationPermission = null
                }
            )
        }
    }
}
