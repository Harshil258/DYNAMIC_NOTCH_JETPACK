package ai.emots.kishan_dynamic.ui.theme

import ai.emots.kishan_dynamic.R
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight

/** Official Inter4.1 static files. No runtime font download or API26-only variation requirement. */
object IslandReferenceFonts {
    val Text = FontFamily(
        Font(R.font.aurora_inter_text_light, FontWeight.Light),
        Font(R.font.aurora_inter_text_regular, FontWeight.Normal),
        Font(R.font.aurora_inter_text_medium, FontWeight.Medium),
        Font(R.font.aurora_inter_text_semibold, FontWeight.SemiBold),
        Font(R.font.aurora_inter_text_bold, FontWeight.Bold)
    )
    val Display = FontFamily(
        Font(R.font.aurora_inter_display_light, FontWeight.Light),
        Font(R.font.aurora_inter_display_regular, FontWeight.Normal),
        Font(R.font.aurora_inter_display_medium, FontWeight.Medium),
        Font(R.font.aurora_inter_display_semibold, FontWeight.SemiBold),
        Font(R.font.aurora_inter_display_bold, FontWeight.Bold)
    )
}

internal fun AppTypographyTokens.withIslandReferenceFonts(): AppTypographyTokens {
    fun androidx.compose.ui.text.TextStyle.text() = copy(fontFamily = IslandReferenceFonts.Text,
        platformStyle = PlatformTextStyle(includeFontPadding = false))
    fun androidx.compose.ui.text.TextStyle.display() = text().copy(fontFamily = IslandReferenceFonts.Display)
    return copy(h1 = h1.display(), h2 = h2.display(), h3 = h3.display(),
        body = body.text(), bodySmall = bodySmall.text(), button = button.text(), caption = caption.text(),
        islandTitle = islandTitle.text(), islandSubtitle = islandSubtitle.text(),
        islandTime = islandTime.text().copy(fontFeatureSettings = "tnum"),
        islandBadge = islandBadge.text(), islandAction = islandAction.text(), badge = badge.text())
}
