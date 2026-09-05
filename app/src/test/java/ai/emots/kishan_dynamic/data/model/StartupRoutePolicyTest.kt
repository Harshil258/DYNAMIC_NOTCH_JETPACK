package ai.emots.kishan_dynamic.data.model

import org.junit.Assert.assertEquals
import org.junit.Test

class StartupRoutePolicyTest {
    @Test
    fun freshInstallStartsWithLanguageBeforeOnboarding() {
        assertEquals(
            StartupRoute.LANGUAGE_SELECTION,
            initialStartupRoute(false, false)
        )
    }

    @Test
    fun selectedLanguageMovesToOnboarding() {
        assertEquals(
            StartupRoute.ONBOARDING,
            initialStartupRoute(false, true)
        )
    }

    @Test
    fun completedOnboardingDoesNotReplayFirstRunLanguage() {
        assertEquals(
            StartupRoute.MAIN_HUB,
            initialStartupRoute(true, false)
        )
    }

    @Test
    fun editorDeepLinkTargetsEditorOnlyFromMainHub() {
        assertEquals(
            StartupRoute.ACTION_ISLAND_EDITOR,
            startupRouteForIntent(StartupRoute.MAIN_HUB, openActionIslandEditor = true)
        )
        assertEquals(
            StartupRoute.ONBOARDING,
            startupRouteForIntent(StartupRoute.ONBOARDING, openActionIslandEditor = true)
        )
    }
}
