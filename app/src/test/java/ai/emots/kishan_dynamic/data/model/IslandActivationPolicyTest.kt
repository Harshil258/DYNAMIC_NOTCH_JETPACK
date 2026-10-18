package ai.emots.kishan_dynamic.data.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class IslandActivationPolicyTest {

    @Test
    fun setupRequired_whenNoPermissionsGranted() {
        val snapshot = IslandActivationPolicy.evaluate(
            overlayGranted = false,
            overlayConnected = false,
            notificationsGranted = false,
            notificationsConnected = false,
            islandEnabled = true
        )

        assertEquals(IslandActivationStatus.SETUP_REQUIRED, snapshot.status)
        assertEquals("Setup required", snapshot.title)
        assertEquals("Set up Dynamic Island", snapshot.primaryActionText)
        assertFalse(snapshot.canToggleDirectly)
        assertFalse(snapshot.isRunning)
    }

    @Test
    fun permissionNeeded_whenOnlyOnePermissionGranted() {
        val snapshot = IslandActivationPolicy.evaluate(
            overlayGranted = true,
            overlayConnected = true,
            notificationsGranted = false,
            notificationsConnected = false,
            islandEnabled = true
        )

        assertEquals(IslandActivationStatus.PERMISSION_NEEDED, snapshot.status)
        assertEquals("Permission needed", snapshot.title)
        assertEquals("Grant required access", snapshot.primaryActionText)
        assertFalse(snapshot.canToggleDirectly)
        assertFalse(snapshot.isRunning)
    }

    @Test
    fun servicePaused_whenPermissionGrantedInSettingsButServiceNotConnectedInRuntime() {
        val snapshot = IslandActivationPolicy.evaluate(
            overlayGranted = true,
            overlayConnected = false, // Android OS killed/paused accessibility service
            notificationsGranted = true,
            notificationsConnected = true,
            islandEnabled = true
        )

        assertEquals(IslandActivationStatus.SERVICE_PAUSED, snapshot.status)
        assertEquals("Island paused", snapshot.title)
        assertEquals("Reactivate Island", snapshot.primaryActionText)
        assertEquals(IslandActivationSnapshot.BadgeType.ERROR, snapshot.badgeType)
        assertFalse(snapshot.canToggleDirectly)
        assertFalse(snapshot.isRunning)
    }

    @Test
    fun readyOff_whenAllPermissionsConnected_butIslandDisabledByUser() {
        val snapshot = IslandActivationPolicy.evaluate(
            overlayGranted = true,
            overlayConnected = true,
            notificationsGranted = true,
            notificationsConnected = true,
            islandEnabled = false
        )

        assertEquals(IslandActivationStatus.READY_OFF, snapshot.status)
        assertEquals("Island is off", snapshot.title)
        assertEquals("Turn on Island", snapshot.primaryActionText)
        assertTrue(snapshot.canToggleDirectly)
        assertFalse(snapshot.isRunning)
    }

    @Test
    fun readyOff_whenIslandDisabledByUser_evenIfServiceNotYetConnected() {
        val snapshot = IslandActivationPolicy.evaluate(
            overlayGranted = true,
            overlayConnected = false,
            notificationsGranted = true,
            notificationsConnected = true,
            islandEnabled = false
        )

        assertEquals(IslandActivationStatus.READY_OFF, snapshot.status)
        assertEquals("Island is off", snapshot.title)
        assertTrue(snapshot.canToggleDirectly)
        assertFalse(snapshot.isRunning)
    }

    @Test
    fun running_whenAllPermissionsConnected_andIslandEnabled() {
        val snapshot = IslandActivationPolicy.evaluate(
            overlayGranted = true,
            overlayConnected = true,
            notificationsGranted = true,
            notificationsConnected = true,
            islandEnabled = true
        )

        assertEquals(IslandActivationStatus.RUNNING, snapshot.status)
        assertEquals("Island is on", snapshot.title)
        assertEquals("Turn off Island", snapshot.primaryActionText)
        assertTrue(snapshot.canToggleDirectly)
        assertTrue(snapshot.isRunning)
        assertEquals(IslandActivationSnapshot.BadgeType.SUCCESS, snapshot.badgeType)
    }

    @Test
    fun starting_whenActivationInProgress() {
        val snapshot = IslandActivationPolicy.evaluate(
            overlayGranted = true,
            overlayConnected = true,
            notificationsGranted = true,
            notificationsConnected = true,
            islandEnabled = true,
            isStarting = true
        )

        assertEquals(IslandActivationStatus.STARTING, snapshot.status)
        assertEquals("Starting…", snapshot.title)
        assertFalse(snapshot.isRunning)
    }
}
