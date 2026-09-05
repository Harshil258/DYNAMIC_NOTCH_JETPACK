package ai.emots.kishan_dynamic.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ai.emots.kishan_dynamic.data.release.AnalyticsEvent
import ai.emots.kishan_dynamic.data.release.AppAnalytics
import ai.emots.kishan_dynamic.ui.theme.AppTheme

/** Small, explicit upgrade affordance shown only beside a loaded ad surface. */
@Composable
fun RemoveAdsInlineCta(
    placement: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .defaultMinSize(minHeight = 44.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                AppAnalytics.track(
                    AnalyticsEvent("remove_ads_cta_tapped", mapOf("placement" to placement))
                )
                onClick()
            }
            .padding(horizontal = AppTheme.spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs)
    ) {
        AppleIcon(
            glyph = AppleGlyph.Crown,
            tint = AppTheme.colors.gold,
            size = 15.dp
        )
        AppText(
            text = "Remove ads",
            style = AppTheme.typography.caption,
            color = AppTheme.colors.textSecondary
        )
    }
}
