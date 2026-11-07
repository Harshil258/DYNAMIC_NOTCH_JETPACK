# Android Notification Pipeline & Payload Processing Audit (Corrected)

## 1. End-to-End Architectural Data Flow

The Dynamic Island processes Android notifications through a decoupled, asynchronous, 8-stage pipeline:

```
┌────────────────────────────────────────────────────────────────────────┐
│ 1. Android Notification Received                                       │
│    IslandNotificationListener.onNotificationPosted(sbn)               │
└───────────────────────────────────┬────────────────────────────────────┘
                                    │
┌───────────────────────────────────▼────────────────────────────────────┐
│ 2. Payload Extraction & Normalization                                  │
│    - Extras extraction (Title, Text, BigText, InboxLines, Progress)    │
│    - NotificationContentResolver.resolveNotificationContent()          │
│    - Asynchronous artwork decoding via NotificationArtworkRepository  │
└───────────────────────────────────┬────────────────────────────────────┘
                                    │
┌───────────────────────────────────▼────────────────────────────────────┐
│ 3. Classification & Event Filtering                                    │
│    - CallNotificationPolicy (VoIP CallStyle vs incoming vs ongoing)    │
│    - ClockNotificationClassifier (Timer / stopwatch chronometer)       │
│    - NotificationLiveActivityMapper.map() (Navigation, Delivery, etc.) │
│    - NotificationEventPolicy.evaluate() (IGNORED vs TRANSIENT/ONGOING) │
└───────────────────────────────────┬────────────────────────────────────┘
                                    │
┌───────────────────────────────────▼────────────────────────────────────┐
│ 4. Queue State Reduction & Ordering                                    │
│    - NotificationReducer.reduce(state, NotificationEvent.Posted)       │
│    - Deduplication, in-place progress update, sorting by importance    │
└───────────────────────────────────┬────────────────────────────────────┘
                                    │
┌───────────────────────────────────▼────────────────────────────────────┐
│ 5. Island Selection & Cross-State Arbitration                          │
│    - IslandStateManager state synchronization lock                     │
│    - Call interruption policy, user-ownership retention check          │
└───────────────────────────────────┬────────────────────────────────────┘
                                    │
┌───────────────────────────────────▼────────────────────────────────────┐
│ 6. Presentation Mapping & Compose Rendering                            │
│    - IslandPresentationMapper.resolveOverlayDemoState(state, count)    │
│    - DynamicIslandPill renders matched layout (Compact / Expanded)     │
└───────────────────────────────────┬────────────────────────────────────┘
                                    │
┌───────────────────────────────────▼────────────────────────────────────┐
│ 7. User Interaction Detection                                          │
│    - Tap / Long-press on pill (combinedClickable)                      │
│    - Vertical drag upward swipe (detectVerticalDragGestures)           │
│    - Inline reply text entry / Action button click                     │
└───────────────────────────────────┬────────────────────────────────────┘
                                    │
┌───────────────────────────────────▼────────────────────────────────────┐
│ 8. Action Execution & Dispatch                                         │
│    - NotificationActionRegistry.send(notificationId, actionId)         │
│    - NotificationActionRegistry.sendReply(context, id, actionId, text) │
│    - NotificationActionDispatch.attempt() -> PendingIntent.send()      │
└────────────────────────────────────────────────────────────────────────┘
```

---

## 2. Notification Field Processing Audit

The table below documents every platform field extracted from `StatusBarNotification` and `Notification`, its exact source code handler, and whether it is actively used, ignored, or unsupported.

| Notification Field / Extra | Extraction Code & Function | Processing Behavior | Current Status |
| :--- | :--- | :--- | :--- |
| **`sbn.packageName`** | `IslandNotificationListener.kt:247` | Used to filter own notifications (`packageName == sbn.packageName`), lookup app label, and match against `blockedNotificationPackages`. | **USED** |
| **`sbn.key`** | `IslandNotificationListener.kt:248` | Unique stable identifier for the notification lifecycle, action registry, and artwork cache. | **USED** |
| **`sbn.postTime`** | `IslandNotificationListener.kt:359` | Assigned to `NotificationInfo.timestamp` for recency ordering in `NotificationReducer`. | **USED** |
| **`sbn.isClearable`** | `IslandNotificationListener.kt:369` | Passed to `NotificationInfo.isClearable` to determine swipe dismissibility. | **USED** |
| **`sbn.groupKey`** | `IslandNotificationListener.kt:366` | Assigned to `NotificationInfo.groupKey` for grouping child notifications. | **USED** |
| **`Notification.FLAG_GROUP_SUMMARY`** | `IslandNotificationListener.kt:255` | Evaluated; empty summaries are dropped to prevent phantom notifications (`line 279`). | **USED** |
| **`Notification.FLAG_ONGOING_EVENT`** | `IslandNotificationListener.kt:256` | Flags ongoing foreground services for Live Activity consideration or silent progress updates. | **USED** |
| **`Notification.category`** | `IslandNotificationListener.kt:257, 337` | Used to classify `CATEGORY_TRANSPORT` (Media), `CATEGORY_CALL`, `CATEGORY_ALARM`, and `CATEGORY_NAVIGATION`. | **USED** |
| **`Notification.visibility`** | `IslandNotificationListener.kt:387` | Evaluated against `NotificationSensitiveContentPolicy`. If not `VISIBILITY_PUBLIC`, sensitive copy is masked and pictures suppressed. | **USED** |
| **`Notification.contentIntent`** | `IslandNotificationListener.kt:327-330` | Wrapped into a synthetic `"open"` action target in `NotificationActionRegistry`. | **USED** |
| **`Notification.actions[]`** | `IslandNotificationListener.kt:309-326` | Extracted into `NotificationActionInfo` list; `PendingIntent` references stored securely in `NotificationActionRegistry`. | **USED** |
| **`RemoteInput` (in actions)** | `IslandNotificationListener.kt:315-325` | Checks `allowFreeFormInput == true`. Flags action as `isReply = true` to enable in-island typing. | **USED** |
| **`EXTRA_TITLE`** | `NotificationContentResolver.kt:19` | Extracted as notification header / title. | **USED** |
| **`EXTRA_TEXT`** | `NotificationContentResolver.kt:20` | Extracted as compact message preview. | **USED** |
| **`EXTRA_BIG_TEXT`** | `NotificationContentResolver.kt:21` | Extracted as multi-line body for expanded view. | **USED** |
| **`EXTRA_SUB_TEXT`** | `NotificationContentResolver.kt:22` | Extracted for secondary metadata (e.g. email account or group info). | **USED** |
| **`EXTRA_SUMMARY_TEXT`** | `NotificationContentResolver.kt:23` | Fallback text for group headers. | **USED** |
| **`EXTRA_CONVERSATION_TITLE`**| `NotificationContentResolver.kt:24` | Extracted for group messaging threads. | **USED** |
| **`android.textLines` (InboxStyle)**| `NotificationContentResolver.kt:25` | Extracted into `inboxLines` list; rendered joined by newlines in expanded view. | **USED** |
| **`EXTRA_PROGRESS`, `EXTRA_PROGRESS_MAX`** | `IslandNotificationListener.kt:370-372` | Bounded progress integer extracted; drives download/upload progress bars. | **USED** |
| **`EXTRA_PROGRESS_INDETERMINATE`** | `IslandNotificationListener.kt:373` | Extracted; drives indeterminate animated progress bars. | **USED** |
| **`EXTRA_SHOW_CHRONOMETER`** | `IslandNotificationListener.kt:260` | Extracted; enables live ticking chronometers. | **USED** |
| **`android.chronometerCountDown`** | `IslandNotificationListener.kt:261` | Extracted; controls countdown vs count-up chronometer mode. | **USED** |
| **`EXTRA_LARGE_ICON_BIG` & `EXTRA_LARGE_ICON`** | `NotificationArtworkRepository.kt:186-189` | Decoded asynchronously off the main thread; cached as avatar JPEG. | **USED** |
| **`EXTRA_PICTURE` & `EXTRA_PICTURE_ICON`** | `NotificationArtworkRepository.kt:194-197` | Decoded asynchronously; cached as picture JPEG (suppressed if sensitive). | **USED** |
| **`EXTRA_MESSAGING_PERSON`** | `NotificationArtworkRepository.kt:204` | Extracted for `MessagingStyle` sender avatar icon. | **USED** |
| **`EXTRA_MEDIA_SESSION`** | `IslandNotificationListener.kt:623, 652` | Parcelable token extracted to bind to `MediaController` for metadata & playback state. | **USED** |
| **`android.callType` & `CallStyle`** | `IslandNotificationListener.kt:263` & `CallNotificationPolicy.kt` | Extracted to distinguish incoming (1) vs ongoing (2) VoIP calls. | **USED** |
| **`Notification.smallIcon`** | `IslandNotificationListener.kt` | Ignored directly as a bitmap; app identity rendered using system `PackageManager.getApplicationIcon(packageName)` in Compose `PackageIcon.kt`. | **IGNORED** |
| **`Notification.sound` & `vibrate`** | `IslandNotificationListener.kt` | Ignored; the system notification manager handles alert tones. Island does not duplicate sound playback. | **IGNORED** |
| **`Notification.tickerText`** | `IslandNotificationListener.kt` | Obsolete since Android 5.0; ignored. | **IGNORED** |
| **`Notification.contentView` (RemoteViews)**| `IslandNotificationListener.kt:578-617` | Parsed via view-tree reflection ONLY for OEM Clock chronometers; custom app layouts are otherwise unsupported. | **PARTIAL** |
| **`Notification.bubbleMetadata`** | `IslandNotificationListener.kt` | Bubble shortcut intents are not extracted or linked. | **UNSUPPORTED** |

---

## 3. Style-Specific Processing

### A. MessagingStyle (`Notification.MessagingStyle`)
- **Implemented:**
  - `EXTRA_CONVERSATION_TITLE` extracted in `NotificationContentResolver.kt:24`.
  - `EXTRA_MESSAGING_PERSON` icon extracted asynchronously in `NotificationArtworkRepository.kt:204`.
  - In `NotificationIsland.kt`, messages display sender avatar, title, and conversation body.
  - RemoteInput reply actions trigger `InlineReplyBox` directly in the island.
- **Limitation / Missing:**
  - `EXTRA_MESSAGES` (historic bundle array of multiple message parts) is not extracted as structured individual message objects; only the latest text lines or big text are processed.

### B. MediaStyle (`Notification.MediaStyle`)
- **Implemented:**
  - `EXTRA_MEDIA_SESSION` extracted in `IslandNotificationListener.kt:623`.
  - Binds to `MediaController`, extracting live `PlaybackState` (playing/paused/buffering), position, duration, track title, artist, and album art.
  - Supported actions: `MusicPrevious`, `MusicPlayPause`, `MusicNext`, and audio seek scrubber.

### C. CallStyle (`Notification.CallStyle`)
- **Implemented:**
  - `CallNotificationPolicy.kt` inspects `android.callType`, `template` (`CallStyle`), and action labels.
  - Incoming VoIP calls extract `acceptActionId` and `declineActionId` and post to `IslandStateManager.postNotificationIncomingCall`.
  - Ongoing VoIP calls extract `ongoingActionId` ("hang up" / "end call") and post to `IslandStateManager.postNotificationOngoingCall`.

---

## 4. Replay Architecture: Normalized Payloads vs. Raw StatusBarNotification

### Critical Architectural Distinction:
It is critical to distinguish between **replaying a real Android framework `StatusBarNotification`** and **replaying a normalized payload**:

1. **Replaying an actual Android `StatusBarNotification`:**
   - **NOT FEASIBLE in Pure Unit / Playground Tests:** `StatusBarNotification` is a framework class tightly coupled to `android.os.Parcel`, internal hidden fields, and the active system `NotificationListenerService`. Creating mock `StatusBarNotification` objects requires brittle mockito reflection and cannot deserialize real binary parcels cleanly across different Android API versions (e.g. Android 8 vs Android 14).
   - Furthermore, `PendingIntent.send()` from captured notifications will fail if replayed on another device or after process expiration (token expiration).

2. **Replaying a Normalized Notification Payload DTO (`RecordedNotificationPayload`):**
   - **100% FEASIBLE & ARCHITECTURALLY CLEAN:** We can serialize the extracted fields into a platform-agnostic JSON format that exercises `resolveNotificationContent()`, `CallNotificationPolicy`, `NotificationLiveActivityMapper`, and `NotificationReducer` without touching hidden framework APIs.

### The Ingestion Boundary Specification:

To enable faithful payload replay in the Playground without duplicating classification logic:

```kotlin
// Serialized fixture format (JSON)
data class RecordedNotificationPayload(
    val id: String = "recorded-01",
    val packageName: String,
    val appName: String,
    val category: String = "general",
    val title: String,
    val text: String,
    val bigText: String? = null,
    val subText: String? = null,
    val inboxLines: List<String> = emptyList(),
    val isOngoing: Boolean = false,
    val isClearable: Boolean = true,
    val progress: Int = 0,
    val progressMax: Int = 0,
    val isProgressIndeterminate: Boolean = false,
    val showChronometer: Boolean = false,
    val chronometerBaseElapsed: Long = 0L,
    val isSensitive: Boolean = false,
    val template: String = "",
    val actions: List<RecordedAction> = emptyList(),
    val mockAvatarAssetPath: String? = null,
    val mockPictureAssetPath: String? = null
)

data class RecordedAction(
    val id: String,
    val label: String,
    val isReply: Boolean = false
)
```

### Ingestion Seam Refactoring:
Extract the mapping function from `IslandNotificationListener`:
```kotlin
fun RecordedNotificationPayload.toNotificationInfo(): NotificationInfo {
    val content = resolveNotificationContent(
        appName = appName,
        title = title,
        text = text,
        bigText = bigText,
        subText = subText,
        summaryText = null,
        conversationTitle = null,
        inboxLines = inboxLines
    )
    return NotificationInfo(
        id = id,
        packageName = packageName,
        appName = appName,
        title = content.title,
        text = content.compactText,
        expandedText = content.expandedText,
        subText = content.subText,
        inboxLines = content.inboxLines,
        isPriority = category in setOf("call", "alarm", "navigation"),
        category = category,
        isOngoing = isOngoing,
        isClearable = isClearable,
        progress = progress,
        progressMax = progressMax,
        isProgressIndeterminate = isProgressIndeterminate,
        showChronometer = showChronometer,
        chronometerBaseElapsedRealtime = chronometerBaseElapsed,
        template = template,
        isSensitive = isSensitive,
        hasContentIntent = true,
        actions = actions.map { NotificationActionInfo(it.id, it.label, it.isReply) }
    )
}
```

This normalized DTO allows the Playground to load bundled JSON fixtures for:
- Food Delivery updates (Swiggy / Zomato order stages)
- Navigation turn maneuvers (Google Maps turn-by-turn)
- Group messaging threads with multi-line inbox styles (WhatsApp)
- Media playback tracks
- VoIP calls with Accept/Decline actions
- Sensitive banking/OTP notifications with masked previews

All of these will run through the **exact production classification logic** (`NotificationLiveActivityMapper.map(info)`, `NotificationEventPolicy.evaluate(info)`, and `NotificationReducer.reduce()`).
