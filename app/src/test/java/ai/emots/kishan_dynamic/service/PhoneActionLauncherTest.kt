package ai.emots.kishan_dynamic.service

import android.content.Intent
import org.junit.Assert.assertEquals
import org.junit.Test

class PhoneActionLauncherTest {

    @Test
    fun directCallRequiresExplicitCallPermission() {
        assertEquals(Intent.ACTION_CALL, PhoneActionLauncher.actionFor(true))
        assertEquals(Intent.ACTION_DIAL, PhoneActionLauncher.actionFor(false))
    }
}
