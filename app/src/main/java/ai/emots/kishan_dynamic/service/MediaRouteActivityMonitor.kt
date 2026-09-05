package ai.emots.kishan_dynamic.service

import android.content.Context
import android.media.MediaRouter
import ai.emots.kishan_dynamic.data.liveactivity.mapScreenMirroringRoute

/**
 * Observes Android live-video route selection without owning the route or
 * copying any source app state. The monitor only publishes a state while a
 * non-default video destination is selected.
 */
@Suppress("DEPRECATION")
class MediaRouteActivityMonitor(private val context: Context) {

    private val router = context.getSystemService(Context.MEDIA_ROUTER_SERVICE) as? MediaRouter
    private val callback = object : MediaRouter.Callback() {
        override fun onRouteSelected(
            router: MediaRouter,
            type: Int,
            route: MediaRouter.RouteInfo
        ) {
            if (type and MediaRouter.ROUTE_TYPE_LIVE_VIDEO != 0) publish(route)
        }

        override fun onRouteUnselected(
            router: MediaRouter,
            type: Int,
            route: MediaRouter.RouteInfo
        ) {
            if (type and MediaRouter.ROUTE_TYPE_LIVE_VIDEO != 0) {
                IslandStateManager.clearLiveActivity("media-route-screen-mirroring")
            }
        }

        override fun onRouteChanged(router: MediaRouter, route: MediaRouter.RouteInfo) {
            if (route == router.getSelectedRoute(MediaRouter.ROUTE_TYPE_LIVE_VIDEO)) {
                publish(route)
            }
        }

        override fun onRouteAdded(router: MediaRouter, route: MediaRouter.RouteInfo) = Unit

        override fun onRouteRemoved(router: MediaRouter, route: MediaRouter.RouteInfo) {
            if (route == router.getSelectedRoute(MediaRouter.ROUTE_TYPE_LIVE_VIDEO)) {
                IslandStateManager.clearLiveActivity("media-route-screen-mirroring")
            }
        }

        override fun onRouteGrouped(
            router: MediaRouter,
            route: MediaRouter.RouteInfo,
            group: MediaRouter.RouteGroup,
            index: Int
        ) = Unit

        override fun onRouteUngrouped(
            router: MediaRouter,
            route: MediaRouter.RouteInfo,
            group: MediaRouter.RouteGroup
        ) = Unit

        override fun onRouteVolumeChanged(router: MediaRouter, route: MediaRouter.RouteInfo) = Unit
    }

    fun start() {
        val mediaRouter = router ?: return
        mediaRouter.addCallback(
            MediaRouter.ROUTE_TYPE_LIVE_VIDEO,
            callback,
            MediaRouter.CALLBACK_FLAG_PERFORM_ACTIVE_SCAN
        )
        publish(mediaRouter.getSelectedRoute(MediaRouter.ROUTE_TYPE_LIVE_VIDEO))
    }

    fun close() {
        router?.removeCallback(callback)
        IslandStateManager.clearLiveActivity("media-route-screen-mirroring")
    }

    private fun publish(route: MediaRouter.RouteInfo) {
        mapScreenMirroringRoute(
            isDefaultRoute = route == router?.getDefaultRoute(),
            routeName = route.getName(context)?.toString(),
            routeDescription = route.description?.toString()
        )?.let(IslandStateManager::postLiveActivity)
            ?: IslandStateManager.clearLiveActivity("media-route-screen-mirroring")
    }
}
