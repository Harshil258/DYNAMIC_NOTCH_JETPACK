package ai.emots.kishan_dynamic.data.model

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SetupCompletionPolicyTest {

    @Test
    fun requiredPermissionsMustAllBeGranted() {
        assertFalse(requiredSetupIsComplete(grantedRequiredCount = 1, requiredCount = 2))
        assertTrue(requiredSetupIsComplete(grantedRequiredCount = 2, requiredCount = 2))
    }

    @Test
    fun optionalPermissionsDoNotChangeTheMilestone() {
        assertTrue(requiredSetupIsComplete(grantedRequiredCount = 2, requiredCount = 2))
    }

    @Test
    fun emptyRequiredSetIsNotASetupCompletionSignal() {
        assertFalse(requiredSetupIsComplete(grantedRequiredCount = 0, requiredCount = 0))
    }
}
