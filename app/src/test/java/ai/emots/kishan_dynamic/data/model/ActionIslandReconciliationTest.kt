package ai.emots.kishan_dynamic.data.model

import org.junit.Assert.assertEquals
import org.junit.Test

class ActionIslandReconciliationTest {

    @Test
    fun removesUnavailablePackagesAndNormalizesOrder() {
        val reconciled = reconcilePinnedApps(
            pinnedApps = listOf(
                ActionAppShortcut("removed", "Removed", order = 7),
                ActionAppShortcut("keep-b", "Keep B", order = 2, isEnabled = false),
                ActionAppShortcut("keep-a", "Keep A", order = 9)
            ),
            availablePackages = setOf("keep-a", "keep-b")
        )

        assertEquals(listOf("keep-b", "keep-a"), reconciled.map { it.packageName })
        assertEquals(listOf(0, 1), reconciled.map { it.order })
        assertEquals(false, reconciled.first().isEnabled)
    }

    @Test
    fun emptyPackageSnapshotDoesNotDeleteShortcuts() {
        val pinned = listOf(ActionAppShortcut("keep", "Keep", order = 4))

        assertEquals(pinned, reconcilePinnedApps(pinned, emptySet()))
    }
}
