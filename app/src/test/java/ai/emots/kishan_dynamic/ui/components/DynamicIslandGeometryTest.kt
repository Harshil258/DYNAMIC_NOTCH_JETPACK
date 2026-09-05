package ai.emots.kishan_dynamic.ui.components

import ai.emots.kishan_dynamic.ui.theme.AppIslandTokens
import ai.emots.kishan_dynamic.data.model.IslandState
import ai.emots.kishan_dynamic.data.model.NotificationInfo
import ai.emots.kishan_dynamic.data.notification.NotificationPriorityPolicy
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DynamicIslandGeometryTest {
    private val tokens = AppIslandTokens()

    @Test
    fun allCollapsedStatesUseTheSharedCompactClassification() {
        val compactStates = IslandDemoState.entries.filter { it.isCompactPresentation() }

        assertEquals(21, compactStates.size)
        assertTrue(IslandDemoState.Idle.isCompactPresentation())
        assertTrue(IslandDemoState.Minimal.isCompactPresentation())
        assertTrue(IslandDemoState.MusicCompact.isCompactPresentation())
        assertTrue(IslandDemoState.NotificationWithMusicCompact.isCompactPresentation())
        assertTrue(IslandDemoState.NotificationStacked.isCompactPresentation())
        assertTrue(IslandDemoState.MusicCompact.usesCompanionBubble())
        assertTrue(IslandDemoState.CallCompact.usesCompanionBubble())
        assertTrue(IslandDemoState.ChargingCompact.usesCompanionBubble())
        assertTrue(IslandDemoState.LowBatteryCompact.usesCompanionBubble())
        assertTrue(IslandDemoState.SilentModeCompact.usesCompanionBubble())
        assertTrue(IslandDemoState.VibrateModeCompact.usesCompanionBubble())
        assertTrue(IslandDemoState.NormalModeCompact.usesCompanionBubble())
        assertTrue(IslandDemoState.NotificationWithMusicCompact.usesCompanionBubble())
        assertTrue(IslandDemoState.NotificationStacked.usesCompanionBubble())
        assertTrue(IslandDemoState.BluetoothConnected.isCompactPresentation())
        assertTrue(IslandDemoState.BluetoothConnecting.isCompactPresentation())
    }

    @Test
    fun everyCollapsedStateResolvesToExactlyOneHeightToken() {
        IslandDemoState.entries
            .filter { it.isCompactPresentation() }
            .forEach { state ->
                assertEquals(tokens.compactHeight, state.resolvedHeight(tokens))
            }
    }

    @Test
    fun timerUsesTheReferenceExportHeight() {
        assertEquals(85.73f, IslandDemoState.TimerExpanded.resolvedHeight(tokens).value, 0.01f)
        assertEquals(85.08f, IslandDemoState.ShortcutExpanded.resolvedHeight(tokens).value, 0.01f)
        assertEquals(86f, IslandDemoState.AirPodsConnected.resolvedHeight(tokens).value, 0.01f)
        assertEquals(86f, IslandDemoState.SatelliteConnected.resolvedHeight(tokens).value, 0.01f)
        assertEquals(86f, IslandDemoState.FindMyAlert.resolvedHeight(tokens).value, 0.01f)
        assertEquals(86f, IslandDemoState.MovedToIPhone.resolvedHeight(tokens).value, 0.01f)
        assertEquals(168f, IslandDemoState.FaceTimeCallExpanded.resolvedHeight(tokens).value, 0.01f)
        assertEquals(172.64f, IslandDemoState.SharedMediaCallExpanded.resolvedHeight(tokens).value, 0.01f)
        assertEquals(177f, IslandDemoState.MusicExpanded.resolvedHeight(tokens).value, 0.01f)
        assertEquals(177f, IslandDemoState.VideoRemoteExpanded.resolvedHeight(tokens).value, 0.01f)
        assertEquals(166f, IslandDemoState.CallExpanded.resolvedHeight(tokens).value, 0.01f)
        assertEquals(148f, IslandDemoState.AirplaneAlert.resolvedHeight(tokens).value, 0.01f)
        assertEquals(144f, IslandDemoState.ScreenMirroringAlert.resolvedHeight(tokens).value, 0.01f)
        assertEquals(162f, IslandDemoState.MobileDataAlert.resolvedHeight(tokens).value, 0.01f)
        assertEquals(142f, IslandDemoState.TransitRouteAlert.resolvedHeight(tokens).value, 0.01f)
        assertEquals(185.3f, IslandDemoState.ColorOptions.resolvedHeight(tokens).value, 0.01f)
        assertEquals(tokens.standardExpandedHeight, IslandDemoState.NotificationExpanded.resolvedHeight(tokens))
        assertEquals(tokens.standardExpandedHeight, IslandDemoState.CallAvatars.resolvedHeight(tokens))
        assertEquals(tokens.standardExpandedHeight, IslandDemoState.RingerModeExpanded.resolvedHeight(tokens))
    }

    @Test
    fun minimalServiceStateUsesTheSplitPresentation() {
        assertEquals(IslandDemoState.Minimal, mapIslandStateToDemoState(IslandState.Minimal))
    }

    @Test
    fun multipleNotificationsUseTheStackedPresentationUntilExpanded() {
        val notifications = listOf(
            NotificationInfo(id = "one", packageName = "com.example.one", appName = "One", title = "First", text = "First"),
            NotificationInfo(id = "two", packageName = "com.example.two", appName = "Two", title = "Second", text = "Second")
        )

        assertEquals(
            IslandDemoState.NotificationStacked,
            mapIslandStateToDemoState(IslandState.Notification(notifications = notifications))
        )
        assertEquals(
            IslandDemoState.NotificationExpanded,
            mapIslandStateToDemoState(IslandState.Notification(notifications = notifications, isExpanded = true))
        )
    }

    @Test
    fun onePriorityNotificationUsesTheReferenceSplitPresentation() {
        val timer = NotificationInfo(
            id = "timer",
            packageName = "com.android.clock",
            appName = "Clock",
            title = "Timer",
            text = "01:24",
            category = "timer"
        )

        assertTrue(NotificationPriorityPolicy.isPriority(timer))
        assertEquals(
            IslandDemoState.NotificationStacked,
            mapIslandStateToDemoState(IslandState.Notification(notifications = listOf(timer)))
        )
    }
}
