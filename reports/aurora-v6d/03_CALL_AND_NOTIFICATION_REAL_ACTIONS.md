# Aurora V6D Deliverable 03: Call and Notification Real Actions

## 1. Call Lifecycle State Machine & Control Differentiation

The Aurora Dynamic Island distinguishes every phase of the telephony lifecycle. Each phase features purposeful controls, geometry, typography, and color schemes:

```
[ Incoming / Ringing ]
        │
        ▼ (Answered)
[ Connected Compact / Banner ] ──► [ Connected Expanded ]
        │                                  │
        └──────────────┬───────────────────┘
                       ▼ (Ended / Missed)
             [ Call Summary Ended ]
                       │
                       ▼ (Dismiss / Redial)
             [ Previous State Restored ]
```

### 1.1 Incoming / Ringing Call (`CallAvatars` / `IncomingCallIsland`)
- **Visual Surface**: 367×86dp capsule (R43), matching Figma `Dynamic Island-6.svg`.
- **Top / Identity**: 56dp circular avatar with breathing pulse (`rememberBreathing(0.96f, 1.03f)`), caller label ("iPhone" / "Mobile", 13sp `#A4A4A9`), and caller name (16sp SemiBold white).
- **Controls**: Trailing pair of circular 52dp action buttons:
  - **Decline**: Destructive Red (`#FA3532`) with `AppleGlyph.EndCall` (26dp Figma ink size).
  - **Accept**: Positive Green (`#37C058` / `IslandColors.CapsuleGreen`) with `AppleGlyph.Phone` (22dp Figma ink size).
- **Behavioral Guarantee**: Never displays duration, "Redial", or "Done" during ringing.

### 1.2 Connected Call Compact (`CallCompact`)
- **Visual Surface**: Canonical 36.67dp compact capsule.
- **START Slot**: 12dp green phone glyph + running elapsed timer in monospace tabular figures (`IslandColors.Green`).
- **END Slot / Companion**: Audio waveform bubble when companion island is present.

### 1.3 Connected Call Expanded (`CallExpanded`, `FaceTimeCallExpanded`, `SharedMediaCallExpanded`)
- **Visual Surface**: 367×166dp expanded sheet (R42), matching `Dynamic Island-14.svg`.
- **Top / Identity**: 56dp avatar, caller name (16sp), subtitle with audio routing ("Mobile Call"), info glyph.
- **Action Grid**: Circular 44dp controls in 48dp accessible touch targets:
  - **Speakerphone**: Active: White background + Black glyph; Inactive: `#2C2C2E` glass + White glyph. Driven by real platform audio state.
  - **Microphone / Mute**: Active: White background + Black glyph; Inactive: `#2C2C2E` glass + White glyph. Driven by `isMicrophoneMute`.
  - **Camera / Video**: (FaceTime/VoIP calls only) Hardware toggle.
  - **SharePlay**: (Supported VoIP calls only) Hardware toggle.
  - **End Call**: Solid Apple Red (`#FA3532`) with `AppleGlyph.EndCall`.

### 1.4 Call Summary / Ended Call (`CallSummaryExpanded`)
- **Visual Surface**: 367×112dp capsule (R42), content-aware height.
- **Top Row**: 40dp contact avatar START; caller name (15sp SemiBold white); phase label (`callSummaryPhaseLabel`, e.g. "Call ended · Incoming", "Outgoing call ended", "Missed call"); neutral static duration END (`callSummaryDurationLabel`, neutral text color, NOT green).
- **Action Hierarchy Overhaul**:
  - **P0 Fix**: Completely eliminated the cramped 3-button grey pill row (`Redial weight(1f)`, `SMS weight(.7f)`, `Done weight(.8f)`).
  - **Primary Action**: Prominent Redial / "Call back" (if missed) button (`IslandActionStyle.Primary`, `#0A84FF` Apple Vibrant Blue, 18dp Phone glyph, 38dp visual height, 48dp touch region).
  - **Tertiary Action**: Quiet "Done" dismiss button (`IslandActionStyle.Tertiary`, `#1C1C1E` dark glass, 16dp Close glyph, 38dp visual height, 48dp touch region).
  - **Fallthrough**: When phone number is unavailable or dialing is unsupported, "Done" gracefully expands to a single clean dismiss action.

---

## 2. Platform Capability Detection (`IslandActionCapability`)

In previous iterations, intent checks fell back to `.getOrDefault(true)`, which created ghost buttons when phone numbers or system intents could not be handled. In V6D:
- **`IslandActionCapability.canDial`**: Validates non-null string, verifies existence of digits, and queries package manager with `.getOrDefault(false)`.
- **`IslandActionCapability.canSendSms`**: Validates non-null string, verifies digits, and queries `smsto:` intent with `.getOrDefault(false)`.
- **`IslandActionCapability.canOpenSettings`**: Queries system settings intents safely with `.getOrDefault(false)`.
- **Demo / Preview Isolation**: `isDemoPreview = true` explicitly supplies canonical demonstration data without altering production capability checks.

---

## 3. Privacy & Lock-Screen Protection

Under `LockScreenPrivacyPolicy.callRecord`:
1. **Device Locked (`deviceLocked = true`)**:
   - `contactName` is redacted to generic state: "Incoming call", "Outgoing call", or "Missed call".
   - `phoneNumber` is wiped to empty string `""`.
   - `avatarUri` is wiped to `null`.
   - `durationSeconds` is preserved so the user understands call duration without sensitive identity leaks.
2. **Device Unlocked (`deviceLocked = false`)**:
   - Authentic caller name, phone number, and avatar URI are fully restored.

---

## 4. Notification Action Policies

1. **Source-Backed Only**:
   - Only actions explicitly posted in `Notification.actions` are rendered. The island never manufactures synthetic actions (e.g. fake "Mark as read" or fake "Like").
2. **Reply Action**:
   - Uses `IslandActionStyle.Primary` with `#0A84FF` background and `AppleGlyph.Chat`.
   - Opening inline reply launches `InlineReplyBox` with automatic focus requester and persistent draft storage (`LocalIslandReplyDraftStore`).
3. **Destructive Actions**:
   - "Dismiss", "Delete", "Cancel" actions render with `IslandActionStyle.Destructive` (`#33FA3532` background, `#FFFF453A` text, `AppleGlyph.Close`).
4. **No-Action Notifications**:
   - Notifications without actions render a clean content card with 0dp reserved action space.
