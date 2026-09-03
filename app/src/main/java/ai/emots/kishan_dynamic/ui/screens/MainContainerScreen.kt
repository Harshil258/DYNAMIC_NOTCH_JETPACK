package ai.emots.kishan_dynamic.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ai.emots.kishan_dynamic.ui.components.AppText
import ai.emots.kishan_dynamic.ui.components.AppleGlyph
import ai.emots.kishan_dynamic.ui.components.AppleIcon
import ai.emots.kishan_dynamic.ui.kit.AppBackground
import ai.emots.kishan_dynamic.ui.theme.AppTheme

enum class SubScreen {
    DisplaySettings,
    NotificationSettings,
    MusicSettings,
    CallSettings,
    BatterySettings,
    QuickControl,
    PermissionHub
}

private data class NavTab(
    val label: String,
    val glyph: AppleGlyph
)

/** Height reserved at the bottom of every root screen for the floating dock. */
val BottomDockInset = 92.dp

@Composable
fun MainContainerScreen(
    initialTab: Int = 0
) {
    var selectedTab by remember { mutableIntStateOf(initialTab) }
    var currentSubScreen by remember { mutableStateOf<SubScreen?>(null) }

    BackHandler(enabled = currentSubScreen != null) {
        currentSubScreen = null
    }

    val tabs = remember {
        listOf(
            NavTab("Island", AppleGlyph.Notch),
            NavTab("Features", AppleGlyph.Controls),
            NavTab("Live", AppleGlyph.Music),
            NavTab("Settings", AppleGlyph.Settings)
        )
    }

    AppBackground {
        AnimatedContent(
            targetState = currentSubScreen,
            transitionSpec = {
                if (targetState != null) {
                    (slideInHorizontally { it } + fadeIn()) togetherWith
                        (slideOutHorizontally { -it / 4 } + fadeOut())
                } else {
                    (slideInHorizontally { -it / 4 } + fadeIn()) togetherWith
                        (slideOutHorizontally { it } + fadeOut())
                }
            },
            label = "sub_screen_navigator"
        ) { sub ->
            when (sub) {
                SubScreen.DisplaySettings -> DisplaySettingsScreen(onBack = { currentSubScreen = null })
                SubScreen.NotificationSettings -> NotificationSettingsScreen(onBack = { currentSubScreen = null })
                SubScreen.MusicSettings -> MusicSettingsScreen(onBack = { currentSubScreen = null })
                SubScreen.CallSettings -> CallSettingsScreen(onBack = { currentSubScreen = null })
                SubScreen.BatterySettings -> BatterySettingsScreen(onBack = { currentSubScreen = null })
                SubScreen.QuickControl -> QuickControlScreen(onBack = { currentSubScreen = null })
                SubScreen.PermissionHub -> PermissionScreen(
                    onContinue = { currentSubScreen = null },
                    onBack = { currentSubScreen = null }
                )

                null -> Box(modifier = Modifier.fillMaxSize()) {
                    AnimatedContent(
                        targetState = selectedTab,
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                        label = "root_tab_switch"
                    ) { tab ->
                        when (tab) {
                            0 -> PlaygroundScreen(
                                onNavigateDisplaySettings = { currentSubScreen = SubScreen.DisplaySettings },
                                onNavigateNotificationSettings = { currentSubScreen = SubScreen.NotificationSettings },
                                onNavigateMusicSettings = { currentSubScreen = SubScreen.MusicSettings },
                                onNavigateCallSettings = { currentSubScreen = SubScreen.CallSettings },
                                onNavigateBatterySettings = { currentSubScreen = SubScreen.BatterySettings },
                                onNavigateQuickControl = { currentSubScreen = SubScreen.QuickControl },
                                onNavigatePermissions = { currentSubScreen = SubScreen.PermissionHub },
                                onNavigateVault = { selectedTab = 3 }
                            )

                            1 -> StudioScreen(
                                onNavigateDisplay = { currentSubScreen = SubScreen.DisplaySettings },
                                onNavigateNotifications = { currentSubScreen = SubScreen.NotificationSettings },
                                onNavigateMusic = { currentSubScreen = SubScreen.MusicSettings },
                                onNavigateCalls = { currentSubScreen = SubScreen.CallSettings },
                                onNavigateBattery = { currentSubScreen = SubScreen.BatterySettings },
                                onNavigateQuickControl = { currentSubScreen = SubScreen.QuickControl }
                            )

                            2 -> LiveActivitiesScreen()
                            3 -> SettingsVaultScreen(
                                onNavigatePermissions = { currentSubScreen = SubScreen.PermissionHub }
                            )
                        }
                    }

                    BottomNavigationDock(
                        tabs = tabs,
                        selectedTab = selectedTab,
                        onSelect = { selectedTab = it },
                        modifier = Modifier.align(Alignment.BottomCenter)
                    )
                }
            }
        }
    }
}

@Composable
private fun BottomNavigationDock(
    tabs: List<NavTab>,
    selectedTab: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        // Soft fade so scrolling content dissolves behind the dock.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(28.dp)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.Transparent,
                            AppTheme.colors.background.copy(alpha = 0.85f)
                        )
                    )
                )
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(AppTheme.colors.background)
                .navigationBarsPadding()
                .padding(horizontal = AppTheme.spacing.md, vertical = AppTheme.spacing.sm)
        ) {
            Row(
                modifier = Modifier
                    .widthIn(max = 520.dp)
                    .fillMaxWidth()
                    .align(Alignment.Center)
                    .height(64.dp)
                    .clip(RoundedCornerShape(AppTheme.radius.pill))
                    .background(AppTheme.colors.surface)
                    .border(0.5.dp, AppTheme.colors.border, RoundedCornerShape(AppTheme.radius.pill))
                    .padding(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                tabs.forEachIndexed { index, tab ->
                    val isSelected = selectedTab == index
                    val interaction = remember { MutableInteractionSource() }
                    val pressed by interaction.collectIsPressedAsState()
                    val scale by animateFloatAsState(
                        targetValue = if (pressed) 0.93f else 1f,
                        animationSpec = spring(dampingRatio = 0.7f, stiffness = Spring.StiffnessMedium),
                        label = "tab_scale"
                    )

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .scale(scale)
                            .clip(RoundedCornerShape(AppTheme.radius.pill))
                            .background(
                                if (isSelected) AppTheme.colors.surfaceElevated else Color.Transparent
                            )
                            .clickable(
                                interactionSource = interaction,
                                indication = null
                            ) { onSelect(index) },
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        AppleIcon(
                            glyph = tab.glyph,
                            tint = if (isSelected) AppTheme.colors.textPrimary else AppTheme.colors.textTertiary,
                            size = 19.dp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        AppText(
                            text = tab.label,
                            style = AppTheme.typography.caption,
                            color = if (isSelected) AppTheme.colors.textPrimary else AppTheme.colors.textTertiary,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}
