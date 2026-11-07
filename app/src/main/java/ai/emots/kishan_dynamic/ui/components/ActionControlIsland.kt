package ai.emots.kishan_dynamic.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
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
import ai.emots.kishan_dynamic.ui.theme.auroraPurchaseGradient
import ai.emots.kishan_dynamic.ui.components.AppText

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
            .padding(start = 20.dp, end = 20.dp, top = 22.dp, bottom = 14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(ai.emots.kishan_dynamic.ui.theme.AppTheme.spacing.sm)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = "Quick controls",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Your essentials, one tap away",
                    color = IslandColors.TextSecondary,
                    fontSize = 11.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(ai.emots.kishan_dynamic.ui.theme.AppTheme.spacing.sm)
            ) {
                if (notificationCount > 0) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(percent = 50))
                            .background(IslandColors.CapsuleRed.copy(alpha = 0.22f))
                            .clickable(onClick = onOpenNotifications)
                            .padding(horizontal = 7.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "$notificationCount ${if (notificationCount == 1) "alert" else "alerts"}",
                            color = IslandColors.CapsuleRed,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                ActionUtilityButton(
                    glyph = AppleGlyph.Lock,
                    onClick = { onUtilityAction(ActionUtilityAction.LOCK) }
                )
                ActionUtilityButton(
                    glyph = AppleGlyph.Settings,
                    onClick = { onUtilityAction(ActionUtilityAction.SETTINGS) }
                )
                ActionUtilityButton(
                    glyph = AppleGlyph.Camera,
                    onClick = { onUtilityAction(ActionUtilityAction.CAMERA) }
                )
                ActionUtilityButton(
                    glyph = AppleGlyph.Expand,
                    onClick = { onUtilityAction(ActionUtilityAction.EDIT) }
                )
            }
        }
        val tileRows = systemTiles.chunked(4)
        for (row in tileRows) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (tile in row) {
                    QuickControlChip(
                        glyph = tile.toGlyph(),
                        label = tile.title,
                        state = systemStates[tile],
                        modifier = Modifier.weight(1f),
                        onClick = { onSystemTileClick(tile) }
                    )
                }
                repeat(4 - row.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
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
                        modifier = Modifier.width(58.dp),
                        onClick = { onCustomActionClick(action.actionId) }
                    )
                }
            }
        }
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
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ActionUtilitySlider(
                glyph = AppleGlyph.Sparkles,
                value = brightnessLevel,
                onValueChange = onBrightnessChange,
                activeColor = Color.White,
                modifier = Modifier.weight(1f)
            )
            ActionUtilitySlider(
                glyph = AppleGlyph.Speaker,
                value = mediaVolumeLevel,
                onValueChange = onMediaVolumeChange,
                activeColor = IslandColors.CapsuleCyan,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
internal fun ActionUtilityButton(
    glyph: AppleGlyph,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .minimumInteractiveComponentSize()
            .semantics { contentDescription = if (glyph == AppleGlyph.Bell) "Open notifications" else "Open ${glyph.name.lowercase()}" }
            .size(24.dp)
            .clip(CircleShape)
            .background(Color(0xFF25262B))
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        AppleIcon(glyph = glyph, tint = Color.White, size = 12.dp)
    }
}

@Composable
internal fun ActionUtilitySlider(
    glyph: AppleGlyph,
    value: Float,
    onValueChange: (Float) -> Unit,
    activeColor: Color = Color.White,
    modifier: Modifier = Modifier
) {
    val progress = value.coerceIn(0f, 1f)

    Box(
        modifier = modifier
            .height(ai.emots.kishan_dynamic.ui.theme.AppTheme.island.minimumActionTarget)
            .clip(RoundedCornerShape(percent = 50))
            .background(Color(0xFF222326))
            .border(0.75.dp, Color(0x22FFFFFF), RoundedCornerShape(percent = 50))
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
        if (progress > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(progress)
                    .background(activeColor)
            )
        }

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val iconTint = if (progress > 0.16f) Color(0xFF141416) else Color.White
            AppleIcon(glyph = glyph, tint = iconTint, size = 15.dp)

            val textTint = if (progress > 0.82f) Color(0xFF141416) else Color(0xB3FFFFFF)
            Text(
                text = "${(progress * 100).toInt()}%",
                color = textTint,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
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
            .heightIn(min = ai.emots.kishan_dynamic.ui.theme.AppTheme.island.minimumActionTarget)
            .clip(RoundedCornerShape(12.dp))
            .background(Brush.linearGradient(listOf(IslandColors.TimerCyan.copy(alpha = 0.28f), IslandColors.Purple.copy(alpha = 0.24f))))
            .clickable(role = Role.Button, onClick = onClick)
            .padding(vertical = 7.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        AppleIcon(glyph = action.toGlyph(), tint = IslandColors.TimerCyan, size = 16.dp)
        Text(text = action.label, color = Color.White, fontSize = 11.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
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
            .heightIn(min = ai.emots.kishan_dynamic.ui.theme.AppTheme.island.minimumActionTarget)
            .clip(RoundedCornerShape(12.dp))
            .background(Brush.linearGradient(listOf(IslandColors.Purple.copy(alpha = 0.34f), IslandColors.Indigo.copy(alpha = 0.22f))))
            .clickable(role = Role.Button, onClick = onClick)
            .padding(vertical = 7.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Box(
            modifier = Modifier
                .size(18.dp)
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
        Text(text = app.appName, color = Color.White, fontSize = 11.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
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
            .heightIn(min = ai.emots.kishan_dynamic.ui.theme.AppTheme.island.minimumActionTarget)
            .clip(RoundedCornerShape(12.dp))
            .background(Brush.linearGradient(listOf(IslandColors.CapsuleGreen.copy(alpha = 0.32f), IslandColors.CapsuleCyan.copy(alpha = 0.18f))))
            .clickable(role = Role.Button, onClick = onClick)
            .padding(vertical = 7.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        ContactAvatar(
            name = contact.name,
            avatarUri = contact.photoUri,
            modifier = Modifier.size(18.dp),
            contentDescription = contact.name
        )
        Text(text = contact.name, color = Color.White, fontSize = 11.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
internal fun QuickControlChip(
    glyph: AppleGlyph,
    label: String,
    state: Boolean?,
    modifier: Modifier,
    onClick: () -> Unit
) {
    val active = state == true
    val unreadable = state == null
    val accent = when {
        active -> IslandColors.CapsuleCyan
        unreadable -> Color(0xFFFFB340)
        else -> IslandColors.TextSecondary
    }
    Column(
        modifier = modifier
            .heightIn(min = ai.emots.kishan_dynamic.ui.theme.AppTheme.island.minimumActionTarget)
            .clip(RoundedCornerShape(12.dp))
            .background(
                when {
                    active -> IslandColors.CapsuleCyan.copy(alpha = 0.24f)
                    unreadable -> Color(0xFFFFB340).copy(alpha = 0.14f)
                    else -> IslandColors.Gray6
                }
            )
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 7.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Box(modifier = Modifier.size(16.dp)) {
            AppleIcon(glyph = glyph, tint = accent, size = 16.dp)
            if (unreadable) {
                Box(
                    modifier = Modifier
                        .size(5.dp)
                        .align(Alignment.TopEnd)
                        .clip(CircleShape)
                        .background(Color(0xFFFFB340))
                )
            }
        }
        Text(
            text = label,
            color = if (active) Color.White else accent,
            fontSize = 11.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
internal fun PremiumExpiryExpandedContent(
    hoursRemaining: Int,
    onControlAction: (IslandControlAction) -> Unit
) {
    val tokens = ai.emots.kishan_dynamic.ui.theme.AppTheme.island
    Column(
        modifier = Modifier.fillMaxWidth().padding(tokens.expandedContentInset),
        verticalArrangement = Arrangement.spacedBy(tokens.expandedContentGap)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(tokens.expandedContentGap)
        ) {
            Box(
                modifier = Modifier.size(tokens.expandedIconSize).clip(CircleShape)
                    .background(ai.emots.kishan_dynamic.ui.theme.AppTheme.colors.auroraPurchaseGradient()),
                contentAlignment = Alignment.Center
            ) {
                AppleIcon(glyph = AppleGlyph.Crown, tint = Color.White, size = tokens.compactIconSize)
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(
                ai.emots.kishan_dynamic.ui.theme.AppTheme.spacing.xs
            )) {
                AppText(
                    text = "Aurora Pro access",
                    style = ai.emots.kishan_dynamic.ui.theme.AppTheme.typography.islandTitle,
                    color = Color.White
                )
                AppText(
                    text = if (hoursRemaining <= 0) "Your Pro access has ended." else "Your Pro access ends soon.",
                    style = ai.emots.kishan_dynamic.ui.theme.AppTheme.typography.bodySmall,
                    color = IslandColors.TextSecondary
                )
            }
        }
        androidx.compose.foundation.layout.BoxWithConstraints(Modifier.fillMaxWidth()) {
            val stacked = maxWidth < 240.dp || androidx.compose.ui.platform.LocalDensity.current.fontScale > 1.3f
            if (stacked) {
                Column(verticalArrangement = Arrangement.spacedBy(tokens.compactContentGap)) {
                    PremiumExpiryAction("Watch Ads", false, onControlAction)
                    PremiumExpiryAction("Buy Premium", true, onControlAction)
                }
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(tokens.compactContentGap)) {
                    PremiumExpiryAction("Watch Ads", false, onControlAction, Modifier.weight(1f))
                    PremiumExpiryAction("Buy Premium", true, onControlAction, Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun PremiumExpiryAction(
    label: String, primary: Boolean, onAction: (IslandControlAction) -> Unit, modifier: Modifier = Modifier
) {
    ai.emots.kishan_dynamic.ui.kit.AppButton(
        text = label,
        onClick = { onAction(if (primary) IslandControlAction.PremiumBuy else IslandControlAction.PremiumWatchAd) },
        modifier = modifier,
        style = if (primary) ai.emots.kishan_dynamic.ui.kit.AppButtonStyle.AuroraPro
            else ai.emots.kishan_dynamic.ui.kit.AppButtonStyle.Secondary
    )
}
