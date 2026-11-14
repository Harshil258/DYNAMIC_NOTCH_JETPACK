package ai.emots.kishan_dynamic.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ai.emots.kishan_dynamic.ui.motion.pressScale
import ai.emots.kishan_dynamic.ui.theme.AppTheme
import ai.emots.kishan_dynamic.ui.theme.IslandColors

/**
 * Semantic taxonomy for Dynamic Island actions (Aurora V6D Specification).
 * Every action must communicate purpose, priority, and genuine platform capability.
 */
enum class IslandActionStyle {
    /** Best next action (e.g. verified Redial, Reply). High-contrast tinted capsule. */
    Primary,

    /** Secondary affordance (e.g. Open app, secondary messaging). Subdued but clearly enabled. */
    Secondary,

    /** Ringing call accept. Prominent green circular action with white glyph. */
    Positive,

    /** Decline/end call, stop active recording. Prominent red circular or capsule action. */
    Destructive,

    /** Dismiss/Done or quiet overflow. Quiet background, legible neutral text. */
    Tertiary,

    /** Toggle control (mute/speaker/video). Reflects confirmed hardware state. */
    Toggle,

    /** Informational status (not a button). No clickable affordance or fake chevron. */
    Passive,

    /** Overflow for additional supported actions when space is constrained. */
    Overflow
}

/** Semantic color accents for actions and status badges. */
enum class IslandSemanticAccent {
    Blue,
    Green,
    Red,
    Cyan,
    Orange,
    Neutral
}

/**
 * Reusable, semantically typed action capsule for the Dynamic Island.
 * Enforces ≥48dp accessible touch target while preserving a calibrated 36-40dp visual capsule.
 */
@Composable
fun IslandActionPill(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    glyph: AppleGlyph? = null,
    style: IslandActionStyle = IslandActionStyle.Primary,
    accent: IslandSemanticAccent? = null,
    visualHeight: Dp = 38.dp,
    minTouchTarget: Dp = 48.dp,
    glyphSize: Dp = 18.dp,
    fontSize: TextUnit = 12.5.sp,
    fontWeight: FontWeight = FontWeight.SemiBold,
    contentDescription: String? = null,
    enabled: Boolean = true,
    fillWidth: Boolean = true
) {
    if (style == IslandActionStyle.Passive) {
        // Passive status: Non-interactive presentation
        Box(
            modifier = modifier
                .height(visualHeight)
                .clip(RoundedCornerShape(visualHeight / 2))
                .background(Color(0xFF1E2129))
                .padding(horizontal = 14.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                if (glyph != null) {
                    AppleIcon(glyph = glyph, tint = IslandColors.TextSecondary, size = glyphSize)
                }
                Text(
                    text = label,
                    color = IslandColors.TextSecondary,
                    fontSize = fontSize,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        return
    }

    val interaction = remember { MutableInteractionSource() }
    val capsuleShape = RoundedCornerShape(visualHeight / 2)

    val colors = when (style) {
        IslandActionStyle.Primary -> {
            when (accent) {
                IslandSemanticAccent.Cyan -> ActionStyleColors(Color(0xFF0284C7), Color.White, Color.White, null)
                IslandSemanticAccent.Green -> ActionStyleColors(Color(0xFF16A34A), Color.White, Color.White, null)
                IslandSemanticAccent.Red -> ActionStyleColors(IslandColors.CapsuleRed, Color.White, Color.White, null)
                IslandSemanticAccent.Orange -> ActionStyleColors(Color(0xFFEA580C), Color.White, Color.White, null)
                else -> ActionStyleColors(Color(0xFF0A84FF), Color.White, Color.White, null) // Apple Vibrant Blue
            }
        }
        IslandActionStyle.Positive -> ActionStyleColors(IslandColors.CapsuleGreen, Color.White, Color.White, null)
        IslandActionStyle.Destructive -> ActionStyleColors(
            Color(0x33FA3532),
            Color(0xFFFF453A),
            Color(0xFFFF453A),
            BorderDef(0.5.dp, Brush.linearGradient(listOf(Color(0x66FA3532), Color(0x33FA3532))))
        )
        IslandActionStyle.Secondary -> ActionStyleColors(
            Color(0xFF222B3A),
            Color.White,
            accent?.let { accentToColor(it) } ?: IslandColors.CapsuleCyan,
            BorderDef(0.5.dp, Brush.linearGradient(listOf(Color(0x44FFFFFF), Color(0x18FFFFFF))))
        )
        IslandActionStyle.Tertiary -> ActionStyleColors(
            Color(0xFF1C1C1E),
            Color(0xFFE5E7EB),
            IslandColors.TextSecondary,
            BorderDef(0.5.dp, Brush.linearGradient(listOf(Color(0x28FFFFFF), Color(0x10FFFFFF))))
        )
        IslandActionStyle.Toggle -> ActionStyleColors(
            Color.White,
            Color.Black,
            Color.Black,
            null
        )
        IslandActionStyle.Overflow -> ActionStyleColors(
            Color(0xFF2C2C2E),
            Color.White,
            Color.White,
            BorderDef(0.5.dp, Brush.linearGradient(listOf(Color(0x33FFFFFF), Color(0x15FFFFFF))))
        )
        IslandActionStyle.Passive -> ActionStyleColors(Color.Transparent, Color.White, Color.White, null)
    }
    val bg = colors.bg
    val textColor = colors.textColor
    val glyphTint = colors.glyphTint
    val borderStroke = colors.borderStroke

    Box(
        modifier = modifier
            .defaultMinSize(minWidth = minTouchTarget, minHeight = minTouchTarget)
            .clickable(
                interactionSource = interaction,
                indication = null,
                role = Role.Button,
                enabled = enabled,
                onClick = onClick
            )
            .semantics {
                if (contentDescription != null) {
                    this.contentDescription = contentDescription
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .then(if (fillWidth) Modifier.fillMaxWidth() else Modifier)
                .height(visualHeight)
                .clip(capsuleShape)
                .pressScale(interaction, pressedScale = 0.96f, enabled = enabled)
                .background(bg)
                .then(
                    if (borderStroke != null) Modifier.border(borderStroke.width, borderStroke.brush, capsuleShape)
                    else Modifier
                )
                .padding(horizontal = 14.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (glyph != null) {
                    AppleIcon(
                        glyph = glyph,
                        tint = glyphTint,
                        size = glyphSize
                    )
                    Spacer(modifier = Modifier.width(7.dp))
                }
                Text(
                    text = label,
                    color = textColor,
                    fontSize = fontSize,
                    fontWeight = fontWeight,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/**
 * Reusable icon-only circular or rounded action button.
 * Used for call accept/decline, active call toggles, media transport, and recording stop.
 */
@Composable
fun IslandIconAction(
    glyph: AppleGlyph,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: IslandActionStyle = IslandActionStyle.Secondary,
    visualSize: Dp = 44.dp,
    touchTargetSize: Dp = 48.dp,
    iconSize: Dp? = null,
    contentDescription: String,
    isActiveToggle: Boolean = false,
    enabled: Boolean = true
) {
    val interaction = remember { MutableInteractionSource() }
    val effectiveIconSize = iconSize ?: glyph.figmaInkSize(20.dp)

    val (bg, glyphTint, borderStroke) = when (style) {
        IslandActionStyle.Positive -> Triple(IslandColors.CapsuleGreen, Color.White, null)
        IslandActionStyle.Destructive -> Triple(IslandColors.CapsuleRed, Color.White, null)
        IslandActionStyle.Primary -> Triple(Color(0xFF0A84FF), Color.White, null)
        IslandActionStyle.Toggle -> {
            if (isActiveToggle) Triple(Color.White, Color.Black, null)
            else Triple(Color(0xFF2C2C2E), Color.White, BorderDef(0.5.dp, Brush.linearGradient(listOf(Color(0x33FFFFFF), Color(0x15FFFFFF)))))
        }
        IslandActionStyle.Tertiary -> Triple(
            Color(0xFF1C1C1E),
            IslandColors.TextSecondary,
            BorderDef(0.5.dp, Brush.linearGradient(listOf(Color(0x28FFFFFF), Color(0x10FFFFFF))))
        )
        else -> Triple(
            Color(0xFF2C2C2E),
            Color.White,
            BorderDef(0.5.dp, Brush.linearGradient(listOf(Color(0x33FFFFFF), Color(0x15FFFFFF))))
        )
    }

    Box(
        modifier = modifier
            .size(touchTargetSize.coerceAtLeast(visualSize))
            .clickable(
                interactionSource = interaction,
                indication = null,
                role = Role.Button,
                enabled = enabled,
                onClick = onClick
            )
            .semantics { this.contentDescription = contentDescription },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(visualSize)
                .clip(CircleShape)
                .pressScale(interaction, pressedScale = 0.94f, enabled = enabled)
                .background(bg)
                .then(
                    if (borderStroke != null) Modifier.border(borderStroke.width, borderStroke.brush, CircleShape)
                    else Modifier
                ),
            contentAlignment = Alignment.Center
        ) {
            AppleIcon(
                glyph = glyph,
                tint = glyphTint,
                size = effectiveIconSize
            )
        }
    }
}

/**
 * Standard horizontal control container for Dynamic Island actions.
 * Enforces consistent 8dp gaps and alignment without vertical layout breakage.
 */
@Composable
fun IslandControlRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.spacedBy(8.dp),
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
    content: @Composable RowScope.() -> Unit
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = horizontalArrangement,
        verticalAlignment = verticalAlignment,
        content = content
    )
}

/**
 * Safe, conservative platform action capability resolver.
 * Replaces dangerous getOrDefault(true) fallbacks with verifiable Intent checks.
 */
object IslandActionCapability {
    fun canDial(context: Context, phoneNumber: String?): Boolean {
        if (phoneNumber.isNullOrBlank() || !phoneNumber.any(Char::isDigit)) return false
        return runCatching {
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phoneNumber"))
            context.packageManager.queryIntentActivities(intent, 0).isNotEmpty()
        }.getOrDefault(false)
    }

    fun canSendSms(context: Context, phoneNumber: String?): Boolean {
        if (phoneNumber.isNullOrBlank() || !phoneNumber.any(Char::isDigit)) return false
        return runCatching {
            val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:$phoneNumber"))
            context.packageManager.queryIntentActivities(intent, 0).isNotEmpty()
        }.getOrDefault(false)
    }

    fun canOpenSettings(context: Context, action: String = Settings.ACTION_SETTINGS): Boolean {
        return runCatching {
            val intent = Intent(action)
            context.packageManager.queryIntentActivities(intent, 0).isNotEmpty()
        }.getOrDefault(false)
    }
}

private data class ActionStyleColors(
    val bg: Color,
    val textColor: Color,
    val glyphTint: Color,
    val borderStroke: BorderDef?
)

private data class BorderDef(val width: Dp, val brush: Brush)

private fun accentToColor(accent: IslandSemanticAccent): Color = when (accent) {
    IslandSemanticAccent.Blue -> Color(0xFF0A84FF)
    IslandSemanticAccent.Green -> IslandColors.CapsuleGreen
    IslandSemanticAccent.Red -> IslandColors.CapsuleRed
    IslandSemanticAccent.Cyan -> IslandColors.CapsuleCyan
    IslandSemanticAccent.Orange -> Color(0xFFFB8B28)
    IslandSemanticAccent.Neutral -> Color.White
}
