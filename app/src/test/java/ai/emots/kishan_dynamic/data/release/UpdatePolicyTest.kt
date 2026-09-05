package ai.emots.kishan_dynamic.data.release

import org.junit.Assert.assertEquals
import org.junit.Test

class UpdatePolicyTest {
    @Test
    fun ignoresDisabledOrCurrentVersions() {
        assertEquals(
            UpdateType.NONE,
            UpdatePolicy.decide(3, 4, 0, enabled = false).type
        )
        assertEquals(
            UpdateType.NONE,
            UpdatePolicy.decide(4, 4, 4, enabled = true).type
        )
    }

    @Test
    fun separatesOptionalAndMandatoryUpdates() {
        assertEquals(UpdateType.SOFT, UpdatePolicy.decide(2, 3, 2, true).type)
        assertEquals(UpdateType.FORCE, UpdatePolicy.decide(2, 3, 3, true).type)
    }
}
