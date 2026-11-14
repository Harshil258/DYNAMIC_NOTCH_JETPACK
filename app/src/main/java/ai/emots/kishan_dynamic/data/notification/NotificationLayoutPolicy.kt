package ai.emots.kishan_dynamic.data.notification

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ai.emots.kishan_dynamic.data.model.NotificationActionInfo
import ai.emots.kishan_dynamic.data.model.NotificationActionKind
import ai.emots.kishan_dynamic.data.model.NotificationInfo

/**
 * Layout recipes for the universal adaptive notification renderer.
 * Selection uses typed source capabilities only — never localized copy.
 */
enum class NotificationLayoutRecipe {
    /** Title + body alert; the clean fallback for any unknown notification. */
    TEXT_ALERT,

    /** MessagingStyle alert with a small leading attachment crop (Figma 27:992). */
    THUMBNAIL_ALERT,

    /** Non-conversation BigPicture attachment rendered at intrinsic aspect ratio. */
    LARGE_MEDIA,

    /** MessagingStyle conversation history with per-message senders. */
    CONVERSATION,

    /** InboxStyle multi-line digest. */
    INBOX_DIGEST,

    /** Determinate or indeterminate source progress. */
    PROGRESS,

    /** Live chronometer / countdown. */
    TIMER,

    /** Ongoing navigation source. */
    NAVIGATION,

    /** Group summary that survived child filtering. */
    GROUP_SUMMARY
}

/** Typed visual treatment for a genuine source action. */
data class NotificationActionPresentation(
    val style: NotificationActionVisual,
    val glyph: NotificationActionGlyph,
    val isPrimary: Boolean
)

enum class NotificationActionVisual { PRIMARY, SECONDARY, POSITIVE, DESTRUCTIVE }

enum class NotificationActionGlyph { NONE, CHAT, CHEVRON_RIGHT, PHONE, CLOSE, ARCHIVE, CHECK, BELL_SLASH, THUMB_UP, THUMB_DOWN }

/**
 * Single authoritative content policy for expanded notification pages.
 * One recipe per source; optional regions occupy zero height when unavailable.
 */
object NotificationLayoutPolicy {

    const val EXPANDED_MIN_HEIGHT_DP = 86f

    fun recipe(source: NotificationInfo): NotificationLayoutRecipe = when {
        source.progressMax > 0 || source.isProgressIndeterminate -> NotificationLayoutRecipe.PROGRESS
        source.showChronometer || source.chronometerBaseElapsedRealtime > 0 ||
            source.category == "timer" || source.category == "stopwatch" -> NotificationLayoutRecipe.TIMER
        source.isOngoing && source.category == "navigation" -> NotificationLayoutRecipe.NAVIGATION
        source.picturePath != null && source.isConversation -> NotificationLayoutRecipe.THUMBNAIL_ALERT
        source.picturePath != null -> NotificationLayoutRecipe.LARGE_MEDIA
        source.isGroupSummary -> NotificationLayoutRecipe.GROUP_SUMMARY
        source.isConversation && source.messages.isNotEmpty() -> NotificationLayoutRecipe.CONVERSATION
        source.inboxLines.size > 1 -> NotificationLayoutRecipe.INBOX_DIGEST
        else -> NotificationLayoutRecipe.TEXT_ALERT
    }

    /**
     * Maps a typed source action to its visual treatment. The first capable
     * forward action (reply/open/call) earns the single primary treatment;
     * everything else stays secondary so one card never shouts twice.
     */
    fun actionPresentation(action: NotificationActionInfo, isFirstForwardAction: Boolean): NotificationActionPresentation {
        val kind = effectiveKind(action)
        return when (kind) {
            NotificationActionKind.REPLY -> NotificationActionPresentation(
                NotificationActionVisual.PRIMARY, NotificationActionGlyph.CHAT, isFirstForwardAction
            )
            NotificationActionKind.OPEN -> NotificationActionPresentation(
                NotificationActionVisual.PRIMARY, NotificationActionGlyph.CHEVRON_RIGHT, isFirstForwardAction
            )
            NotificationActionKind.CALL -> NotificationActionPresentation(
                NotificationActionVisual.POSITIVE, NotificationActionGlyph.PHONE, isFirstForwardAction
            )
            NotificationActionKind.DELETE -> NotificationActionPresentation(
                NotificationActionVisual.DESTRUCTIVE, NotificationActionGlyph.CLOSE, false
            )
            NotificationActionKind.ARCHIVE -> NotificationActionPresentation(
                NotificationActionVisual.SECONDARY, NotificationActionGlyph.ARCHIVE, false
            )
            NotificationActionKind.MARK_READ -> NotificationActionPresentation(
                NotificationActionVisual.SECONDARY, NotificationActionGlyph.CHECK, false
            )
            NotificationActionKind.MUTE -> NotificationActionPresentation(
                NotificationActionVisual.SECONDARY, NotificationActionGlyph.BELL_SLASH, false
            )
            NotificationActionKind.THUMBS_UP -> NotificationActionPresentation(
                NotificationActionVisual.SECONDARY, NotificationActionGlyph.THUMB_UP, false
            )
            NotificationActionKind.THUMBS_DOWN -> NotificationActionPresentation(
                NotificationActionVisual.SECONDARY, NotificationActionGlyph.THUMB_DOWN, false
            )
            NotificationActionKind.GENERIC -> NotificationActionPresentation(
                NotificationActionVisual.SECONDARY, NotificationActionGlyph.NONE, false
            )
        }
    }

    fun effectiveKind(action: NotificationActionInfo): NotificationActionKind = when {
        action.isReply || action.kind == NotificationActionKind.REPLY -> NotificationActionKind.REPLY
        action.isDestructive || action.kind == NotificationActionKind.DELETE -> NotificationActionKind.DELETE
        else -> action.kind
    }

    fun isForwardAction(action: NotificationActionInfo): Boolean =
        effectiveKind(action) in setOf(
            NotificationActionKind.REPLY, NotificationActionKind.OPEN, NotificationActionKind.CALL
        )

    /** Richest readable body for a source, honoring inbox and conversation history. */
    fun bodyText(source: NotificationInfo): String = when {
        source.inboxLines.isNotEmpty() -> source.inboxLines.joinToString("\n")
        !source.expandedText.isNullOrBlank() -> source.expandedText
        else -> source.text
    }

    /** Headline identity: conversation title, sender, or source title. */
    fun headline(source: NotificationInfo): String = when {
        source.isConversation && !source.conversationTitle.isNullOrBlank() -> source.conversationTitle
        source.title.isNotBlank() -> source.title
        else -> source.appName
    }

    /**
     * First-frame height estimate only; measured content is authoritative.
     * Keeps the 86dp family floor and never reserves space for absent regions.
     */
    fun estimatePageHeight(source: NotificationInfo, isReplying: Boolean = false): Dp {
        if (isReplying) return 166.dp
        val recipe = recipe(source)
        val base = when (recipe) {
            NotificationLayoutRecipe.THUMBNAIL_ALERT -> 126.dp
            NotificationLayoutRecipe.LARGE_MEDIA -> 168.dp
            else -> 86.dp
        }
        val mediaRecipe = recipe == NotificationLayoutRecipe.THUMBNAIL_ALERT ||
            recipe == NotificationLayoutRecipe.LARGE_MEDIA
        val lineCount = when {
            recipe == NotificationLayoutRecipe.CONVERSATION -> source.messages.size.coerceAtMost(3)
            source.inboxLines.isNotEmpty() -> source.inboxLines.size.coerceAtMost(5)
            else -> bodyText(source).count { it == '\n' } + 1
        }
        val textExtra = if (!mediaRecipe && lineCount >= 3) 18.8.dp else 0.dp
        val actionExtra = if (source.actions.isNotEmpty()) 48.dp else 0.dp
        return base + textExtra + actionExtra
    }
}