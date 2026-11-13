# AURORA V6C — CUSTOM NOTIFICATION STRESS TESTS & LIFECYCLE AUDIT

**Author:** Staff Android Jetpack Compose Engineer & QA Examiner  
**Date:** November 2026  
**Scope:** Real Android Notification Ingestion, Stack Management, Inline Reply, and 24 Stress Variations

---

## 1. Zero-Synthetic-Data Production Contract

A core finding of the V6 independent audit was the danger of synthetic demonstration payloads (e.g. "Tamia Castillo", "Alex Morgan", "Route 24", "Market Street") appearing in production builds. In Aurora V6C:

1. **Production Isolation:** In `IslandOverlayService.kt`, all state derives exclusively from genuine Android system events:
   - `IslandNotificationListener` for real Android `StatusBarNotification`s.
   - `MediaSessionManager` for active media playback.
   - `TelecomManager` / `PhoneStateListener` for live telephony calls.
   - `BatteryManager` for real hardware charge percentage.
2. **Strict Guard on Previews:** Synthetic fixtures are quarantined behind `isDemoPreview` flags (`record?.id == "gallery_summary"` or explicit playground debug modes). If a notification or call summary payload is null in production, the Island displays honest truthful system states or collapses to Idle.

---

## 2. 24 Custom Notification Stress Test Matrix

The following 24 payload stress variations were tested across Compose unit harnesses, `PlaygroundScreen`, and live Android view hierarchies:

| # | Variation Name | Payload Characteristics | Adaptive Island Behavior | Result |
|---|---|---|---|---|
| 1 | **Minimal Short** | Title: "Sam", Text: "Hey" (single line) | Compact: 190×36.7dp. Expanded: naturally hugs to 86dp without empty gaps. | **PASS** |
| 2 | **Multi-line Body** | 3-line message body (180 chars) | Text reflows cleanly; height expands dynamically to accommodate 3 lines without clipping. | **PASS** |
| 3 | **Extreme Title Length** | Title: 85 characters with zero spaces | `TextOverflow.Ellipsis` clamps at 1 line; title never pushes trailing time off-screen. | **PASS** |
| 4 | **Extreme Body Length** | Text: 500 characters of continuous prose | Displays first 4 lines in expanded card; trailing text smoothly fades with ellipsis. | **PASS** |
| 5 | **Missing App Icon** | `packageName = ""` or uninstalled package | Gracefully renders generic notification vector fallback; zero crash or blank box. | **PASS** |
| 6 | **Single Action Button** | 1 button ("Mark as read") | Single button spans row naturally; accessible 48dp touch target enforced. | **PASS** |
| 7 | **Dual Action Buttons** | 2 buttons ("Reply", "Archive") | Balanced 2-button horizontal row with 8dp spacing; full hit bounds. | **PASS** |
| 8 | **Triple Action Buttons** | 3 buttons ("Reply", "Mark Read", "Mute") | Buttons distribute proportionally without text truncation. | **PASS** |
| 9 | **Quad Action Buttons** | 4 buttons ("Reply", "Done", "Snooze", "Block") | Wraps gracefully using `FlowRow`; expands island height to prevent button overlap. | **PASS** |
| 10 | **Inline Reply Active** | User taps "Reply"; keyboard focus requested | Island expands to reply composition mode; draft preserved in `ReplyDraftMemory`. | **PASS** |
| 11 | **Large Picture Asset** | `picturePath != null` (e.g. camera photo, screenshot) | Expands to `NotificationImage` mode (367×126dp, R44); image clipped with 12dp radius. | **PASS** |
| 12 | **Large Picture + 2 Actions** | Picture + "Share" and "Delete" buttons | Height dynamically adapts to 154dp to fit image plus action row comfortably. | **PASS** |
| 13 | **Empty Message Text** | `text = ""`, Title: "Reminder" | Body row collapses to 0dp; header and timestamp remain optically centered. | **PASS** |
| 14 | **Emoji Only Content** | Title: "🎉🥳", Text: "🚀🔥💃✨" | Full color emoji rendering without font baseline shift or glyph clipping. | **PASS** |
| 15 | **RTL Text Direction** | Arabic/Hebrew text | `LocalLayoutDirection` flips layout naturally: icon on right, text aligns right. | **PASS** |
| 16 | **Financial Sensitive (Locked)** | "Bank of America: $1,250 transferred" | **Redacted:** "Bank · Payment received · Unlock to view transaction details". Zero balance leak. | **PASS** |
| 17 | **OTP Verification (Locked)** | "Google: 849201 is your verification code" | **Redacted:** "Google · New notification · Unlock to view". OTP strictly masked. | **PASS** |
| 18 | **Determinate Progress (45%)** | Download in progress: 45 of 100 MB | Progress bar renders cyan gradient at 45% width; metric shows "45%". | **PASS** |
| 19 | **Determinate Progress (100%)** | Download complete: 100 of 100 MB | Progress bar fills 100%; converts to checkmark state. | **PASS** |
| 20 | **Indeterminate Progress** | Ongoing backup without max length | Smooth pulsing progress shimmer across track. | **PASS** |
| 21 | **Dual Stacked Queue** | 2 unread notifications from different apps | `NotificationStacked` mode: leading icon + "+1" badge at trailing edge. | **PASS** |
| 22 | **Maximum Queue (50 items)** | 50 incoming notifications loaded simultaneously | Capacity clamps at 50 (`MAX_NOTIFICATIONS = 50`); oldest non-priority trimmed. | **PASS** |
| 23 | **Notification with Active Music** | Incoming notification while Spotify plays | Main pill shows album art + waveform; companion bubble shows notification badge. | **PASS** |
| 24 | **Queue Dismissed to Empty** | User dismisses last remaining notification | Island performs non-destructive collapse and returns to clean `Idle` silhouette. | **PASS** |

---

## 3. Inline Reply Lifecycle & Draft Memory

1. **Focus Request & Accessibility Window:**
   - Tapping "Reply" triggers `enableOverlayFocusable()`, allowing the accessibility overlay window to receive soft keyboard input (`FLAG_NOT_FOCUSABLE` temporarily cleared).
2. **Draft Persistence (`ReplyDraftMemory`):**
   - If user types a reply and receives an incoming call or minimizes the app, draft text is retained in `ReplyDraftMemory.store.put(notificationId, draft)`.
   - Returning to the notification restores the exact text previously typed.
3. **Dispatch & Cleanup:**
   - Tapping "Send" passes text to `NotificationActionRegistry.sendReply(context, notificationId, actionId, replyText)`.
   - On successful dispatch, draft is cleared and `restoreOverlayNonFocusable()` immediately restores pass-through touch events.

---

## 4. Gesture & Dismissal Integrity

- **Non-Destructive Swipe Collapse (UX-C06 / PD-009):** Swiping up on an expanded notification card collapses the Island to its compact silhouette. The notification remains preserved in the Android notification shade unless the user explicitly enables "Dismiss cancels OS notification" in settings.
- **Horizontal Pager Navigation:** In multi-notification stacked states, swiping left/right cycles through active cards without lag or visual tearing.
