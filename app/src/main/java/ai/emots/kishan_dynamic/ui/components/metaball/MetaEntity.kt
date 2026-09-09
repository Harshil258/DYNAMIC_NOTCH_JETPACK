package ai.emots.kishan_dynamic.ui.components.metaball

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

/**
 * Individual entity that participates in the metaball animation.
 *
 * Each MetaEntity has two layers:
 * 1. metaContent: A blurred version of the shape (typically a solid color Box)
 *    that creates the stretchy organic bridge when near other entities.
 * 2. content: The sharp visible content (icons, text) rendered on top.
 */
@Composable
fun MetaEntity(
    modifier: Modifier = Modifier,
    blur: Float = 30f,
    metaContent: @Composable BoxScope.() -> Unit,
    content: @Composable BoxScope.() -> Unit = {}
) {
    Box(
        modifier = modifier
            .width(IntrinsicSize.Min)
            .height(IntrinsicSize.Min),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier.customBlur(blur),
            content = metaContent
        )
        content()
    }
}
