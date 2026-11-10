package ai.emots.kishan_dynamic.ui.components

import androidx.compose.runtime.Composer
import org.junit.Assert.assertEquals
import org.junit.Test

/** Guards the startup crash observed with HotSwan2.2 and the previous Compose runtime. */
class HotSwanComposeBoundaryTest {
    @Test fun resolvedComposeRuntimeSupportsTheHotSwanExecutionBoundary() {
        val method = Composer::class.java.getMethod(
            "shouldExecute", Boolean::class.javaPrimitiveType, Int::class.javaPrimitiveType
        )
        assertEquals(Boolean::class.javaPrimitiveType, method.returnType)
        Composer::class.java.getMethod("startReplaceGroup", Int::class.javaPrimitiveType)
        Composer::class.java.getMethod("endReplaceGroup")
        Composer::class.java.getMethod("skipToGroupEnd")
    }
}
