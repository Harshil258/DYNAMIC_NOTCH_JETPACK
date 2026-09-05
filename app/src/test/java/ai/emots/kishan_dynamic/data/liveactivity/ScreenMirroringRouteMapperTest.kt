package ai.emots.kishan_dynamic.data.liveactivity

import ai.emots.kishan_dynamic.data.model.LiveActivityKind
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ScreenMirroringRouteMapperTest {

    @Test
    fun mapsSelectedExternalVideoRoute() {
        val activity = mapScreenMirroringRoute(
            isDefaultRoute = false,
            routeName = "Living Room TV"
        )

        assertEquals(LiveActivityKind.SCREEN_MIRRORING_ALERT, activity?.kind)
        assertEquals("Living Room TV", activity?.subtitle)
    }

    @Test
    fun ignoresDefaultOrUnnamedRoutes() {
        assertNull(mapScreenMirroringRoute(true, "Phone"))
        assertNull(mapScreenMirroringRoute(false, " ", " "))
    }
}
