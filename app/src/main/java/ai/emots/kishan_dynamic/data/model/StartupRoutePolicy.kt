package ai.emots.kishan_dynamic.data.model

/** Startup sequence for a fresh install, kept independent from Compose navigation. */
enum class StartupRoute {
    LANGUAGE_SELECTION,
    ONBOARDING,
    MAIN_HUB,
    ACTION_ISLAND_EDITOR
}

fun initialStartupRoute(
    onboardingCompleted: Boolean,
    languageSelectionCompleted: Boolean
): StartupRoute = when {
    onboardingCompleted -> StartupRoute.MAIN_HUB
    !languageSelectionCompleted -> StartupRoute.LANGUAGE_SELECTION
    else -> StartupRoute.ONBOARDING
}

/** Deep links from the overlay may target the editor only after setup routing is complete. */
fun startupRouteForIntent(baseRoute: StartupRoute, openActionIslandEditor: Boolean): StartupRoute =
    if (openActionIslandEditor && baseRoute == StartupRoute.MAIN_HUB) {
        StartupRoute.ACTION_ISLAND_EDITOR
    } else {
        baseRoute
    }
