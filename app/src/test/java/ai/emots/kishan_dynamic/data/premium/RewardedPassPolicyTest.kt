package ai.emots.kishan_dynamic.data.premium

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RewardedPassPolicyTest {
    @Test
    fun createsAnExpiryFromTheConfiguredDuration() {
        assertEquals(90_000_000L, RewardedPassPolicy.expiresAt(3_600_000L, 24))
    }

    @Test
    fun rejectsNonPositiveDurations() {
        assertNull(RewardedPassPolicy.expiresAt(1_000L, 0))
        assertNull(RewardedPassPolicy.expiresAt(1_000L, -1))
    }

    @Test
    fun describesReferenceDefaultAsSevenDays() {
        assertEquals("7 days", RewardedPassPolicy.durationLabel(RewardedPassPolicy.DEFAULT_HOURS))
        assertEquals("12 hours", RewardedPassPolicy.durationLabel(12))
        assertEquals("1 hour", RewardedPassPolicy.durationLabel(1))
    }
}
