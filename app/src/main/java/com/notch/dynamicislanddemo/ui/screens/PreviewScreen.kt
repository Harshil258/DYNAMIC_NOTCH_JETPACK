package com.notch.dynamicislanddemo.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kyant.backdrop.Backdrop
import com.notch.dynamicislanddemo.models.DummyData
import com.notch.dynamicislanddemo.models.IslandState
import com.notch.dynamicislanddemo.ui.components.DynamicIsland
import com.notch.dynamicislanddemo.ui.glass.GlassCard
import com.notch.dynamicislanddemo.utils.HapticFeedback

data class PreviewItem(
    val emoji: String,
    val title: String,
    val state: IslandState
)

@Composable
fun PreviewScreen(
    backdrop: Backdrop,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isLightTheme = !isSystemInDarkTheme()
    val primaryTextColor = if (isLightTheme) Color.Black else Color.White
    val secondaryTextColor = if (isLightTheme) {
        Color.Black.copy(alpha = 0.7f)
    } else {
        Color.White.copy(alpha = 0.7f)
    }
    
    var currentState by remember { mutableStateOf<IslandState>(IslandState.Minimal) }
    
    val previewItems = remember {
        listOf(
            PreviewItem("⚫", "Minimal", IslandState.Minimal),
            PreviewItem("🎵", "Music", IslandState.CompactMusic(
                track = DummyData.sampleMusicTracks[0],
                isPlaying = true
            )),
            PreviewItem("🎼", "Music Player", IslandState.MusicPlayer(
                track = DummyData.sampleMusicTracks[0],
                isPlaying = true
            )),
            PreviewItem("📞", "Incoming Call", IslandState.IncomingCall(
                caller = DummyData.sampleContacts[0]
            )),
            PreviewItem("☎️", "Ongoing Call", IslandState.OngoingCall(
                duration = "0:08",
                contactName = DummyData.sampleContacts[0].name
            )),
            PreviewItem("🔔", "Notification", IslandState.Notification(
                notifications = DummyData.sampleNotifications
            )),
            PreviewItem("🔋", "Charging", IslandState.Charging(
                percentage = 75,
                isLowBattery = false
            )),
            PreviewItem("⚠️", "Low Battery", IslandState.Charging(
                percentage = 15,
                isLowBattery = true
            )),
            PreviewItem("🔇", "Silent Mode", IslandState.SilentMode(true)),
            PreviewItem("📱", "Multitask", IslandState.Multitask(
                leftActivity = DummyData.musicActivity,
                rightActivity = DummyData.timerActivity
            )),
            PreviewItem("🎛️", "Control", IslandState.ActionControl(
                config = DummyData.sampleActionControl
            )),
            PreviewItem("🎶", "Music + Notif", IslandState.NotificationWithMusic(
                notifications = DummyData.sampleNotifications,
                track = DummyData.sampleMusicTracks[0],
                isPlaying = true
            ))
        )
    }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 60.dp, bottom = 100.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Header
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            BasicText(
                text = "Preview Dynamic Island",
                style = TextStyle(
                    color = primaryTextColor,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
            )
            BasicText(
                text = "Select a state below to preview",
                style = TextStyle(
                    color = secondaryTextColor,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Normal
                )
            )
        }
        
        // Live Preview Area
        GlassCard(
            backdrop = backdrop,
            modifier = Modifier.fillMaxWidth(),
            padding = 32.dp
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                BasicText(
                    text = "Live Preview",
                    style = TextStyle(
                        color = primaryTextColor,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                )
                
                // Dynamic Island Preview with more space
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    contentAlignment = Alignment.TopCenter
                ) {
                    DynamicIsland(
                        state = currentState,
                        onMusicAction = {},
                        onCallAction = {},
                        onSilentModeToggle = {},
                        onNotificationAction = {},
                        onNotificationDismiss = {},
                        onActionControlToggle = {},
                        onActionControlAppLaunch = {},
                        onActionControlContact = {},
                        onActionControlBrightness = {},
                        onActionControlVolume = {}
                    )
                }
                
                // State Name
                GlassCard(
                    backdrop = backdrop,
                    padding = 12.dp,
                    containerColor = if (isLightTheme) {
                        Color(0xFF007AFF).copy(alpha = 0.1f)
                    } else {
                        Color(0xFF0A84FF).copy(alpha = 0.15f)
                    }
                ) {
                    BasicText(
                        text = when (currentState) {
                            is IslandState.Minimal -> "⚫ Minimal State"
                            is IslandState.Hidden -> "🚫 Hidden"
                            is IslandState.CompactMusic -> "🎵 Compact Music"
                            is IslandState.MusicPlayer -> "🎼 Music Player"
                            is IslandState.IncomingCall -> "📞 Incoming Call"
                            is IslandState.OngoingCall -> "☎️ Ongoing Call"
                            is IslandState.Notification -> "🔔 Notification"
                            is IslandState.Charging -> if ((currentState as IslandState.Charging).isLowBattery) "⚠️ Low Battery" else "🔋 Charging"
                            is IslandState.SilentMode -> "🔇 Silent Mode"
                            is IslandState.Multitask -> "📱 Multitask"
                            is IslandState.ActionControl -> "🎛️ Action Control"
                            is IslandState.NotificationWithMusic -> "🎶 Music + Notification"
                        },
                        style = TextStyle(
                            color = primaryTextColor,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
        
        // States Grid Header
        BasicText(
            text = "Island States",
            style = TextStyle(
                color = primaryTextColor,
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold
            ),
            modifier = Modifier.fillMaxWidth()
        )
        
        // State Grid with Glass Cards
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            previewItems.chunked(2).forEach { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    rowItems.forEach { item ->
                        val isSelected = currentState.javaClass == item.state.javaClass
                        
                        GlassCard(
                            backdrop = backdrop,
                            modifier = Modifier
                                .weight(1f)
                                .height(110.dp)
                                .clickable {
                                    HapticFeedback.light(context)
                                    currentState = item.state
                                },
                            padding = 16.dp,
                            containerColor = if (isSelected) {
                                if (isLightTheme) {
                                    Color(0xFF007AFF).copy(alpha = 0.2f)
                                } else {
                                    Color(0xFF0A84FF).copy(alpha = 0.3f)
                                }
                            } else null
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                BasicText(
                                    text = item.emoji,
                                    style = TextStyle(fontSize = 36.sp)
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                BasicText(
                                    text = item.title,
                                    style = TextStyle(
                                        color = if (isSelected) {
                                            if (isLightTheme) Color(0xFF007AFF) else Color(0xFF0A84FF)
                                        } else {
                                            primaryTextColor
                                        },
                                        fontSize = 13.sp,
                                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                                        textAlign = TextAlign.Center
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                    
                    // Add spacer if odd number of items in row
                    if (rowItems.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}
