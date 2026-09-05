package ai.emots.kishan_dynamic.data.premium

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PremiumExpiryPolicyTest {
    private val hour = 60L * 60L * 1000L

    @Test
    fun onlyPassesWithinTheTwentyFourHourWarningWindow() {
        assertNull(PremiumExpiryPolicy.remainingWarningHours(25L * hour, 0L))
        assertEquals(24, PremiumExpiryPolicy.remainingWarningHours(24L * hour, 0L))
        assertEquals(1, PremiumExpiryPolicy.remainingWarningHours(hour + 30L, 0L))
    }

    @Test
    fun expiredEntitlementsDoNotReappearAsAWarning() {
        assertNull(PremiumExpiryPolicy.remainingWarningHours(0L, 1L))
        assertNull(PremiumExpiryPolicy.remainingWarningHours(-hour, 0L))
    }
}
