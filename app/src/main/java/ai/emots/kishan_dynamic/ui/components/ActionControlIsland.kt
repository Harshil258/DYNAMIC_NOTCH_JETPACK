package ai.emots.kishan_dynamic.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ai.emots.kishan_dynamic.data.model.ActionAppShortcut
import ai.emots.kishan_dynamic.data.model.ActionContactShortcut
import ai.emots.kishan_dynamic.data.model.ActionCustomShortcut
import ai.emots.kishan_dynamic.data.model.ActionSystemTile
import ai.emots.kishan_dynamic.data.model.ActionUtilityAction
import ai.emots.kishan_dynamic.ui.theme.IslandColors
import ai.emots.kishan_dynamic.ui.theme.IslandReferenceFonts

@Composable
internal fun ActionControlExpandedContent(
    systemTiles: List<ActionSystemTile>,
    systemStates: Map<ActionSystemTile, Boolean?>,
    apps: List<ActionAppShortcut>,
    contacts: List<ActionContactShortcut>,
    customActions: List<ActionCustomShortcut>,
    onAppClick: (String) -> Unit,
    onContactClick: (String) -> Unit,
    onCustomActionClick: (String) -> Unit,
    onSystemTileClick: (ActionSystemTile) -> Unit,
    brightnessLevel: Float,
    mediaVolumeLevel: Float,
    onBrightnessChange: (Float) -> Unit,
    onMediaVolumeChange: (Float) -> Unit,
    notificationCount: Int,
    onUtilityAction: (ActionUtilityAction) -> Unit,
    onOpenNotifications: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(start = 18.dp, end = 18.dp, top = 16.dp, bottom = 14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Top Header Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                Text(
                    text = "Control Center",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = IslandReferenceFonts.Text
                )
                Text(
                    text = "Quick controls",
                    color = Color.White.copy(alpha = 0.65f),
                    fontSize = 11.sp,
                    fontFamily = IslandReferenceFonts.Text
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (notificationCount > 0) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(IslandColors.CapsuleRed.copy(alpha = 0.20f))
                            .border(0.5.dp, IslandColors.CapsuleRed.copy(alpha = 0.40f), RoundedCornerShape(12.dp))
                            .clickable(onClick = onOpenNotifications)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "$notificationCount ${if (notificationCount == 1) "alert" else "alerts"}",
                            color = IslandColors.CapsuleRed,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = IslandReferenceFonts.Text
                        )
                    }
                }
                ActionUtilityButton(
                    glyph = AppleGlyph.Lock,
                    onClick = { onUtilityAction(ActionUtilityAction.LOCK) }
                )
                ActionUtilityButton(
                    glyph = AppleGlyph.Camera,
                    onClick = { onUtilityAction(ActionUtilityAction.CAMERA) }
                )
                ActionUtilityButton(
                    glyph = AppleGlyph.Settings,
                    onClick = { onUtilityAction(ActionUtilityAction.SETTINGS) }
                )
            }
        }

        // Quick Controls Grid
        val tileColumns = when {
            LocalDensity.current.fontScale > 1.3f -> 2
            LocalConfiguration.current.screenWidthDp < 360 -> 3
            else -> 4
        }
        val tileRows = systemTiles.chunked(tileColumns)
        for (row in tileRows) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (tile in row) {
                    QuickControlChip(
                        tile = tile,
                        state = systemStates[tile],
                        modifier = Modifier.weight(1f),
                        onClick = { onSystemTileClick(tile) }
                    )
                }
                repeat(tileColumns - row.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }

        // Optional Custom Actions
        if (customActions.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (action in customActions) {
                    ActionCustomChip(
                        action = action,
                        modifier = Modifier.width(62.dp),
                        onClick = { onCustomActionClick(action.actionId) }
                    )
                }
            }
        }

        // Optional App Shortcuts
        if (apps.isNotEmpty()) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                for (app in apps.take(4)) {
                    ActionAppChip(
                        app = app,
                        modifier = Modifier.weight(1f),
                        onClick = { onAppClick(app.packageName) }
                    )
                }
                repeat((4 - apps.take(4).size).coerceAtLeast(0)) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }

        // Optional Contacts
        if (contacts.isNotEmpty()) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                for (contact in contacts.take(4)) {
                    ActionContactChip(
                        contact = contact,
                        modifier = Modifier.weight(1f),
                        onClick = { onContactClick(contact.phoneNumber) }
                    )
                }
                repeat((4 - contacts.take(4).size).coerceAtLeast(0)) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }

        // Brightness & Volume Sliders
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ActionUtilitySlider(
                glyph = AppleGlyph.Sparkles,
                value = brightnessLevel,
                onValueChange = onBrightnessChange,
                modifier = Modifier.weight(1f)
            )
            ActionUtilitySlider(
                glyph = AppleGlyph.Speaker,
                value = mediaVolumeLevel,
                onValueChange = onMediaVolumeChange,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
internal fun ActionUtilityButton(glyph: AppleGlyph, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(30.dp)
            .clip(CircleShape)
            .background(Color(0xFF22242D))
            .border(0.5.dp, Color.White.copy(alpha = 0.12f), CircleShape)
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        AppleIcon(glyph = glyph, tint = Color.White.copy(alpha = 0.85f), size = 14.dp)
    }
}

@Composable
internal fun ActionUtilitySlider(
    glyph: AppleGlyph,
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val progress = value.coerceIn(0f, 1f)

    Box(
        modifier = modifier
            .height(44.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(Color(0xFF1A1C24))
            .border(0.75.dp, Color.White.copy(alpha = 0.10f), RoundedCornerShape(22.dp))
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = { offset ->
                        val newProgress = (offset.x / size.width.toFloat()).coerceIn(0f, 1f)
                        onValueChange(newProgress)
                    }
                )
            }
            .pointerInput(Unit) {
                detectHorizontalDragGestures { change, _ ->
                    change.consume()
                    val newProgress = (change.position.x / size.width.toFloat()).coerceIn(0f, 1f)
                    onValueChange(newProgress)
                }
            }
    ) {
        val fillBrush = if (glyph == AppleGlyph.Sparkles) {
            Brush.horizontalGradient(listOf(Color(0xFFFFD60A), Color(0xFFFFF3B0)))
        } else {
            Brush.horizontalGradient(listOf(Color(0xFF007AFF), Color(0xFF64D2FF)))
        }

        if (progress > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(progress)
                    .clip(RoundedCornerShape(22.dp))
                    .background(fillBrush)
            )
        }

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val iconTint = if (progress > 0.18f) Color(0xFF0D0F14) else Color.White
            AppleIcon(glyph = glyph, tint = iconTint, size = 16.dp)

            val textTint = if (progress > 0.82f) Color(0xFF0D0F14) else Color.White.copy(alpha = 0.85f)
            Text(
                text = "${(progress * 100).toInt()}%",
                color = textTint,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = IslandReferenceFonts.Text
            )
        }
    }
}

internal fun tileActiveColor(tile: ActionSystemTile): Color = when (tile) {
    ActionSystemTile.WIFI, ActionSystemTile.BLUETOOTH -> Color(0xFF007AFF) // Apple iOS Blue
    ActionSystemTile.MOBILE_DATA, ActionSystemTile.HOTSPOT -> Color(0xFF34C759) // Apple iOS Green
    ActionSystemTile.TORCH, ActionSystemTile.AUTO_BRIGHTNESS -> Color(0xFFFF9F0A) // Apple iOS Amber
    ActionSystemTile.LOCATION -> Color(0xFF5856D6) // Apple iOS Indigo
    ActionSystemTile.DO_NOT_DISTURB, ActionSystemTile.DARK_MODE -> Color(0xFFAF52DE) // Apple iOS Purple
    ActionSystemTile.ROTATION_LOCK -> Color(0xFFFF3B30) // Apple iOS Red
    ActionSystemTile.POWER_SAVER -> Color(0xFFFFCC00) // Apple iOS Yellow
    else -> Color(0xFF007AFF)
}

internal fun ActionSystemTile.toGlyph(): AppleGlyph = when (this) {
    ActionSystemTile.WIFI -> AppleGlyph.Wifi
    ActionSystemTile.BLUETOOTH -> AppleGlyph.Bluetooth
    ActionSystemTile.MOBILE_DATA -> AppleGlyph.Globe
    ActionSystemTile.TORCH -> AppleGlyph.Torch
    ActionSystemTile.LOCATION -> AppleGlyph.Location
    ActionSystemTile.ROTATION_LOCK -> AppleGlyph.Rotate
    ActionSystemTile.AIRPLANE_MODE -> AppleGlyph.Airplane
    ActionSystemTile.DO_NOT_DISTURB -> AppleGlyph.Moon
    ActionSystemTile.HOTSPOT -> AppleGlyph.PersonalHotspot
    ActionSystemTile.SCREENSHOT -> AppleGlyph.Camera
    ActionSystemTile.DARK_MODE -> AppleGlyph.Moon
    ActionSystemTile.AUTO_BRIGHTNESS -> AppleGlyph.Sparkles
    ActionSystemTile.POWER_SAVER -> AppleGlyph.Power
    ActionSystemTile.SYNC -> AppleGlyph.Reset
    ActionSystemTile.NFC -> AppleGlyph.Link
}

internal fun ActionCustomShortcut.toGlyph(): AppleGlyph = when (iconKey) {
    "camera" -> AppleGlyph.Camera
    "bell" -> AppleGlyph.Bell
    "calendar" -> AppleGlyph.History
    else -> AppleGlyph.Settings
}

@Composable
internal fun ActionCustomChip(
    action: ActionCustomShortcut,
    modifier: Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .heightIn(min = 52.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Brush.linearGradient(listOf(IslandColors.TimerCyan.copy(alpha = 0.22f), IslandColors.Purple.copy(alpha = 0.18f))))
            .border(0.5.dp, IslandColors.TimerCyan.copy(alpha = 0.30f), RoundedCornerShape(14.dp))
            .clickable(role = Role.Button, onClick = onClick)
            .padding(vertical = 7.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        AppleIcon(glyph = action.toGlyph(), tint = IslandColors.TimerCyan, size = 16.dp)
        Text(text = action.label, color = Color.White, fontSize = 11.sp, fontFamily = IslandReferenceFonts.Text, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
internal fun ActionAppChip(
    app: ActionAppShortcut,
    modifier: Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .heightIn(min = 52.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Brush.linearGradient(listOf(IslandColors.Purple.copy(alpha = 0.24f), IslandColors.Indigo.copy(alpha = 0.16f))))
            .border(0.5.dp, IslandColors.Purple.copy(alpha = 0.30f), RoundedCornerShape(14.dp))
            .clickable(role = Role.Button, onClick = onClick)
            .padding(vertical = 7.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .background(IslandColors.Purple),
            contentAlignment = Alignment.Center
        ) {
            PackageIcon(
                packageName = app.packageName,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape),
                contentDescription = app.appName
            )
        }
        Text(text = app.appName, color = Color.White, fontSize = 11.sp, fontFamily = IslandReferenceFonts.Text, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
internal fun ActionContactChip(
    contact: ActionContactShortcut,
    modifier: Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .heightIn(min = 52.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Brush.linearGradient(listOf(IslandColors.CapsuleGreen.copy(alpha = 0.22f), IslandColors.CapsuleCyan.copy(alpha = 0.14f))))
            .border(0.5.dp, IslandColors.CapsuleGreen.copy(alpha = 0.30f), RoundedCornerShape(14.dp))
            .clickable(role = Role.Button, onClick = onClick)
            .padding(vertical = 7.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        ContactAvatar(
            name = contact.name,
            avatarUri = contact.photoUri,
            modifier = Modifier.size(20.dp),
            contentDescription = contact.name
        )
        Text(text = contact.name, color = Color.White, fontSize = 11.sp, fontFamily = IslandReferenceFonts.Text, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
internal fun QuickControlChip(
    tile: ActionSystemTile,
    state: Boolean?,
    modifier: Modifier,
    onClick: () -> Unit
) {
    val active = state == true
    val activeColor = tileActiveColor(tile)

    Column(
        modifier = modifier
            .heightIn(min = 52.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(
                if (active) activeColor.copy(alpha = 0.22f)
                else Color(0xFF1E2028)
            )
            .border(
                if (active) 0.75.dp else 0.5.dp,
                if (active) activeColor.copy(alpha = 0.45f) else Color.White.copy(alpha = 0.08f),
                RoundedCornerShape(14.dp)
            )
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 7.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .background(
                    if (active) activeColor.copy(alpha = 0.25f)
                    else Color.White.copy(alpha = 0.06f)
                ),
            contentAlignment = Alignment.Center
        ) {
            AppleIcon(glyph = tile.toGlyph(), tint = if (active) activeColor else Color.White, size = 13.dp)
        }
        Text(
            text = tile.title,
            color = if (active) Color.White else Color.White.copy(alpha = 0.70f),
            fontSize = 11.sp,
            fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal,
            fontFamily = IslandReferenceFonts.Text,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
internal fun PremiumExpiryExpandedContent(
    hoursRemaining: Int,
    onControlAction: (IslandControlAction) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Identity row
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(13.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFF9747FF), Color(0xFF5E5CE6))
                        )
                    )
                    .border(
                        0.75.dp,
                        Color(0xFFBF5AF2).copy(alpha = 0.50f),
                        RoundedCornerShape(13.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                AppleIcon(glyph = AppleGlyph.Crown, tint = Color(0xFFFFD60A), size = 20.dp)
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Aurora Pro",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = IslandReferenceFonts.Text
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(5.dp))
                            .background(Color(0x33FFD60A))
                            .border(0.5.dp, Color(0x80FFD60A), RoundedCornerShape(5.dp))
                            .padding(horizontal = 5.dp, vertical = 1.5.dp)
                    ) {
                        Text(
                            text = "ACTIVE",
                            color = Color(0xFFFFD60A),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = IslandReferenceFonts.Text
                        )
                    }
                }
                Text(
                    text = if (hoursRemaining <= 0) "Your Pro access has ended." else "Pro features active • Renews in ${hoursRemaining}h",
                    color = Color.White.copy(alpha = 0.70f),
                    fontSize = 12.sp,
                    fontFamily = IslandReferenceFonts.Text,
                    maxLines = 1
                )
            }
        }

        // Perks micro-strip
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            PremiumPerkChip(text = "✦ 60 Islands", modifier = Modifier.weight(1f))
            PremiumPerkChip(text = "✦ Zero Ads", modifier = Modifier.weight(1f))
            PremiumPerkChip(text = "✦ Notch Tuner", modifier = Modifier.weight(1f))
        }

        // Action buttons row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Watch Ads button (+24h)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(38.dp)
                    .clip(RoundedCornerShape(19.dp))
                    .background(Color(0xFF22242F))
                    .border(0.75.dp, Color.White.copy(alpha = 0.14f), RoundedCornerShape(19.dp))
                    .clickable(role = Role.Button) {
                        onControlAction(IslandControlAction.PremiumWatchAd)
                    },
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    AppleIcon(glyph = AppleGlyph.Play, tint = Color.White, size = 12.dp)
                    Text(
                        text = "Watch Ad (+24h)",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = IslandReferenceFonts.Text
                    )
                }
            }

            // Buy Premium button (Radiant Aurora gradient)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(38.dp)
                    .clip(RoundedCornerShape(19.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xFF9747FF), Color(0xFF5E5CE6))
                        )
                    )
                    .border(0.75.dp, Color(0xFFBF5AF2).copy(alpha = 0.60f), RoundedCornerShape(19.dp))
                    .clickable(role = Role.Button) {
                        onControlAction(IslandControlAction.PremiumBuy)
                    },
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    AppleIcon(glyph = AppleGlyph.Crown, tint = Color(0xFFFFD60A), size = 13.dp)
                    Text(
                        text = "Unlock Lifetime",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = IslandReferenceFonts.Text
                    )
                }
            }
        }
    }
}

@Composable
private fun PremiumPerkChip(text: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(Color.White.copy(alpha = 0.06f))
            .border(0.5.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(6.dp))
            .padding(vertical = 3.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color.White.copy(alpha = 0.65f),
            fontSize = 9.5.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = IslandReferenceFonts.Text,
            maxLines = 1
        )
    }
}
