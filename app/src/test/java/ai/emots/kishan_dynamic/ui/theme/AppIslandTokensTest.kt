package ai.emots.kishan_dynamic.ui.theme

import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Test

class AppIslandTokensTest {
    private val tokens = AppIslandTokens()

    @Test
    fun expandedWidth_matchesReferencePhoneMargins() {
        assertEquals(371f, tokens.expandedWidth(393.dp).value, 0.001f)
        assertEquals(408f, tokens.expandedWidth(430.dp).value, 0.001f)
    }

    @Test
    fun expandedWidth_shrinksOnNarrowWindowsWithoutOverflow() {
        assertEquals(298f, tokens.expandedWidth(320.dp).value, 0.001f)
        assertEquals(258f, tokens.expandedWidth(280.dp).value, 0.001f)
    }

    @Test
    fun expandedWidth_isCappedOnTabletsAndFoldables() {
        assertEquals(408f, tokens.expandedWidth(600.dp).value, 0.001f)
        assertEquals(408f, tokens.expandedWidth(840.dp).value, 0.001f)
    }
}
