package ai.emots.kishan_dynamic.ui.components

import ai.emots.kishan_dynamic.data.model.CallDirection
import ai.emots.kishan_dynamic.data.model.CallRecord
import ai.emots.kishan_dynamic.data.model.ContactInfo
import ai.emots.kishan_dynamic.data.model.DurationAccuracy
import ai.emots.kishan_dynamic.data.model.IslandState
import ai.emots.kishan_dynamic.data.model.LiveActivityInfo
import ai.emots.kishan_dynamic.data.model.LiveActivityKind
import ai.emots.kishan_dynamic.data.model.LockScreenPrivacyPolicy
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AuroraV6CParityAndAdaptiveTest {

    @Test
    fun minimalAndIdleMappersAreReconciled() {
        assertEquals(IslandDemoState.Minimal, resolveOverlayDemoState(IslandState.Minimal, 0))
        assertEquals(IslandDemoState.Minimal, mapIslandStateToDemoState(IslandState.Minimal))
        assertEquals(IslandDemoState.Idle, resolveOverlayDemoState(IslandState.Hidden, 0))
        assertEquals(IslandDemoState.Idle, mapIslandStateToDemoState(IslandState.Hidden))
    }

    @Test
    fun callSummaryLockScreenPrivacyRedaction() {
        val record = CallRecord(
            id = "test-call",
            contactName = "Secret Contact",
            phoneNumber = "+1234567890",
            avatarUri = "/avatars/secret.png",
            startedAtMillis = 1000L,
            durationSeconds = 120L,
            direction = CallDirection.INCOMING,
            durationAccuracy = DurationAccuracy.RELIABLE_MONOTONIC
        )

        // When locked and hiding sensitive content: caller info MUST be redacted
        val redacted = LockScreenPrivacyPolicy.callRecord(record, deviceLocked = true, hideSensitiveContent = true)
        assertEquals("Incoming call", redacted?.contactName)
        assertEquals("", redacted?.phoneNumber)
        assertNull(redacted?.avatarUri)
        assertEquals(120L, redacted?.durationSeconds)

        // Outgoing call redacted
        val outgoing = record.copy(direction = CallDirection.OUTGOING)
        assertEquals("Outgoing call", LockScreenPrivacyPolicy.callRecord(outgoing, deviceLocked = true, hideSensitiveContent = true)?.contactName)

        // Missed call redacted
        val missed = record.copy(direction = CallDirection.MISSED)
        assertEquals("Missed call", LockScreenPrivacyPolicy.callRecord(missed, deviceLocked = true, hideSensitiveContent = true)?.contactName)

        // When unlocked: preserved
        assertEquals(record, LockScreenPrivacyPolicy.callRecord(record, deviceLocked = false, hideSensitiveContent = true))
    }

    @Test
    fun callSummaryDurationAndPhaseLabels() {
        val record = CallRecord(
            id = "test-call",
            contactName = "Alice",
            phoneNumber = "+123",
            startedAtMillis = 1000L,
            durationSeconds = 125L,
            direction = CallDirection.INCOMING,
            durationAccuracy = DurationAccuracy.RELIABLE_MONOTONIC
        )
        assertEquals("02:05", callSummaryDurationLabel(record))
        assertEquals("Call ended · Incoming", callSummaryPhaseLabel(record))

        val inferred = record.copy(durationAccuracy = DurationAccuracy.INFERRED_WALL_CLOCK)
        assertEquals("≈02:05", callSummaryDurationLabel(inferred))

        val unknown = record.copy(durationAccuracy = DurationAccuracy.UNKNOWN)
        assertEquals("Duration unavailable", callSummaryDurationLabel(unknown))

        val missed = record.copy(direction = CallDirection.MISSED)
        assertEquals("Missed", callSummaryDurationLabel(missed))
        assertEquals("Missed call", callSummaryPhaseLabel(missed))
    }

    @Test
    fun all60DemoStatesHaveTargetGeometryInFamilyResolver() {
        assertEquals(60, IslandDemoState.entries.size)
        assertEquals(60, IslandFamilyGeometry.targets.size)
        for (state in IslandDemoState.entries) {
            assertTrue("State ${state.name} must exist in targets", IslandFamilyGeometry.targets.containsKey(state))
            val target = IslandFamilyGeometry.target(state)
            assertTrue("Target width must be > 0 for ${state.name}", target.width > 0f)
            assertTrue("Target height must be > 0 for ${state.name}", target.height > 0f)
            assertTrue("Target radius must be > 0 for ${state.name}", target.radius > 0f)
        }
    }

    @Test
    fun navigationManeuverNeutralStraightFallback() {
        fun resolveManeuver(info: LiveActivityInfo?): AppleGlyph = when (info?.maneuverType?.lowercase()) {
            "left", "sharp_left" -> AppleGlyph.NavigationLeft
            "right", "sharp_right" -> AppleGlyph.NavigationRight
            "straight" -> AppleGlyph.NavigationStraight
            else -> if (info == null) AppleGlyph.NavigationRight else AppleGlyph.NavigationStraight
        }

        // Live info with null maneuver MUST NOT turn right
        val liveInfoNullManeuver = LiveActivityInfo("nav", LiveActivityKind.NAVIGATION, "Nav", maneuverType = null)
        assertEquals(AppleGlyph.NavigationStraight, resolveManeuver(liveInfoNullManeuver))

        // Live info with unknown maneuver MUST NOT turn right
        val liveInfoUnknown = LiveActivityInfo("nav", LiveActivityKind.NAVIGATION, "Nav", maneuverType = "u_turn")
        assertEquals(AppleGlyph.NavigationStraight, resolveManeuver(liveInfoUnknown))

        // Live info with explicit left
        val liveInfoLeft = LiveActivityInfo("nav", LiveActivityKind.NAVIGATION, "Nav", maneuverType = "left")
        assertEquals(AppleGlyph.NavigationLeft, resolveManeuver(liveInfoLeft))

        // Live info with explicit right
        val liveInfoRight = LiveActivityInfo("nav", LiveActivityKind.NAVIGATION, "Nav", maneuverType = "right")
        assertEquals(AppleGlyph.NavigationRight, resolveManeuver(liveInfoRight))

        // Demo preview (info == null) keeps Figma fixture right turn
        assertEquals(AppleGlyph.NavigationRight, resolveManeuver(null))
    }
}
