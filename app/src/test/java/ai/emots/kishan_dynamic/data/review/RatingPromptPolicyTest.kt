package ai.emots.kishan_dynamic.data.review

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RatingPromptPolicyTest {
    @Test
    fun waitsForTheThirdAppOpen() {
        assertFalse(prompt(appOpenCount = 2))
        assertTrue(prompt(appOpenCount = 3))
    }

    @Test
    fun respectsRatedDisabledAndDismissalLimits() {
        assertFalse(prompt(hasRated = true))
        assertFalse(prompt(enabled = false))
        assertFalse(prompt(dismissalCount = RatingPromptPolicy.maximumDismissals))
    }

    @Test
    fun waitsThreeDaysAfterDismissal() {
        val dismissedAt = 1_000L
        assertFalse(prompt(lastDismissedAtMillis = dismissedAt, nowMillis = dismissedAt + 1_000L))
        assertTrue(
            prompt(
                lastDismissedAtMillis = dismissedAt,
                nowMillis = dismissedAt + RatingPromptPolicy.dismissalCooldownMillis
            )
        )
    }

    private fun prompt(
        appOpenCount: Int = 3,
        hasRated: Boolean = false,
        dismissalCount: Int = 0,
        lastDismissedAtMillis: Long = 0L,
        nowMillis: Long = 10_000L,
        enabled: Boolean = true
    ) = RatingPromptPolicy.shouldPrompt(
        appOpenCount = appOpenCount,
        hasRated = hasRated,
        dismissalCount = dismissalCount,
        lastDismissedAtMillis = lastDismissedAtMillis,
        nowMillis = nowMillis,
        enabled = enabled
    )
}
