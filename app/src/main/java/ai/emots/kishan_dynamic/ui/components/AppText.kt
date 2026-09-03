package ai.emots.kishan_dynamic.ui.components

import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import ai.emots.kishan_dynamic.ui.theme.AppTheme

/**
 * Universal token-driven Text composable.
 * Every text in the app uses this component with an AppTheme.typography token,
 * ensuring 100% adaptability and global token switching.
 */
@Composable
fun AppText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = AppTheme.typography.body,
    color: Color = Color.Unspecified,
    textAlign: TextAlign? = null,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
    fontWeight: FontWeight? = null,
    fontFamily: androidx.compose.ui.text.font.FontFamily? = null,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    lineHeight: TextUnit = TextUnit.Unspecified
) {
    val resolvedColor = if (color != Color.Unspecified) color else AppTheme.colors.textPrimary
    var finalStyle = style.copy(color = resolvedColor)

    if (textAlign != null) {
        finalStyle = finalStyle.copy(textAlign = textAlign)
    }
    if (fontWeight != null) {
        finalStyle = finalStyle.copy(fontWeight = fontWeight)
    }
    if (fontFamily != null) {
        finalStyle = finalStyle.copy(fontFamily = fontFamily)
    }
    if (letterSpacing != TextUnit.Unspecified) {
        finalStyle = finalStyle.copy(letterSpacing = letterSpacing)
    }
    if (lineHeight != TextUnit.Unspecified) {
        finalStyle = finalStyle.copy(lineHeight = lineHeight)
    }

    BasicText(
        text = text,
        modifier = modifier,
        style = finalStyle,
        maxLines = maxLines,
        overflow = overflow
    )
}
