package ai.emots.kishan_dynamic.data.support

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FeedbackPolicyTest {
    @Test
    fun requiresRatingAndWrittenFeedback() {
        assertFalse(FeedbackPolicy.canSubmit(0, "Helpful app"))
        assertFalse(FeedbackPolicy.canSubmit(5, "   "))
        assertTrue(FeedbackPolicy.canSubmit(4, "Helpful app"))
    }

    @Test
    fun trimsLongFeedbackToReferenceLimit() {
        assertTrue(FeedbackPolicy.trimComment("x".repeat(700)).length == 500)
    }
}
