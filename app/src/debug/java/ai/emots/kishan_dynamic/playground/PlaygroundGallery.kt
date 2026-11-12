package ai.emots.kishan_dynamic.playground

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import ai.emots.kishan_dynamic.data.model.*
import ai.emots.kishan_dynamic.ui.components.*
import ai.emots.kishan_dynamic.ui.kit.*
import ai.emots.kishan_dynamic.ui.motion.LocalReducedMotion
import ai.emots.kishan_dynamic.ui.theme.AppTheme
import ai.emots.kishan_dynamic.ui.theme.IslandColors
import kotlin.math.abs

/** Categorization for fast, intuitive visual navigation of all 60 states. */
private enum class GalleryCategory(val title: String) {
    ALL("All (60)"),
    CALLS("Calls (7)"),
    NOTIFICATIONS("Alerts & Msgs (5)"),
    MEDIA("Music & Audio (4)"),
    ALERTS("System Alerts (14)"),
    ACTIVITIES("Activities (12)"),
    DEVICE("Device & Battery (12)"),
    CARDS("Controls & Cards (6)")
}

private enum class GalleryViewMode(val title: String) {
    LIVE("Live"),
    FIGMA("Figma Ref"),
    SIDE_BY_SIDE("Side-by-Side")
}

private fun statesForCategory(category: GalleryCategory): List<IslandDemoState> = when (category) {
    GalleryCategory.ALL -> IslandDemoState.entries
    GalleryCategory.CALLS -> listOf(
        IslandDemoState.OngoingCallBanner,
        IslandDemoState.CallCompact,
        IslandDemoState.CallExpanded,
        IslandDemoState.FaceTimeCallExpanded,
        IslandDemoState.SharedMediaCallExpanded,
        IslandDemoState.CallSummaryExpanded,
        IslandDemoState.CallAvatars
    )
    GalleryCategory.NOTIFICATIONS -> listOf(
        IslandDemoState.NotificationCompact,
        IslandDemoState.NotificationStacked,
        IslandDemoState.NotificationWithMusicCompact,
        IslandDemoState.NotificationExpanded,
        IslandDemoState.NotificationImage
    )
    GalleryCategory.MEDIA -> listOf(
        IslandDemoState.MusicCompact,
        IslandDemoState.MusicExpanded,
        IslandDemoState.VideoRemoteExpanded,
        IslandDemoState.VoiceMemoExpanded
    )
    GalleryCategory.ALERTS -> listOf(
        IslandDemoState.AirPodsConnected,
        IslandDemoState.SatelliteConnected,
        IslandDemoState.FindMyAlert,
        IslandDemoState.SilentModeCompact,
        IslandDemoState.SilentModeExpanded,
        IslandDemoState.RingerModeExpanded,
        IslandDemoState.MovedToIPhone,
        IslandDemoState.AirDropActivity,
        IslandDemoState.AirplaneAlert,
        IslandDemoState.ScreenMirroringAlert,
        IslandDemoState.MobileDataAlert,
        IslandDemoState.TransitRouteAlert,
        IslandDemoState.FocusModeExpanded,
        IslandDemoState.ScreenRecordingExpanded
    )
    GalleryCategory.ACTIVITIES -> listOf(
        IslandDemoState.TimerCompact,
        IslandDemoState.TimerExpanded,
        IslandDemoState.TimerImage,
        IslandDemoState.DeliveryCompact,
        IslandDemoState.DeliveryExpanded,
        IslandDemoState.FlightCompact,
        IslandDemoState.FlightExpanded,
        IslandDemoState.FlightTrackerExpanded,
        IslandDemoState.SportsCompact,
        IslandDemoState.SportsExpanded,
        IslandDemoState.NavigationCompact,
        IslandDemoState.TransportActivity
    )
    GalleryCategory.DEVICE -> listOf(
        IslandDemoState.Idle,
        IslandDemoState.Minimal,
        IslandDemoState.ChargingCompact,
        IslandDemoState.ChargingExpanded,
        IslandDemoState.LowBatteryCompact,
        IslandDemoState.LowBatteryExpanded,
        IslandDemoState.VibrateModeCompact,
        IslandDemoState.NormalModeCompact,
        IslandDemoState.RingerVolumeCompact,
        IslandDemoState.MediaVolumeCompact,
        IslandDemoState.BluetoothConnected,
        IslandDemoState.BluetoothConnecting
    )
    GalleryCategory.CARDS -> listOf(
        IslandDemoState.ActionControlExpanded,
        IslandDemoState.ShortcutExpanded,
        IslandDemoState.PremiumExpiryExpanded,
        IslandDemoState.SubscriptionPricing,
        IslandDemoState.MovieCard,
        IslandDemoState.ColorOptions
    )
}

private fun getOppositeState(state: IslandDemoState): IslandDemoState? = when (state) {
    IslandDemoState.CallCompact -> IslandDemoState.CallExpanded
    IslandDemoState.CallExpanded -> IslandDemoState.CallCompact
    IslandDemoState.MusicCompact -> IslandDemoState.MusicExpanded
    IslandDemoState.MusicExpanded -> IslandDemoState.MusicCompact
    IslandDemoState.NotificationCompact -> IslandDemoState.NotificationExpanded
    IslandDemoState.NotificationExpanded -> IslandDemoState.NotificationCompact
    IslandDemoState.TimerCompact -> IslandDemoState.TimerExpanded
    IslandDemoState.TimerExpanded -> IslandDemoState.TimerCompact
    IslandDemoState.ChargingCompact -> IslandDemoState.ChargingExpanded
    IslandDemoState.ChargingExpanded -> IslandDemoState.ChargingCompact
    IslandDemoState.LowBatteryCompact -> IslandDemoState.LowBatteryExpanded
    IslandDemoState.LowBatteryExpanded -> IslandDemoState.LowBatteryCompact
    IslandDemoState.DeliveryCompact -> IslandDemoState.DeliveryExpanded
    IslandDemoState.DeliveryExpanded -> IslandDemoState.DeliveryCompact
    IslandDemoState.FlightCompact -> IslandDemoState.FlightExpanded
    IslandDemoState.FlightExpanded -> IslandDemoState.FlightCompact
    IslandDemoState.SportsCompact -> IslandDemoState.SportsExpanded
    IslandDemoState.SportsExpanded -> IslandDemoState.SportsCompact
    IslandDemoState.SilentModeCompact -> IslandDemoState.SilentModeExpanded
    IslandDemoState.SilentModeExpanded -> IslandDemoState.SilentModeCompact
    IslandDemoState.BluetoothConnecting -> IslandDemoState.BluetoothConnected
    IslandDemoState.BluetoothConnected -> IslandDemoState.BluetoothConnecting
    else -> null
}

/** Comprehensive, ergonomic 60-state visual testing studio. */
@Composable
fun PlaygroundGallery(initialState: IslandDemoState = IslandDemoState.Idle, onBack: () -> Unit = {}) {
    var selected by remember(initialState) { mutableStateOf(initialState) }
    var width by remember { mutableIntStateOf(393) }
    var large by remember { mutableStateOf(false) }
    var rtl by remember { mutableStateOf(false) }
    var showMetrics by remember { mutableStateOf(true) }
    var viewMode by remember { mutableStateOf(GalleryViewMode.LIVE) }
    var groupCallerFixture by remember { mutableStateOf(false) }
    var carrierControlsFixture by remember { mutableStateOf(false) }
    var navigationSourceMode by remember { mutableIntStateOf(0) }
    var selectedCategoryIndex by remember { mutableIntStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }

    val gridState = rememberLazyGridState()

    var measuredActualW by remember { mutableFloatStateOf(0f) }
    var measuredActualH by remember { mutableFloatStateOf(0f) }
    var deltaW by remember { mutableFloatStateOf(0f) }
    var deltaH by remember { mutableFloatStateOf(0f) }

    val observer = remember { PlaygroundGeometry() }
    val context = LocalContext.current
    val referenceMessages = remember(context) { FigmaReferenceFixtures.messages(context) }
    val configuration = Configuration(LocalConfiguration.current).apply { screenWidthDp = width; screenHeightDp = 850 }
    val density = LocalDensity.current
    val target = IslandFamilyGeometry.target(selected)

    val currentCategoryStates = remember(selectedCategoryIndex) {
        statesForCategory(GalleryCategory.entries[selectedCategoryIndex])
    }

    val filteredStates = remember(currentCategoryStates, searchQuery) {
        if (searchQuery.isBlank()) currentCategoryStates
        else currentCategoryStates.filter {
            it.name.contains(searchQuery.trim(), ignoreCase = true) ||
            "${it.ordinal + 1}".contains(searchQuery.trim())
        }
    }

    val allStates = IslandDemoState.entries
    val activeList = if (filteredStates.isNotEmpty()) filteredStates else allStates
    val activePos = activeList.indexOf(selected).let { if (it >= 0) it else 0 }
    val prevInList = activeList[(activePos - 1 + activeList.size) % activeList.size]
    val nextInList = activeList[(activePos + 1) % activeList.size]
    val oppositeState = getOppositeState(selected)

    LaunchedEffect(selected, filteredStates) {
        val idx = filteredStates.indexOf(selected)
        if (idx >= 0) {
            gridState.animateScrollToItem(idx)
        }
    }

    AppScreen(
        scrollable = false,
        topBar = {
            AppTopBar(
                title = "60-state Gallery",
                subtitle = "Shared production renderer · synthetic demonstration",
                onBack = onBack
            )
        },
        bottomAction = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = AppTheme.spacing.md, vertical = AppTheme.spacing.xs),
                horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AppButton(
                    text = "◀  #${prevInList.ordinal + 1} ${prevInList.name.take(12)}",
                    onClick = { selected = prevInList },
                    modifier = Modifier.weight(1f),
                    style = AppButtonStyle.Secondary
                )
                AppButton(
                    text = "#${nextInList.ordinal + 1} ${nextInList.name.take(12)}  ▶",
                    onClick = { selected = nextInList },
                    modifier = Modifier.weight(1f),
                    style = AppButtonStyle.Primary
                )
            }
        }
    ) {
        Column(Modifier.fillMaxSize()) {
            // 1. PINNED PREVIEW STAGE (always visible above the state list)
            AppCard(
                modifier = Modifier.fillMaxWidth(),
                padding = AppTheme.spacing.sm
            ) {
                // Stepper Header: Previous - Title - Next
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    AppIconButton(
                        glyph = AppleGlyph.ChevronLeft,
                        onClick = { selected = prevInList },
                        contentDescription = "Previous state"
                    )

                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "${selected.ordinal + 1} / 60 · ${selected.name}",
                            style = AppTheme.typography.islandTitle,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${target.width.toInt()}×${target.height.toInt()}dp (r=${target.radius.toInt()}dp) · ${target.family.name}",
                            style = AppTheme.typography.caption,
                            color = IslandColors.CapsuleCyan,
                            fontSize = 11.sp
                        )
                    }

                    AppIconButton(
                        glyph = AppleGlyph.ChevronRight,
                        onClick = { selected = nextInList },
                        contentDescription = "Next state"
                    )
                }

                Spacer(modifier = Modifier.height(AppTheme.spacing.xs))

                AppSegmented(
                    options = listOf("Live Island", "Figma Ref", "Side-by-Side"),
                    selectedIndex = viewMode.ordinal,
                    onOptionSelected = { viewMode = GalleryViewMode.entries[it] },
                    modifier = Modifier.fillMaxWidth().height(30.dp)
                )

                Spacer(modifier = Modifier.height(AppTheme.spacing.xs))

                // The Black OLED Canvas with Notch Clearance Indicator
                val stageMinHeight = when (viewMode) {
                    GalleryViewMode.SIDE_BY_SIDE -> (target.height * 2 + 56).dp
                    else -> if (selected.isCompactPresentation()) 72.dp else minOf((target.height + 28).dp, 280.dp)
                }
                var swipeOffset by remember { mutableFloatStateOf(0f) }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = stageMinHeight)
                        .wrapContentHeight()
                        .clip(RoundedCornerShape(16.dp))
                        .background(AppTheme.colors.background)
                        .border(0.75.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(16.dp))
                        .pointerInput(selected, activeList) {
                            detectHorizontalDragGestures(
                                onDragEnd = {
                                    if (swipeOffset < -40f) selected = nextInList
                                    else if (swipeOffset > 40f) selected = prevInList
                                    swipeOffset = 0f
                                },
                                onHorizontalDrag = { _, dragAmount ->
                                    swipeOffset += dragAmount
                                }
                            )
                        },
                    contentAlignment = Alignment.TopCenter
                ) {
                    // Small simulated camera cutout dot
                    Box(
                        Modifier
                            .padding(top = 9.dp)
                            .size(11.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF15181F))
                            .border(0.5.dp, Color.White.copy(alpha = 0.15f), CircleShape)
                    )

                    val renderLiveIsland = @Composable {
                        androidx.compose.foundation.layout.BoxWithConstraints {
                            val fixtureDensity = minOf(density.density, constraints.maxWidth.toFloat() / width)
                            CompositionLocalProvider(
                                LocalConfiguration provides configuration,
                                LocalDensity provides Density(fixtureDensity, if (large) 2f else 1f),
                                LocalLayoutDirection provides if (rtl) LayoutDirection.Rtl else LayoutDirection.Ltr,
                                LocalReducedMotion provides true,
                                LocalIslandRenderSettings provides IslandRenderSettings(
                                    maximumHeight = (constraints.maxHeight / fixtureDensity).dp
                                ),
                                LocalIslandGeometryObserver provides observer,
                                LocalIslandFixtureIcons provides mapOf(FigmaReferenceFixtures.packageName to ai.emots.kishan_dynamic.R.drawable.figma_reference_app)
                            ) {
                                DynamicIslandPill(
                                    state = selected,
                                    batteryPercent = if (selected in setOf(IslandDemoState.LowBatteryCompact, IslandDemoState.LowBatteryExpanded)) 20 else 85,
                                    callParticipants = if (selected == IslandDemoState.CallAvatars && groupCallerFixture) listOf(ContactInfo("Alex Morgan", ""), ContactInfo("Jordan Lee", "")) else emptyList(),
                                    ringerMode = if (selected == IslandDemoState.RingerModeExpanded) RingerModeType.NORMAL else RingerModeType.SILENT,
                                    notifications = if (selected == IslandDemoState.NotificationExpanded) referenceMessages.map { it.copy(picturePath = null) } else referenceMessages,
                                    notificationActiveIndex = 1,
                                    unseenNotificationCount = 3,
                                    musicTrack = FigmaReferenceFixtures.music(context),
                                    callContact = FigmaReferenceFixtures.caller(context),
                                    callSummary = if (selected == IslandDemoState.CallSummaryExpanded) CallRecord("gallery_summary", "Tamia Castillo", "+1 (555) 019-2834", startedAtMillis = System.currentTimeMillis() - 165_000L, durationSeconds = 165L, direction = CallDirection.INCOMING) else null,
                                    liveActivityInfo = if (selected == IslandDemoState.ColorOptions && navigationSourceMode > 0)
                                        ai.emots.kishan_dynamic.data.model.LiveActivityInfo(
                                            id = "gallery-navigation", kind = ai.emots.kishan_dynamic.data.model.LiveActivityKind.NAVIGATION,
                                            title = "Navigation", primaryMetric = if (navigationSourceMode == 1) "500 m" else "1.2 km",
                                            secondaryMetric = if (navigationSourceMode == 1) "University Avenue" else "University Avenue toward the Riverside science museum",
                                            secondaryText = if (navigationSourceMode == 1) "Turn left at the next intersection" else "Continue past the town library and follow the signs toward the west entrance",
                                            maneuverType = "left", statusBadgeText = "NEXT TURN"
                                        ) else null,
                                    availableControlActions = if (carrierControlsFixture && selected == IslandDemoState.CallExpanded)
                                        setOf(IslandControlAction.CallToggleSpeaker, IslandControlAction.CallToggleMute, IslandControlAction.CallEnd) else null,
                                    bluetoothDeviceName = "Studio headphones",
                                    bluetoothBatteryPercent = 75,
                                    liveActivityProgress = if (selected == IslandDemoState.AirPodsConnected) .75f else null
                                )
                                LaunchedEffect(observer.regions.values.toList(), selected) {
                                    observer.regions.values.firstOrNull { it.first == "Shell" }?.second?.let {
                                        val w = it.width / fixtureDensity
                                        val h = it.height / fixtureDensity
                                        measuredActualW = w
                                        measuredActualH = h
                                        deltaW = w - IslandFamilyGeometry.resolve(selected, width.toFloat()).width
                                        deltaH = h - target.height
                                    }
                                }
                            }
                        }
                    }

                    val renderFigmaReference = @Composable {
                        val figmaBitmap = remember(selected) { FigmaReferenceFixtures.loadFigmaBitmap(context, selected) }
                        if (figmaBitmap != null) {
                            Image(
                                bitmap = figmaBitmap.asImageBitmap(),
                                contentDescription = "Figma Reference for ${selected.name}",
                                modifier = Modifier
                                    .padding(vertical = 12.dp)
                                    .size(target.width.dp, target.height.dp)
                                    .clip(RoundedCornerShape(target.radius.dp))
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .padding(vertical = 24.dp)
                                    .size(target.width.dp, target.height.dp)
                                    .clip(RoundedCornerShape(target.radius.dp))
                                    .background(Color(0xFF10141D)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Figma reference not found for ${selected.name}", fontSize = 11.sp, color = Color.Gray)
                            }
                        }
                    }

                    when (viewMode) {
                        GalleryViewMode.LIVE -> renderLiveIsland()
                        GalleryViewMode.FIGMA -> renderFigmaReference()
                        GalleryViewMode.SIDE_BY_SIDE -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    "LIVE ANDROID PILL",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AppTheme.colors.primary
                                )
                                renderLiveIsland()

                                Spacer(Modifier.height(4.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp)
                                ) {
                                    Box(Modifier.weight(1f).height(0.5.dp).background(Color.White.copy(alpha = 0.2f)))
                                    val figmaId = FigmaReferenceFixtures.figmaNodeId(selected) ?: "Reference"
                                    Text(
                                        "  FIGMA $figmaId · ${target.width.toInt()}×${target.height.toInt()}dp  ",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = IslandColors.CapsuleCyan
                                    )
                                    Box(Modifier.weight(1f).height(0.5.dp).background(Color.White.copy(alpha = 0.2f)))
                                }
                                Spacer(Modifier.height(4.dp))

                                renderFigmaReference()
                            }
                        }
                    }
                }

                // Measurement Pill & Quick Expand/Collapse
                if (showMetrics) {
                    Spacer(modifier = Modifier.height(AppTheme.spacing.xs))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val isAccurate = abs(deltaW) < 0.6f && abs(deltaH) < 0.6f
                        val deltaColor = if (isAccurate) Color(0xFF34C759) else if (abs(deltaW) < 2f && abs(deltaH) < 2f) Color(0xFFFF9500) else Color(0xFFFF3B30)

                        Text(
                            text = if (measuredActualW > 0f) "Actual: %.1f × %.1fdp · Δ %.1f / %.1fdp".format(measuredActualW, measuredActualH, deltaW, deltaH) else "Measuring surface...",
                            fontSize = 11.sp,
                            color = deltaColor,
                            fontWeight = FontWeight.Medium
                        )

                        if (oppositeState != null) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF2C2C2E))
                                    .clickable { selected = oppositeState }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = if (selected.isCompactPresentation()) "Expand ↗" else "Collapse ↙",
                                    fontSize = 11.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(AppTheme.spacing.xs))

            // 2. SIMULATION CONTROLS TOOLBAR (Compact & space-efficient)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs)
            ) {
                AppSegmented(
                    options = listOf("320", "360", "393", "430"),
                    selectedIndex = listOf(320, 360, 393, 430).indexOf(width).coerceAtLeast(0),
                    onOptionSelected = { width = listOf(320, 360, 393, 430)[it] },
                    modifier = Modifier.weight(1.3f).height(AppTheme.island.minimumActionTarget * density.fontScale.coerceAtLeast(1f))
                )

                AppChip(
                    label = if (large) "200%" else "100%",
                    selected = large,
                    onClick = { large = !large }
                )

                AppChip(
                    label = if (rtl) "RTL" else "LTR",
                    selected = rtl,
                    onClick = { rtl = !rtl }
                )

                AppIconButton(
                    glyph = AppleGlyph.Search,
                    tint = if (isSearchActive || searchQuery.isNotBlank()) AppTheme.colors.primary else AppTheme.colors.textSecondary,
                    onClick = { isSearchActive = !isSearchActive }
                )
            }

            if (selected == IslandDemoState.CallAvatars) AppChip(
                label = if (groupCallerFixture) "Group callers" else "Single incoming caller",
                selected = groupCallerFixture, onClick = { groupCallerFixture = !groupCallerFixture }
            )
            if (selected == IslandDemoState.CallExpanded) AppChip(
                label = if (carrierControlsFixture) "Supported audio controls" else "Five-control demonstration",
                selected = carrierControlsFixture, onClick = { carrierControlsFixture = !carrierControlsFixture }
            )
            if (selected == IslandDemoState.ColorOptions) AppChip(
                label = listOf("Figma lane guide", "Source navigation", "Long source navigation")[navigationSourceMode],
                selected = navigationSourceMode > 0, onClick = { navigationSourceMode = (navigationSourceMode + 1) % 3 }
            )

            // 3. OPTIONAL SEARCH BAR
            AnimatedVisibility(visible = isSearchActive || searchQuery.isNotBlank()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = AppTheme.spacing.xxs)
                        .clip(RoundedCornerShape(12.dp))
                        .background(AppTheme.colors.surfaceVariant)
                        .border(0.75.dp, AppTheme.colors.border, RoundedCornerShape(12.dp))
                        .padding(horizontal = AppTheme.spacing.sm, vertical = AppTheme.spacing.xs),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AppleIcon(
                        glyph = AppleGlyph.Search,
                        tint = AppTheme.colors.textSecondary,
                        size = 15.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        singleLine = true,
                        textStyle = AppTheme.typography.body.copy(color = Color.White),
                        cursorBrush = SolidColor(AppTheme.colors.primary),
                        modifier = Modifier.weight(1f),
                        decorationBox = { innerTextField ->
                            if (searchQuery.isEmpty()) {
                                Text(
                                    text = "Search 60 states by name or #...",
                                    style = AppTheme.typography.body,
                                    color = AppTheme.colors.textSecondary
                                )
                            }
                            innerTextField()
                        }
                    )
                    if (searchQuery.isNotEmpty()) {
                        AppIconButton(
                            glyph = AppleGlyph.Close,
                            onClick = { searchQuery = "" },
                            iconSize = 13.dp
                        )
                    }
                }
            }

            // 4. CATEGORY FILTER TABS
            AppScrollableTabs(
                options = GalleryCategory.entries.map { it.title },
                selectedIndex = selectedCategoryIndex,
                onOptionSelected = { idx ->
                    selectedCategoryIndex = idx
                    val catStates = statesForCategory(GalleryCategory.entries[idx])
                    if (selected !in catStates && catStates.isNotEmpty()) {
                        selected = catStates.first()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = AppTheme.spacing.xxs)
            )

            // 5. STATES SELECTION 2-COLUMN GRID
            LazyVerticalGrid(
                state = gridState,
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(vertical = AppTheme.spacing.xs),
                horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs),
                verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs)
            ) {
                items(filteredStates, key = { it.name }) { state ->
                    val isCurrent = state == selected
                    val stateTarget = IslandFamilyGeometry.target(state)
                    val bgColor = if (isCurrent) AppTheme.colors.primary.copy(alpha = 0.20f) else AppTheme.colors.surfaceVariant
                    val borderColor = if (isCurrent) AppTheme.colors.primary else AppTheme.colors.border.copy(alpha = 0.4f)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 52.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(bgColor)
                            .border(if (isCurrent) 1.5.dp else 0.75.dp, borderColor, RoundedCornerShape(12.dp))
                            .clickable { selected = state }
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(if (isCurrent) AppTheme.colors.primary else Color(0xFF1E2430)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${state.ordinal + 1}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isCurrent) Color.Black else Color.White
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = state.name,
                                style = AppTheme.typography.caption,
                                color = if (isCurrent) Color.White else AppTheme.colors.textPrimary,
                                fontWeight = if (isCurrent) FontWeight.SemiBold else FontWeight.Normal,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${stateTarget.width.toInt()}×${stateTarget.height.toInt()} · ${stateTarget.family.name.take(9)}",
                                fontSize = 10.sp,
                                color = if (isCurrent) IslandColors.CapsuleCyan else AppTheme.colors.textSecondary,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}
