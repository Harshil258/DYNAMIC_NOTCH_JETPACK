package ai.emots.kishan_dynamic.ui.localization

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AppLocalizationTest {
    @Test
    fun translatesCoreHindiCopy() {
        assertEquals("सेटिंग्स", AppLocalization.translate("hi", "Settings"))
        assertTrue(AppLocalization.hasTranslation("hi", "Settings"))
    }

    @Test
    fun unknownCopyAndUntranslatedLocalesFallBackToSource() {
        assertEquals("Future label", AppLocalization.translate("hi", "Future label"))
        assertEquals("設定", AppLocalization.translate("ja", "Settings"))
        assertTrue(AppLocalization.translatedLocaleCodes.contains("gu"))
    }

    @Test
    fun coversTheReferenceLocaleSet() {
        listOf("ar", "de", "es", "fr", "hi", "ja", "ko", "pt", "zh").forEach { code ->
            assertTrue(code, AppLocalization.translatedLocaleCodes.contains(code))
            assertTrue(code, AppLocalization.hasTranslation(code, "Settings"))
        }
    }
}
