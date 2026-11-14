package ai.emots.kishan_dynamic.playground

import android.content.Context
import android.os.SystemClock
import ai.emots.kishan_dynamic.R
import ai.emots.kishan_dynamic.data.model.NotificationActionInfo
import ai.emots.kishan_dynamic.data.model.NotificationActionKind
import ai.emots.kishan_dynamic.data.model.NotificationInfo
import ai.emots.kishan_dynamic.data.model.NotificationMessageInfo

/**
 * Synthetic real-world notification shapes for gallery QA of the universal
 * adaptive renderer. Debug only; never touches production preferences.
 * Each mode exercises one layout recipe plus its action/edge variants.
 */
object RealWorldNotificationFixtures {

    val modeLabels = listOf(
        "Figma 27:992 (126dp)",
        "Short Text (86dp)",
        "Multi-line",
        "Media",
        "Inbox digest",
        "Download 63%",
        "Indeterminate",
        "Timer",
        "Navigation",
        "Group summary",
        "Big picture",
        "Conversation",
        "Empty fallback",
        "Typed actions",
        "Long copy"
    )

    fun isRealWorldMode(mode: Int): Boolean = mode in 4..14

    fun notification(mode: Int, index: Int, context: Context): NotificationInfo = when (mode) {
        4 -> NotificationInfo(
            id = "inbox-$index",
            packageName = "com.google.android.gm",
            appName = "Gmail",
            title = "Inbox digest",
            text = "5 new messages",
            subText = "5 new messages",
            inboxLines = listOf(
                "Priya: Can you review the Q3 forecast before lunch?",
                "GitHub: [aurora/island] PR #482 approved by maintainers",
                "Figma: New comment on Notification Expanded",
                "Calendar: Design sync moved to 15:30",
                "Slack: 3 new mentions in #dynamic-island"
            ),
            timestamp = index + 1L,
            actions = listOf(
                NotificationActionInfo("inbox-archive-$index", "Archive", kind = NotificationActionKind.ARCHIVE),
                NotificationActionInfo("inbox-read-$index", "Mark as read", kind = NotificationActionKind.MARK_READ)
            )
        )
        5 -> NotificationInfo(
            id = "download-$index",
            packageName = "com.android.chrome",
            appName = "Chrome",
            title = "Downloading design-assets.zip",
            text = "63% · 12.4 MB of 19.7 MB",
            category = "progress",
            isOngoing = true,
            isClearable = false,
            progress = 63,
            progressMax = 100,
            timestamp = index + 1L,
            actions = listOf(NotificationActionInfo("dl-cancel-$index", "Cancel"))
        )
        6 -> NotificationInfo(
            id = "sync-$index",
            packageName = "com.google.android.apps.photos",
            appName = "Photos",
            title = "Backing up photos",
            text = "Preparing…",
            isOngoing = true,
            isClearable = false,
            isProgressIndeterminate = true,
            timestamp = index + 1L
        )
        7 -> NotificationInfo(
            id = "timer-$index",
            packageName = "com.google.android.deskclock",
            appName = "Clock",
            title = if (index % 2 == 0) "Focus timer" else "Countdown",
            text = if (index % 2 == 0) "15 minute focus session" else "5 minute break",
            category = "timer",
            isOngoing = true,
            showChronometer = true,
            chronometerBaseElapsedRealtime = if (index % 2 == 0) {
                SystemClock.elapsedRealtime() - (95_000L + index * 1_000L)
            } else {
                SystemClock.elapsedRealtime() + 300_000L - index * 1_000L
            },
            chronometerCountDown = index % 2 == 1,
            timestamp = index + 1L,
            actions = listOf(
                NotificationActionInfo("timer-pause-$index", "Pause"),
                NotificationActionInfo("timer-stop-$index", "Stop", kind = NotificationActionKind.DELETE, isDestructive = true)
            )
        )
        8 -> NotificationInfo(
            id = "nav-$index",
            packageName = "com.google.android.apps.maps",
            appName = "Maps",
            title = "Turn left onto University Ave",
            text = "500 m · then turn right onto Riverside Dr",
            subText = "12 min · 4.2 km",
            category = "navigation",
            isOngoing = true,
            isClearable = false,
            timestamp = index + 1L,
            actions = listOf(NotificationActionInfo("nav-exit-$index", "Exit navigation"))
        )
        9 -> NotificationInfo(
            id = "group-$index",
            packageName = "com.whatsapp",
            appName = "WhatsApp",
            title = "WhatsApp",
            text = "7 messages from 4 chats",
            subText = "4 chats",
            isGroupSummary = true,
            groupChildCount = 7,
            timestamp = index + 1L,
            actions = listOf(
                NotificationActionInfo("group-read-$index", "Mark as read", kind = NotificationActionKind.MARK_READ),
                NotificationActionInfo("group-mute-$index", "Mute", kind = NotificationActionKind.MUTE)
            )
        )
        10 -> NotificationInfo(
            id = "bigpic-$index",
            packageName = "com.instagram.android",
            appName = "Instagram",
            title = "design.daily",
            text = "Posted a new photo",
            picturePath = FigmaReferenceFixtures.resourceUri(context, R.drawable.shared_media_art),
            timestamp = index + 1L,
            actions = listOf(
                NotificationActionInfo("pic-like-$index", "Like", kind = NotificationActionKind.THUMBS_UP),
                NotificationActionInfo("pic-open-$index", "View", kind = NotificationActionKind.OPEN)
            )
        )
        11 -> NotificationInfo(
            id = "chat-$index",
            packageName = "com.whatsapp",
            appName = "WhatsApp",
            title = "Design team",
            text = "Alex: Shipping the new island today",
            isConversation = true,
            conversationTitle = "Design team",
            messages = listOf(
                NotificationMessageInfo("Priya", "The height contract is merged", 1L),
                NotificationMessageInfo("Alex", "Nice! Pager feels much smoother now", 2L),
                NotificationMessageInfo("Priya", "One more review pass and we ship", 3L),
                NotificationMessageInfo("Alex", "Shipping the new island today", 4L)
            ),
            timestamp = index + 1L,
            actions = listOf(
                NotificationActionInfo("chat-reply-$index", "Reply", isReply = true, kind = NotificationActionKind.REPLY),
                NotificationActionInfo("chat-read-$index", "Mark as read", kind = NotificationActionKind.MARK_READ)
            )
        )
        12 -> NotificationInfo(
            id = "empty-$index",
            packageName = "com.unknown.app",
            appName = "Unknown App",
            title = "",
            text = "",
            timestamp = index + 1L
        )
        13 -> NotificationInfo(
            id = "actions-$index",
            packageName = "com.Slack",
            appName = "Slack",
            title = "#dynamic-island",
            text = "Harshil: Can someone verify the 86dp floor on device?",
            subText = "#dynamic-island",
            timestamp = index + 1L,
            actions = listOf(
                NotificationActionInfo("act-reply-$index", "Reply", isReply = true, kind = NotificationActionKind.REPLY),
                NotificationActionInfo("act-read-$index", "Mark as read", kind = NotificationActionKind.MARK_READ),
                NotificationActionInfo("act-mute-$index", "Mute", kind = NotificationActionKind.MUTE),
                NotificationActionInfo("act-delete-$index", "Delete", kind = NotificationActionKind.DELETE, isDestructive = true)
            )
        )
        else -> NotificationInfo(
            id = "long-$index",
            packageName = "com.google.android.gm",
            appName = "Gmail",
            title = "Quarterly planning review — consolidated feedback from all regional leads",
            text = "Thanks for sending the draft ahead of time. I've collected comments from the regional leads and consolidated them into the attached document. The main themes are around notification height stability, pager selection commits, and the new adaptive layout system. Please review before Thursday's sync so we can close the remaining open questions together.",
            timestamp = index + 1L
        )
    }
}