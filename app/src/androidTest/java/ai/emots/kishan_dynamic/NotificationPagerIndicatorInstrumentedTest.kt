package ai.emots.kishan_dynamic

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.*
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import ai.emots.kishan_dynamic.data.model.NotificationInfo
import ai.emots.kishan_dynamic.ui.components.NotificationIslandExpandedPager
import ai.emots.kishan_dynamic.ui.theme.AppTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Task 04B Compose UI InstrumentedTest for Notification Pager Indicators.
 *
 * Verifies that the Expanded Pager shows its pager indicator for 2+ notifications
 * without requiring an extra gesture, and that a single notification does not show
 * unnecessary dots/labels.
 */
@RunWith(AndroidJUnit4::class)
class NotificationPagerIndicatorInstrumentedTest {

    @get:Rule
    val compose = createComposeRule()

    private fun sample(id: String, title: String) = NotificationInfo(
        id = id,
        packageName = "com.test.$id",
        appName = "TestApp",
        title = title,
        text = "Body for $id",
        timestamp = System.currentTimeMillis()
    )

    @Test
    fun multiNotificationPager_showsIndicatorLabel_withoutExtraGesture() {
        val notifications = listOf(
            sample("msg1", "First Message"),
            sample("msg2", "Second Message")
        )

        compose.setContent {
            AppTheme {
                NotificationIslandExpandedPager(
                    notifications = notifications,
                    activeIndex = 0,
                    isCardExpanded = false, // Must show indicator without card expansion gesture
                    onActionSelected = { _, _ -> },
                    onDismiss = {}
                )
            }
        }

        // Pager label "1 of 2" must exist on the first page
        compose.onNodeWithTag("notification_vertical_pager").assert(
            SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, "Notification 1 of 2"))
        compose.onNodeWithTag("notification_vertical_indicator").assertExists()
    }

    @Test
    fun singleNotificationPager_doesNotShowIndicatorLabel() {
        val notifications = listOf(
            sample("single", "Single Message")
        )

        compose.setContent {
            AppTheme {
                NotificationIslandExpandedPager(
                    notifications = notifications,
                    activeIndex = 0,
                    isCardExpanded = false,
                    onActionSelected = { _, _ -> },
                    onDismiss = {}
                )
            }
        }

        // Single notification should not show "1 of 1"
        compose.onNodeWithText("1 of 1", substring = true, useUnmergedTree = true).assertDoesNotExist()
    }
}
