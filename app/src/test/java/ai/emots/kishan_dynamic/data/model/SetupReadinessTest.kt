package ai.emots.kishan_dynamic.data.model

import org.junit.Assert.*
import org.junit.Test

class SetupReadinessTest {
    @Test fun onlyGrantedAndConnectedRequiredServicesAreReady() {
        for (mask in 0..15) {
            val state = SetupReadiness(mask and 1 != 0, mask and 2 != 0,
                mask and 4 != 0, mask and 8 != 0)
            assertEquals(mask == 15, state.ready)
            assertEquals(state.ready, state.action == SetupAction.CONTINUE)
        }
    }
    @Test fun grantsCannotHideDisconnectedServices() {
        assertEquals(SetupAction.REACTIVATE_OVERLAY, SetupReadiness(true, false, true, true).action)
        assertEquals(SetupAction.REACTIVATE_NOTIFICATIONS, SetupReadiness(true, true, true, false).action)
        assertEquals(1, SetupReadiness(true, false, true, true).workingRequiredCount)
    }
    @Test fun missingAccessHasAnActionEvenWithStaleConnectionFlags() {
        assertEquals(SetupAction.ENABLE_OVERLAY, SetupReadiness(false, true, true, true).action)
        assertEquals(SetupAction.ENABLE_NOTIFICATIONS, SetupReadiness(true, true, false, true).action)
        assertEquals(0, SetupReadiness(false, true, false, true).workingRequiredCount)
    }
}
