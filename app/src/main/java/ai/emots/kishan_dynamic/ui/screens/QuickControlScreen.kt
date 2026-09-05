package ai.emots.kishan_dynamic.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ai.emots.kishan_dynamic.data.model.ActionSystemTile
import ai.emots.kishan_dynamic.data.model.ActionCustomPreset
import ai.emots.kishan_dynamic.data.model.ActionCustomShortcut
import ai.emots.kishan_dynamic.data.model.DeviceContactInfo
import ai.emots.kishan_dynamic.data.model.InstalledAppInfo
import ai.emots.kishan_dynamic.data.premium.PremiumAccessResult
import ai.emots.kishan_dynamic.data.premium.PlayBillingPremiumAccessGateway
import ai.emots.kishan_dynamic.data.preferences.AuroraPreferences
import ai.emots.kishan_dynamic.data.repository.ActionIslandRepository
import ai.emots.kishan_dynamic.ui.components.AppText
import ai.emots.kishan_dynamic.ui.components.AppleGlyph
import ai.emots.kishan_dynamic.ui.components.AppleIcon
import ai.emots.kishan_dynamic.ui.components.DynamicIslandPill
import ai.emots.kishan_dynamic.ui.components.IslandDemoState
import ai.emots.kishan_dynamic.ui.dialogs.ProPaywallSheet
import ai.emots.kishan_dynamic.ui.kit.AppCard
import ai.emots.kishan_dynamic.ui.kit.AppButton
import ai.emots.kishan_dynamic.ui.kit.AppFootnote
import ai.emots.kishan_dynamic.ui.kit.AppIconButton
import ai.emots.kishan_dynamic.ui.kit.AppListCard
import ai.emots.kishan_dynamic.ui.kit.AppRowDivider
import ai.emots.kishan_dynamic.ui.kit.AppScreen
import ai.emots.kishan_dynamic.ui.kit.AppSectionSpacer
import ai.emots.kishan_dynamic.ui.kit.AppSectionTitle
import ai.emots.kishan_dynamic.ui.kit.AppTile
import ai.emots.kishan_dynamic.ui.kit.AppToggleRow
import ai.emots.kishan_dynamic.ui.kit.AppTopBar
import ai.emots.kishan_dynamic.ui.motion.appReveal
import ai.emots.kishan_dynamic.ui.theme.AppTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun QuickControlScreen(
    onBack: () -> Unit,
    onNavigateToPremium: () -> Unit = {}
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val repository = remember { ActionIslandRepository(context) }
    val preferences = remember { AuroraPreferences(context) }
    val premiumGateway = remember { PlayBillingPremiumAccessGateway(context) }
    val scope = rememberCoroutineScope()
    DisposableEffect(premiumGateway) {
        onDispose { premiumGateway.close() }
    }
    val billingStatus by premiumGateway.status.collectAsState(initial = null)
    val config by repository.config.collectAsState(initial = ai.emots.kishan_dynamic.data.model.ActionIslandConfig())
    val isProActive by preferences.isProActive.collectAsState(initial = false)
    var installedApps by remember { mutableStateOf<List<InstalledAppInfo>>(emptyList()) }
    var deviceContacts by remember { mutableStateOf<List<DeviceContactInfo>>(emptyList()) }
    var hasContactsPermission by remember {
        mutableStateOf(
            androidx.core.content.ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED
        )
    }
    var searchQuery by remember { mutableStateOf("") }
    var contactQuery by remember { mutableStateOf("") }
    var showPaywall by remember { mutableStateOf(false) }
    var premiumMessage by remember { mutableStateOf<String?>(null) }

    val effectiveMaxApps = if (isProActive) config.maxApps else minOf(config.maxApps, 3)
    val effectiveMaxContacts = if (isProActive) config.maxContacts else minOf(config.maxContacts, 2)
    val effectiveMaxCustomActions = if (isProActive) config.maxCustomActions else minOf(config.maxCustomActions, 2)

    val contactsPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> hasContactsPermission = granted }

    LaunchedEffect(repository) {
        withContext(Dispatchers.IO) { repository.pruneMissingApps() }
        installedApps = withContext(Dispatchers.IO) { repository.installedApps() }
    }

    LaunchedEffect(repository, hasContactsPermission) {
        deviceContacts = if (hasContactsPermission) {
            withContext(Dispatchers.IO) { repository.contacts() }
        } else {
            emptyList()
        }
    }

    val selectedPackages = remember(config.apps) { config.apps.map { it.packageName }.toSet() }
    val visibleApps = remember(installedApps, searchQuery, selectedPackages, effectiveMaxApps) {
        installedApps
            .filter { it.packageName !in selectedPackages }
            .filter { searchQuery.isBlank() || it.appName.contains(searchQuery, ignoreCase = true) }
            .take(12)
    }
    val visibleContacts = remember(deviceContacts, contactQuery, config.contacts, effectiveMaxContacts) {
        val selectedContactIds = config.contacts.map { it.contactId }.toSet()
        deviceContacts
            .filterNot { it.contactId in selectedContactIds }
            .filter { contactQuery.isBlank() || it.name.contains(contactQuery, ignoreCase = true) }
            .take(if (config.contacts.size < effectiveMaxContacts) 8 else 0)
    }
    val availableCustomActions = remember(config.customActions) {
        ActionCustomPreset.entries.filterNot { preset ->
            config.customActions.any { it.actionId == preset.actionId }
        }
    }

    AppScreen {
        AppTopBar(
            modifier = Modifier.appReveal(0),
            title = "Action Island",
            subtitle = "Choose what appears in your long-press control surface",
            onBack = onBack
        )

        AppCard(modifier = Modifier.appReveal(1)) {
            AppToggleRow(
                title = "Show Action Island",
                subtitle = "Long press the island to open your shortcuts",
                glyph = AppleGlyph.Controls,
                checked = config.isEnabled,
                onCheckedChange = { enabled -> scope.launch { repository.setEnabled(enabled) } }
            )
        }

        AppSectionSpacer()
        AppSectionTitle("Preview")
        AppCard(modifier = Modifier.appReveal(2)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(302.dp)
                    .clip(RoundedCornerShape(AppTheme.radius.lg))
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF090B12), Color(0xFF111B3C))
                        )
                    ),
                contentAlignment = Alignment.TopCenter
            ) {
                DynamicIslandPill(
                    state = IslandDemoState.ActionControlExpanded,
                    actionApps = config.apps.filter { it.isEnabled },
                    actionSystemTiles = config.systemTiles,
                    actionContacts = config.contacts.filter { it.isEnabled },
                    actionCustomActions = config.customActions,
                    modifier = Modifier.padding(top = 18.dp)
                )
            }
        }

        AppSectionSpacer()
        AppSectionTitle("System controls")
        AppCard(modifier = Modifier.appReveal(3)) {
            AppText(
                text = "Keep the controls you use most within reach. These choices are persisted on this device.",
                style = AppTheme.typography.bodySmall,
                color = AppTheme.colors.textSecondary
            )
            Spacer(modifier = Modifier.height(AppTheme.spacing.lg))
            ActionSystemTile.entries.chunked(3).forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm)
                ) {
                    row.forEach { tile ->
                        AppTile(
                            label = tile.title,
                            glyph = tile.toGlyph(),
                            selected = tile in config.systemTiles,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                val next = if (tile in config.systemTiles) {
                                    config.systemTiles.filterNot { it == tile }
                                } else {
                                    config.systemTiles + tile
                                }
                                scope.launch { repository.setSystemTiles(next) }
                            }
                        )
                    }
                    repeat(3 - row.size) { Spacer(modifier = Modifier.weight(1f)) }
                }
                if (row != ActionSystemTile.entries.chunked(3).last()) {
                    Spacer(modifier = Modifier.height(AppTheme.spacing.sm))
                }
            }
        }

        AppSectionSpacer()
        AppSectionTitle("Custom actions · ${config.customActions.size}/$effectiveMaxCustomActions")
        AppListCard(modifier = Modifier.appReveal(4)) {
            if (config.customActions.isEmpty()) {
                AppText(
                    text = "Add app-owned actions such as Settings or Camera to the island.",
                    style = AppTheme.typography.bodySmall,
                    color = AppTheme.colors.textSecondary,
                    modifier = Modifier.padding(AppTheme.layout.cardPadding)
                )
            } else {
                config.customActions.forEachIndexed { index, action ->
                    PinnedCustomActionRow(
                        action = action,
                        order = index + 1,
                        canMoveUp = index > 0,
                        canMoveDown = index < config.customActions.lastIndex,
                        onMoveUp = {
                            scope.launch {
                                repository.reorderCustomActions(config.customActions.moveItem(index, index - 1))
                            }
                        },
                        onMoveDown = {
                            scope.launch {
                                repository.reorderCustomActions(config.customActions.moveItem(index, index + 1))
                            }
                        },
                        onRemove = { scope.launch { repository.removeCustomAction(action.actionId) } }
                    )
                    if (index < config.customActions.lastIndex) AppRowDivider()
                }
            }
        }

        Spacer(modifier = Modifier.height(AppTheme.spacing.md))
        AppCard(modifier = Modifier.appReveal(5)) {
            if (availableCustomActions.isEmpty()) {
                AppText(
                    text = if (!isProActive && config.customActions.size >= effectiveMaxCustomActions) {
                        "Free plan includes 2 custom actions."
                    } else {
                        "All custom actions are already pinned."
                    },
                    style = AppTheme.typography.bodySmall,
                    color = AppTheme.colors.textSecondary
                )
            } else {
                availableCustomActions.forEachIndexed { index, preset ->
                    CustomActionOptionRow(
                        preset = preset,
                        enabled = config.customActions.size < effectiveMaxCustomActions,
                        onAdd = {
                            scope.launch {
                                repository.addCustomAction(
                                    ActionCustomShortcut(
                                        actionId = preset.actionId,
                                        label = preset.title,
                                        iconKey = preset.iconKey,
                                        order = config.customActions.size
                                    )
                                )
                            }
                        }
                    )
                    if (index < availableCustomActions.lastIndex) AppRowDivider()
                }
            }
        }

        AppSectionSpacer()
        AppSectionTitle("Pinned apps · ${config.apps.size}/${effectiveMaxApps}")
        AppListCard(modifier = Modifier.appReveal(3)) {
            if (config.apps.isEmpty()) {
                AppText(
                    text = "No apps pinned yet. Add your most-used apps below.",
                    style = AppTheme.typography.bodySmall,
                    color = AppTheme.colors.textSecondary,
                    modifier = Modifier.padding(AppTheme.layout.cardPadding)
                )
            } else {
                config.apps.forEachIndexed { index, app ->
                    PinnedAppRow(
                        packageName = app.packageName,
                        name = app.appName,
                        order = index + 1,
                        enabled = app.isEnabled,
                        onEnabledChange = { enabled ->
                            scope.launch { repository.setAppEnabled(app.packageName, enabled) }
                        },
                        onRemove = { scope.launch { repository.removeApp(app.packageName) } },
                        canMoveUp = index > 0,
                        canMoveDown = index < config.apps.lastIndex,
                        onMoveUp = {
                            scope.launch {
                                repository.reorderApps(config.apps.moveItem(index, index - 1))
                            }
                        },
                        onMoveDown = {
                            scope.launch {
                                repository.reorderApps(config.apps.moveItem(index, index + 1))
                            }
                        }
                    )
                    if (index < config.apps.lastIndex) AppRowDivider()
                }
            }
        }

        Spacer(modifier = Modifier.height(AppTheme.spacing.md))
        AppCard(modifier = Modifier.appReveal(4)) {
            SearchField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
            enabled = config.apps.size < effectiveMaxApps
            )
            Spacer(modifier = Modifier.height(AppTheme.spacing.sm))
            if (visibleApps.isEmpty()) {
                AppText(
                    text = when {
                        installedApps.isEmpty() -> "Loading installed apps…"
                        !isProActive && config.apps.size >= effectiveMaxApps -> "Free plan includes 3 pinned apps."
                        else -> "No matching apps available."
                    },
                    style = AppTheme.typography.bodySmall,
                    color = AppTheme.colors.textSecondary
                )
            } else {
                visibleApps.forEachIndexed { index, app ->
                    InstalledAppRow(
                        app = app,
                        onAdd = { scope.launch { repository.addApp(app) } }
                    )
                    if (index < visibleApps.lastIndex) AppRowDivider()
                }
            }
        }

        AppSectionSpacer()
        AppSectionTitle("Favorite contacts · ${config.contacts.size}/${effectiveMaxContacts}")
        AppListCard(modifier = Modifier.appReveal(5)) {
            if (!hasContactsPermission) {
                AppText(
                    text = "Allow contact access to choose people you call often. Nothing is uploaded.",
                    style = AppTheme.typography.bodySmall,
                    color = AppTheme.colors.textSecondary,
                    modifier = Modifier.padding(AppTheme.layout.cardPadding)
                )
                AppButton(
                    text = "Allow contacts",
                    glyph = AppleGlyph.Phone,
                    onClick = { contactsPermissionLauncher.launch(Manifest.permission.READ_CONTACTS) },
                    modifier = Modifier.padding(horizontal = AppTheme.layout.cardPadding, vertical = AppTheme.spacing.sm)
                )
            } else {
                if (config.contacts.isEmpty()) {
                    AppText(
                        text = "No favorite contacts yet. Add one below.",
                        style = AppTheme.typography.bodySmall,
                        color = AppTheme.colors.textSecondary,
                        modifier = Modifier.padding(AppTheme.layout.cardPadding)
                    )
                } else {
                    config.contacts.forEachIndexed { index, contact ->
                        PinnedContactRow(
                            name = contact.name,
                            number = contact.phoneNumber,
                            avatarUri = contact.photoUri,
                            order = index + 1,
                            enabled = contact.isEnabled,
                            onEnabledChange = { enabled ->
                                scope.launch { repository.setContactEnabled(contact.contactId, enabled) }
                            },
                            onRemove = { scope.launch { repository.removeContact(contact.contactId) } },
                            canMoveUp = index > 0,
                            canMoveDown = index < config.contacts.lastIndex,
                            onMoveUp = {
                                scope.launch {
                                    repository.reorderContacts(config.contacts.moveItem(index, index - 1))
                                }
                            },
                            onMoveDown = {
                                scope.launch {
                                    repository.reorderContacts(config.contacts.moveItem(index, index + 1))
                                }
                            }
                        )
                        if (index < config.contacts.lastIndex) AppRowDivider()
                    }
                }
                Spacer(modifier = Modifier.height(AppTheme.spacing.sm))
                BasicTextField(
                    value = contactQuery,
                    onValueChange = { contactQuery = it },
                    enabled = config.contacts.size < effectiveMaxContacts,
                    singleLine = true,
                    textStyle = AppTheme.typography.body.copy(color = AppTheme.colors.textPrimary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = AppTheme.layout.cardPadding)
                        .clip(RoundedCornerShape(AppTheme.radius.md))
                        .background(AppTheme.colors.surfaceVariant)
                        .padding(horizontal = AppTheme.spacing.md, vertical = AppTheme.spacing.sm),
                    decorationBox = { inner ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            AppleIcon(glyph = AppleGlyph.Search, tint = AppTheme.colors.textSecondary, size = 16.dp)
                            Spacer(modifier = Modifier.size(AppTheme.spacing.sm))
                            if (contactQuery.isEmpty()) AppText("Search contacts", style = AppTheme.typography.body, color = AppTheme.colors.textTertiary)
                            inner()
                        }
                    }
                )
                Spacer(modifier = Modifier.height(AppTheme.spacing.sm))
                if (!isProActive && config.contacts.size >= effectiveMaxContacts) {
                    AppText(
                        text = "Free plan includes 2 favorite contacts.",
                        style = AppTheme.typography.bodySmall,
                        color = AppTheme.colors.textSecondary,
                        modifier = Modifier.padding(horizontal = AppTheme.layout.cardPadding)
                    )
                    Spacer(modifier = Modifier.height(AppTheme.spacing.sm))
                }
                visibleContacts.forEachIndexed { index, contact ->
                        DeviceContactRow(
                            contact = contact,
                            onAdd = { scope.launch { repository.addContact(contact) } }
                        )
                        if (index < visibleContacts.lastIndex) AppRowDivider()
                }
            }
        }

        if (!isProActive) {
            AppButton(
                text = "Unlock unlimited shortcuts",
                glyph = AppleGlyph.Crown,
                style = ai.emots.kishan_dynamic.ui.kit.AppButtonStyle.Secondary,
                onClick = onNavigateToPremium
            )
            (premiumMessage ?: billingStatus)?.let { message ->
                Spacer(modifier = Modifier.height(AppTheme.spacing.sm))
                AppText(
                    text = message,
                    style = AppTheme.typography.caption,
                    color = AppTheme.colors.textSecondary
                )
            }
        }

        AppFootnote("Pinned apps launch directly from the Action Island. Favorite contacts open the dialler. Your selections stay on this device.")
    }

    if (showPaywall) {
        ProPaywallSheet(
            onDismiss = { showPaywall = false },
            onSelectPlan = { planId ->
                premiumMessage = when (val result = premiumGateway.startPurchase(planId)) {
                    is PremiumAccessResult.Unavailable -> result.message
                    is PremiumAccessResult.Started -> result.message
                }
                showPaywall = false
            }
        )
    }
}

@Composable
private fun SearchField(value: String, onValueChange: (String) -> Unit, enabled: Boolean) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        enabled = enabled,
        singleLine = true,
        textStyle = AppTheme.typography.body.copy(color = AppTheme.colors.textPrimary),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(AppTheme.radius.md))
            .background(AppTheme.colors.surfaceVariant)
            .padding(horizontal = AppTheme.spacing.md, vertical = AppTheme.spacing.sm),
        decorationBox = { innerTextField ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                AppleIcon(glyph = AppleGlyph.Search, tint = AppTheme.colors.textSecondary, size = 16.dp)
                Spacer(modifier = Modifier.size(AppTheme.spacing.sm))
                if (value.isEmpty()) {
                    AppText("Search installed apps", style = AppTheme.typography.body, color = AppTheme.colors.textTertiary)
                }
                innerTextField()
            }
        }
    )
}

@Composable
private fun PinnedCustomActionRow(
    action: ActionCustomShortcut,
    order: Int,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onRemove: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = AppTheme.layout.cardPadding, vertical = AppTheme.spacing.sm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(AppTheme.colors.info, AppTheme.colors.accent))),
            contentAlignment = Alignment.Center
        ) {
            AppleIcon(glyph = action.toGlyph(), tint = Color.White, size = 18.dp)
        }
        Spacer(modifier = Modifier.size(AppTheme.spacing.md))
        Column(modifier = Modifier.weight(1f)) {
            AppText(action.label, style = AppTheme.typography.body, fontWeight = FontWeight.SemiBold, color = AppTheme.colors.textPrimary)
            AppText("Custom action $order", style = AppTheme.typography.caption, color = AppTheme.colors.textSecondary)
        }
        ReorderButtons(canMoveUp, canMoveDown, onMoveUp, onMoveDown)
        AppIconButton(glyph = AppleGlyph.Close, onClick = onRemove, tint = AppTheme.colors.textSecondary)
    }
}

@Composable
private fun CustomActionOptionRow(
    preset: ActionCustomPreset,
    enabled: Boolean,
    onAdd: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled, onClick = onAdd)
            .padding(horizontal = AppTheme.layout.cardPadding, vertical = AppTheme.spacing.sm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(AppTheme.colors.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            AppleIcon(
                glyph = preset.toGlyph(),
                tint = if (enabled) AppTheme.colors.accent else AppTheme.colors.textTertiary,
                size = 18.dp
            )
        }
        Spacer(modifier = Modifier.size(AppTheme.spacing.md))
        AppText(
            text = preset.title,
            style = AppTheme.typography.body,
            color = if (enabled) AppTheme.colors.textPrimary else AppTheme.colors.textTertiary,
            modifier = Modifier.weight(1f)
        )
        AppleIcon(
            glyph = AppleGlyph.Expand,
            tint = if (enabled) AppTheme.colors.accent else AppTheme.colors.textTertiary,
            size = 17.dp
        )
    }
}

private fun ActionCustomPreset.toGlyph(): AppleGlyph = when (this) {
    ActionCustomPreset.OPEN_SETTINGS -> AppleGlyph.Settings
    ActionCustomPreset.OPEN_CAMERA -> AppleGlyph.Camera
    ActionCustomPreset.OPEN_NOTIFICATIONS -> AppleGlyph.Bell
    ActionCustomPreset.OPEN_CALENDAR -> AppleGlyph.History
}

private fun ActionCustomShortcut.toGlyph(): AppleGlyph = when (iconKey) {
    "camera" -> AppleGlyph.Camera
    "bell" -> AppleGlyph.Bell
    "calendar" -> AppleGlyph.History
    else -> AppleGlyph.Settings
}

@Composable
private fun PinnedAppRow(
    packageName: String,
    name: String,
    order: Int,
    enabled: Boolean,
    onEnabledChange: (Boolean) -> Unit,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onRemove: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = AppTheme.layout.cardPadding, vertical = AppTheme.spacing.sm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(AppTheme.radius.md))
                .background(AppTheme.colors.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            ai.emots.kishan_dynamic.ui.components.PackageIcon(
                packageName = packageName,
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(AppTheme.radius.md)),
                contentDescription = name
            )
        }
        Spacer(modifier = Modifier.size(AppTheme.spacing.md))
        Column(modifier = Modifier.weight(1f)) {
            AppText(name, style = AppTheme.typography.body, fontWeight = FontWeight.SemiBold, color = AppTheme.colors.textPrimary)
            AppText("Shortcut $order", style = AppTheme.typography.caption, color = AppTheme.colors.textSecondary)
        }
        ReorderButtons(
            canMoveUp = canMoveUp,
            canMoveDown = canMoveDown,
            onMoveUp = onMoveUp,
            onMoveDown = onMoveDown
        )
        Switch(checked = enabled, onCheckedChange = onEnabledChange)
        AppIconButton(glyph = AppleGlyph.Close, onClick = onRemove, tint = AppTheme.colors.textSecondary)
    }
}

@Composable
private fun InstalledAppRow(app: InstalledAppInfo, onAdd: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onAdd)
            .padding(horizontal = AppTheme.layout.cardPadding, vertical = AppTheme.spacing.sm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(AppTheme.radius.md))
                .background(AppTheme.colors.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            ai.emots.kishan_dynamic.ui.components.PackageIcon(
                packageName = app.packageName,
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(AppTheme.radius.md)),
                contentDescription = app.appName
            )
        }
        Spacer(modifier = Modifier.size(AppTheme.spacing.md))
        Column(modifier = Modifier.weight(1f)) {
            AppText(app.appName, style = AppTheme.typography.body, fontWeight = FontWeight.Medium, color = AppTheme.colors.textPrimary)
            AppText(if (app.isSystemApp) "System app" else "Installed app", style = AppTheme.typography.caption, color = AppTheme.colors.textSecondary)
        }
        AppleIcon(glyph = AppleGlyph.Expand, tint = AppTheme.colors.accent, size = 17.dp)
    }
}

@Composable
private fun PinnedContactRow(
    name: String,
    number: String,
    avatarUri: String?,
    order: Int,
    enabled: Boolean,
    onEnabledChange: (Boolean) -> Unit,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onRemove: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = AppTheme.layout.cardPadding, vertical = AppTheme.spacing.sm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ai.emots.kishan_dynamic.ui.components.ContactAvatar(
            name = name,
            avatarUri = avatarUri,
            modifier = Modifier.size(42.dp),
            contentDescription = name
        )
        Spacer(modifier = Modifier.size(AppTheme.spacing.md))
        Column(modifier = Modifier.weight(1f)) {
            AppText(name, style = AppTheme.typography.body, fontWeight = FontWeight.SemiBold, color = AppTheme.colors.textPrimary)
            AppText("$number · Favorite $order", style = AppTheme.typography.caption, color = AppTheme.colors.textSecondary)
        }
        ReorderButtons(
            canMoveUp = canMoveUp,
            canMoveDown = canMoveDown,
            onMoveUp = onMoveUp,
            onMoveDown = onMoveDown
        )
        Switch(checked = enabled, onCheckedChange = onEnabledChange)
        AppIconButton(glyph = AppleGlyph.Close, onClick = onRemove, tint = AppTheme.colors.textSecondary)
    }
}

@Composable
private fun ReorderButtons(
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
        AppText(
            text = "↑",
            style = AppTheme.typography.body,
            color = if (canMoveUp) AppTheme.colors.textPrimary else AppTheme.colors.textTertiary,
            modifier = Modifier
                .clip(CircleShape)
                .clickable(enabled = canMoveUp, onClick = onMoveUp)
                .padding(horizontal = 5.dp, vertical = 2.dp)
        )
        AppText(
            text = "↓",
            style = AppTheme.typography.body,
            color = if (canMoveDown) AppTheme.colors.textPrimary else AppTheme.colors.textTertiary,
            modifier = Modifier
                .clip(CircleShape)
                .clickable(enabled = canMoveDown, onClick = onMoveDown)
                .padding(horizontal = 5.dp, vertical = 2.dp)
        )
    }
}

private fun <T> List<T>.moveItem(from: Int, to: Int): List<T> {
    if (from !in indices || to !in indices) return this
    return toMutableList().also { items ->
        val item = items.removeAt(from)
        items.add(to, item)
    }
}

@Composable
private fun DeviceContactRow(contact: DeviceContactInfo, onAdd: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onAdd)
            .padding(horizontal = AppTheme.layout.cardPadding, vertical = AppTheme.spacing.sm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ai.emots.kishan_dynamic.ui.components.ContactAvatar(
            name = contact.name,
            avatarUri = contact.photoUri,
            modifier = Modifier.size(42.dp),
            contentDescription = contact.name
        )
        Spacer(modifier = Modifier.size(AppTheme.spacing.md))
        Column(modifier = Modifier.weight(1f)) {
            AppText(contact.name, style = AppTheme.typography.body, fontWeight = FontWeight.Medium, color = AppTheme.colors.textPrimary)
            AppText(contact.phoneNumber, style = AppTheme.typography.caption, color = AppTheme.colors.textSecondary)
        }
        AppleIcon(glyph = AppleGlyph.Phone, tint = AppTheme.colors.success, size = 17.dp)
    }
}

@Composable
private fun AppInitial(name: String, accent: Color) {
    Box(
        modifier = Modifier
            .size(42.dp)
            .clip(CircleShape)
            .background(Brush.linearGradient(listOf(accent, AppTheme.colors.info))),
        contentAlignment = Alignment.Center
    ) {
        AppText(
            text = name.take(1).uppercase(),
            style = AppTheme.typography.body.copy(fontWeight = FontWeight.Bold),
            color = Color.White
        )
    }
}

private fun ActionSystemTile.toGlyph(): AppleGlyph = when (this) {
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
