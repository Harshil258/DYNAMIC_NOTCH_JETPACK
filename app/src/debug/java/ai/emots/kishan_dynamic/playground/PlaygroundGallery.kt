package ai.emots.kishan_dynamic.playground

import android.content.res.Configuration
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.*
import ai.emots.kishan_dynamic.data.model.*
import ai.emots.kishan_dynamic.ui.components.*
import ai.emots.kishan_dynamic.ui.kit.*
import ai.emots.kishan_dynamic.ui.motion.LocalReducedMotion
import ai.emots.kishan_dynamic.ui.theme.AppTheme

/** Exhaustive catalog; actions have no connection to Android sources or preferences. */
@Composable
fun PlaygroundGallery(initialState: IslandDemoState = IslandDemoState.Idle, onBack: () -> Unit = {}) {
    var selected by remember(initialState) { mutableStateOf(initialState) }
    var width by remember { mutableIntStateOf(393) }
    var large by remember { mutableStateOf(false) }
    var rtl by remember { mutableStateOf(false) }
    var compare by remember { mutableStateOf(true) }
    var measured by remember { mutableStateOf("Waiting for surface measurement") }
    val observer = remember { PlaygroundGeometry() }
    val configuration = Configuration(LocalConfiguration.current).apply { screenWidthDp = width; screenHeightDp = 850 }
    val density = LocalDensity.current
    val target = IslandFamilyGeometry.target(selected)
    AppScreen(scrollable = true, topBar = { AppTopBar(title = "60-state Gallery", subtitle = "Shared production renderer · synthetic demonstration", onBack = onBack) }) {
        AppSegmented(listOf("320", "360", "393", "430"), listOf(320,360,393,430).indexOf(width), { width = listOf(320,360,393,430)[it] })
        AppButton(if (large) "Text 200%" else "Text 100%", { large = !large }, style = AppButtonStyle.Secondary)
        AppButton(if (rtl) "RTL" else "LTR", { rtl = !rtl }, style = AppButtonStyle.Secondary)
        AppButton("Reference targets and deltas: $compare", { compare = !compare }, style = AppButtonStyle.Secondary)
        AppText("${selected.ordinal + 1} / 60 · ${selected.name}", style = AppTheme.typography.h3)
        AppText("Demonstration · no external actions", style = AppTheme.typography.caption)
        Box(Modifier.fillMaxWidth().heightIn(min = 260.dp), contentAlignment = Alignment.TopCenter) {
            // Reference units are laid out at native density; horizontal scrolling preserves 1:1 size.
            androidx.compose.foundation.layout.BoxWithConstraints {
                val fixtureDensity = minOf(density.density, constraints.maxWidth.toFloat() / width)
                CompositionLocalProvider(LocalConfiguration provides configuration,
                    LocalDensity provides Density(fixtureDensity, if (large) 2f else 1f),
                    LocalLayoutDirection provides if (rtl) LayoutDirection.Rtl else LayoutDirection.Ltr,
                    LocalReducedMotion provides true, LocalIslandGeometryObserver provides observer) {
                    DynamicIslandPill(state = selected,
                        batteryPercent = if (selected in setOf(IslandDemoState.LowBatteryCompact, IslandDemoState.LowBatteryExpanded)) 20 else 85,
                        callParticipants = if (selected == IslandDemoState.CallAvatars) listOf(ContactInfo("Alex Morgan", ""), ContactInfo("Jordan Lee", "")) else emptyList(),
                        ringerMode = if (selected == IslandDemoState.RingerModeExpanded) RingerModeType.NORMAL else RingerModeType.SILENT,
                        notifications = listOf(NotificationInfo(id = "gallery-message", packageName = "demo.aurora", appName = "Messages", title = "Project update", text = "The design review is ready. Please review the latest changes.")),
                        musicTrack = MusicTrack("Evening light", "Aurora studio", packageName = "demo.aurora"),
                        callContact = ContactInfo(name = "Alex Morgan", phoneNumber = ""),
                        bluetoothDeviceName = "Studio headphones", bluetoothBatteryPercent = 75,
                        liveActivityTitle = "Sample activity", liveActivitySubtitle = "Demonstration", liveActivityProgress = .65f)
                    LaunchedEffect(observer.regions.values.toList(), selected) {
                        observer.regions.values.firstOrNull { it.first == "Shell" }?.second?.let {
                            val w = it.width / fixtureDensity; val h = it.height / fixtureDensity
                            measured = "Actual %.2f × %.2fdp · Δ %.2f / %.2fdp".format(w,h,w-IslandFamilyGeometry.resolve(selected,width.toFloat()).width,h-target.height)
                        }
                    }
                }
            }
        }
        AppText(measured, style = AppTheme.typography.caption)
        if (compare) Text("Reference ${target.width} × ${target.height} R${target.radius} · ${target.family}", style = AppTheme.typography.caption)
        IslandDemoState.entries.forEach { state ->
            AppButton("${state.ordinal + 1}. ${state.name}", { selected = state }, style = if (selected == state) AppButtonStyle.Tonal else AppButtonStyle.Secondary)
        }
    }
}
