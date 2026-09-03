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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ai.emots.kishan_dynamic.ui.components.AppleGlyph
import ai.emots.kishan_dynamic.ui.components.AppleIcon
import ai.emots.kishan_dynamic.ui.components.AppText
import ai.emots.kishan_dynamic.ui.components.AtmosphericBackground
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

data class AppleNavTabItem(
    val label: String,
    val glyph: AppleGlyph
)

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
            AppleNavTabItem("Island", AppleGlyph.Notch),
            AppleNavTabItem("Experiences", AppleGlyph.Controls),
            AppleNavTabItem("Activity", AppleGlyph.Music),
            AppleNavTabItem("Settings", AppleGlyph.Settings)
        )
    }

    AtmosphericBackground {
        AnimatedContent(
            targetState = currentSubScreen,
            transitionSpec = {
                if (targetState != null) {
                    (slideInHorizontally { it } + fadeIn()) togetherWith (slideOutHorizontally { -it / 3 } + fadeOut())
                } else {
                    (slideInHorizontally { -it / 3 } + fadeIn()) togetherWith (slideOutHorizontally { it } + fadeOut())
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
                SubScreen.PermissionHub -> PermissionScreen(onContinue = { currentSubScreen = null }, onBack = { currentSubScreen = null })
                null -> {
                    Box(modifier = Modifier.fillMaxSize()) {
                        // Main Root Screens
                        when (selectedTab) {
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
                            3 -> SettingsVaultScreen()
                        }

                        // =================================================================
                        // MINIMAL FLOATING NAVIGATION SYSTEM (CALM, SPATIAL, GEMINI-INSPIRED)
                        // =================================================================
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth()
                        ) {
                            // Subtle Dissolve Scrim
                            // Subtle Dissolve Scrim
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(36.dp)
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(
                                                Color.Transparent,
                                                Color(0xCC000000),
                                                Color.Black
                                            )
                                        )
                                    )
                            )

                            // Floating Capsule Nav Dock grounded in Black
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.Black)
                                    .navigationBarsPadding()
                                    .padding(horizontal = 8.dp, vertical = 8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp)
                                        .clip(RoundedCornerShape(999.dp))
                                        .background(Color(0xF0141418))
                                        .border(
                                            0.5.dp,
                                            Color(0x1CFFFFFF),
                                            RoundedCornerShape(999.dp)
                                        )
                                        .padding(horizontal = 2.dp, vertical = 2.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxSize(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        tabs.forEachIndexed { index, tab ->
                                            val isSelected = selectedTab == index
                                            val interactionSource = remember { MutableInteractionSource() }
                                            val isPressed by interactionSource.collectIsPressedAsState()
                                            val tabScale by animateFloatAsState(
                                                targetValue = if (isPressed) 0.94f else 1.0f,
                                                animationSpec = spring(
                                                    dampingRatio = 0.70f,
                                                    stiffness = Spring.StiffnessMedium
                                                ),
                                                label = "tab_scale"
                                            )

                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .fillMaxHeight()
                                                    .scale(tabScale)
                                                    .clip(RoundedCornerShape(999.dp))
                                                    .background(
                                                        if (isSelected) Color(0xFF222228)
                                                        else Color.Transparent
                                                    )
                                                    .clickable(
                                                        interactionSource = interactionSource,
                                                        indication = null
                                                    ) {
                                                        selectedTab = index
                                                    },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                                                    modifier = Modifier.padding(horizontal = 2.dp)
                                                ) {
                                                    AppleIcon(
                                                        glyph = tab.glyph,
                                                        tint = if (isSelected) Color.White else Color(0xFF636366),
                                                        size = 13.dp
                                                    )
                                                    AppText(
                                                        text = tab.label,
                                                        style = AppTheme.typography.caption.copy(
                                                            fontSize = 10.5.sp,
                                                            letterSpacing = (-0.2).sp
                                                        ),
                                                        color = if (isSelected) Color.White else Color(0xFF636366),
                                                        fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                                                        maxLines = 1,
                                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
