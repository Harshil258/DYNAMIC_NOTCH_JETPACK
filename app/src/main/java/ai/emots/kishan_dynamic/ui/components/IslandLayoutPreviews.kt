package ai.emots.kishan_dynamic.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import ai.emots.kishan_dynamic.data.model.NotificationInfo
import ai.emots.kishan_dynamic.ui.motion.LocalReducedMotion
import ai.emots.kishan_dynamic.ui.theme.AppTheme

@Preview(name = "Pro access · 320dp", widthDp = 320, showBackground = true, backgroundColor = 0xFF243044)
@Preview(name = "Pro access · 360dp", widthDp = 360, showBackground = true, backgroundColor = 0xFF243044)
@Preview(name = "Pro access · 393dp", widthDp = 393, showBackground = true, backgroundColor = 0xFF243044)
@Preview(name = "Pro access · 430dp", widthDp = 430, showBackground = true, backgroundColor = 0xFF243044)
@Preview(name = "Pro access · 200% text", widthDp = 320, fontScale = 2f, showBackground = true, backgroundColor = 0xFF243044)
@Composable
private fun ExpiryIslandPreview() = IslandLayoutPreview(IslandDemoState.PremiumExpiryExpanded)

@Preview(name = "Notification count · 320dp", widthDp = 320, showBackground = true, backgroundColor = 0xFF243044)
@Preview(name = "Notification count · 200% text", widthDp = 320, fontScale = 2f, showBackground = true, backgroundColor = 0xFF243044)
@Composable
private fun CompactCountPreview() = IslandLayoutPreview(IslandDemoState.NotificationStacked)

@Composable
private fun IslandLayoutPreview(state: IslandDemoState) {
    AppTheme(darkTheme = true) {
        androidx.compose.runtime.CompositionLocalProvider(LocalReducedMotion provides true) {
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
                DynamicIslandPill(
                    state = state, premiumHoursRemaining = 0, unseenNotificationCount = 8,
                    notifications = listOf(NotificationInfo("preview", "test.app", "Messages", "New message", "Preview message"))
                )
            }
        }
    }
}
