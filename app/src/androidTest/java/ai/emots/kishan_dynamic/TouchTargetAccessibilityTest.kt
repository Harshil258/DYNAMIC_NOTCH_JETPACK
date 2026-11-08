package ai.emots.kishan_dynamic

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import ai.emots.kishan_dynamic.data.model.NotificationActionInfo
import ai.emots.kishan_dynamic.service.IslandStateManager
import ai.emots.kishan_dynamic.ui.components.*
import ai.emots.kishan_dynamic.ui.theme.AppTheme
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Touch Target Accessibility Verification Test for TASK UI-02B.
 *
 * Verifies that while visual controls are rendered with compact, elegant, iOS-matching
 * dimensions (e.g. 38dp glass circles, 32dp action pills, 24dp dismiss buttons), their
 * interactive touch targets meet or exceed the mandatory 44dp-48dp accessibility standard.
 */
@RunWith(AndroidJUnit4::class)
class TouchTargetAccessibilityTest {

    @get:Rule
    val compose = createComposeRule()

    @Before
    fun isolateFixture() {
        ai.emots.kishan_dynamic.service.PermissionUtils.isAwaitingAccessibilityReturn = false
        IslandStateManager.setPaywallActive(true)
    }

    @After
    fun restoreFixture() {
        IslandStateManager.setPaywallActive(false)
    }

    @Test
    fun islandActionChip_hasAccessibleTouchTarget() {
        var clicked = false
        compose.setContent {
            AppTheme(darkTheme = true) {
                Box(Modifier.width(360.dp)) {
                    IslandActionChip(
                        text = "Reply",
                        isPrimary = true,
                        onClick = { clicked = true },
                        modifier = Modifier.testTag("action_chip")
                    )
                }
            }
        }
        compose.waitForIdle()

        val chipNode = compose.onNodeWithTag("action_chip").fetchSemanticsNode()
        val heightDp = chipNode.boundsInRoot.height / compose.density.density
        assertTrue("IslandActionChip height ($heightDp dp) must be >= 44dp", heightDp >= 44f)

        compose.onNodeWithTag("action_chip").performClick()
        assertTrue("Click callback must execute", clicked)
    }

    @Test
    fun notificationDismissButton_hasAccessibleTouchTarget() {
        var dismissed = false

        compose.setContent {
            AppTheme(darkTheme = true) {
                Box(Modifier.width(360.dp)) {
                    NotificationIslandExpanded(
                        appName = "WhatsApp",
                        sender = "Alex Rivera",
                        message = "Hey! Check out the new Dynamic Island update.",
                        onDismiss = { dismissed = true },
                        pagerLabel = "1 of 3"
                    )
                }
            }
        }
        compose.waitForIdle()

        val clickableNodes = compose.onAllNodes(hasClickAction()).fetchSemanticsNodes()
        assertTrue("Must contain clickable nodes in expanded notification", clickableNodes.isNotEmpty())

        clickableNodes.forEach { node ->
            val heightDp = node.boundsInRoot.height / compose.density.density
            assertTrue("Clickable item ($heightDp dp) in notification must be >= 44dp", heightDp >= 44f)
        }
    }

    @Test
    fun musicPlayerIslandControls_haveAccessibleTouchTargets() {
        compose.setContent {
            AppTheme(darkTheme = true) {
                Box(Modifier.width(360.dp)) {
                    DynamicIslandPill(
                        state = IslandDemoState.MusicExpanded,
                        musicIsPlaying = true,
                        modifier = Modifier.width(360.dp)
                    )
                }
            }
        }
        compose.waitForIdle()

        val clickableNodes = compose.onAllNodes(hasClickAction(), useUnmergedTree = true).fetchSemanticsNodes()
        assertTrue("Music expanded must contain interactive controls", clickableNodes.size >= 4)

        clickableNodes.forEach { node ->
            val heightDp = node.boundsInRoot.height / compose.density.density
            val widthDp = node.boundsInRoot.width / compose.density.density
            assertTrue("Media button height ($heightDp dp) must be >= 44dp", heightDp >= 44f)
            assertTrue("Media button width ($widthDp dp) must be >= 40dp", widthDp >= 40f)
        }
    }

    @Test
    fun ongoingCallIslandControls_haveAccessibleTouchTargets() {
        compose.setContent {
            AppTheme(darkTheme = true) {
                Box(Modifier.width(360.dp)) {
                    OngoingCallIslandExpanded(
                        name = "Sarah Chen",
                        duration = "02:22"
                    )
                }
            }
        }
        compose.waitForIdle()

        val clickableNodes = compose.onAllNodes(hasClickAction(), useUnmergedTree = true).fetchSemanticsNodes()
        assertTrue("Ongoing call expanded must contain action buttons", clickableNodes.size >= 3)

        clickableNodes.forEach { node ->
            val heightDp = node.boundsInRoot.height / compose.density.density
            val widthDp = node.boundsInRoot.width / compose.density.density
            assertTrue("Call control height ($heightDp dp) must be >= 44dp", heightDp >= 44f)
            assertTrue("Call control width ($widthDp dp) must be >= 40dp", widthDp >= 40f)
        }
    }

    @Test
    fun incomingCallIslandControls_haveAccessibleTouchTargets() {
        compose.setContent {
            AppTheme(darkTheme = true) {
                Box(Modifier.width(360.dp)) {
                    IncomingCallIsland(
                        name = "Sarah Chen",
                        label = "Mobile"
                    )
                }
            }
        }
        compose.waitForIdle()

        val clickableNodes = compose.onAllNodes(hasClickAction(), useUnmergedTree = true).fetchSemanticsNodes()
        assertTrue("Incoming call must have accept and decline buttons", clickableNodes.size >= 2)

        clickableNodes.forEach { node ->
            val heightDp = node.boundsInRoot.height / compose.density.density
            val widthDp = node.boundsInRoot.width / compose.density.density
            assertTrue("Incoming call button height ($heightDp dp) must be >= 44dp", heightDp >= 44f)
            assertTrue("Incoming call button width ($widthDp dp) must be >= 40dp", widthDp >= 40f)
        }
    }
}
