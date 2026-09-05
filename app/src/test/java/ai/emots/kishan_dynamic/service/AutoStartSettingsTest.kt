package ai.emots.kishan_dynamic.service

import org.junit.Assert.assertEquals
import org.junit.Test

class AutoStartSettingsTest {
    @Test
    fun classifiesCommonOemFamilies() {
        assertEquals(AutoStartOem.XIAOMI, AutoStartSettings.oemFor("Xiaomi", "POCO"))
        assertEquals(AutoStartOem.HUAWEI, AutoStartSettings.oemFor("HUAWEI", "HONOR"))
        assertEquals(AutoStartOem.OPPO, AutoStartSettings.oemFor("OPPO", "realme"))
        assertEquals(AutoStartOem.ONEPLUS, AutoStartSettings.oemFor("OnePlus", "OnePlus"))
        assertEquals(AutoStartOem.OTHER, AutoStartSettings.oemFor("Google", "Pixel"))
    }
}
