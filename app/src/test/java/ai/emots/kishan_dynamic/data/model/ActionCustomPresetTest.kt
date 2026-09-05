package ai.emots.kishan_dynamic.data.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ActionCustomPresetTest {

    @Test
    fun presetsHaveUniqueStableIdentifiers() {
        val presets = ActionCustomPreset.entries

        assertEquals(4, presets.size)
        assertEquals(presets.size, presets.map { it.actionId }.toSet().size)
        assertTrue(presets.all { it.title.isNotBlank() && it.iconKey.isNotBlank() })
    }

    @Test
    fun freeAndProCustomActionLimitsStayExplicit() {
        val config = ActionIslandConfig()

        assertEquals(4, config.maxCustomActions)
        assertTrue(config.maxCustomActions > 2)
    }

    @Test
    fun persistedShortcutModelsDefaultToEnabled() {
        assertTrue(ActionAppShortcut("pkg", "App", 0).isEnabled)
        assertTrue(ActionContactShortcut("1", "Contact", "123", order = 0).isEnabled)
    }
}
