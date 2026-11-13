# Aurora V6D — Screenshot-to-Code Visual Defect Audit

**Current screenshot (user):** 60-state Gallery, CallSummaryExpanded. Person, completed call duration, redial/SMS/Done controls all present, but the controls look grey, compressed and visually equal.

**Quality reference (user):** SMART ACTIONS marketing visual with a ringing-call action pair, a WhatsApp notification action pair, a media/video card and navigation. This is an **illustration of professional visual hierarchy**, not a direct measured Figma component or a guarantee Android supports all depicted app actions.

## Source-confirmed issues in V6C review branch (`30ed4e9`)

| Issue | Source | What the user sees | Required correction |
|---|---|---|---|
| Three equal-ish Call Summary pills | `CallSummaryIsland.kt`, Redial `weight(1f)`, SMS `weight(.7f)`, Done `weight(.8f)` | Crowded, flat, disabled-looking row | One meaningful primary Redial/Call-back if real + subtle Done; other optional actions behind a proper secondary affordance if necessary |
| Overly tiny icons | `CallSummaryActionButton`: `AppleIcon(... size = 13.dp)` | Small icon compared with text and action surface | Optical 17–20dp functional icons, calibrated per Figma; consistent stroke/solid weight |
| Low-contrast button treatment | `#222B3A`, `IslandColors.Gray6`, `IslandColors.TextSecondary` | Weak foreground/background differentiation | Intent-based primary/secondary/positive/destructive/tertiary styles with readable enabled states |
| Fixed 36dp visual action inside 48dp wrapper | `.height(36.dp)` and `defaultMinSize(minHeight=48.dp)` | Visually thin pill, generous invisible area that may crowd overlay | Family-specific 36–42dp visual and 48dp logical hit region without unsafe window capture |
| Capability fallback treats errors as available | `queryIntentActivities` under `runCatching` with `.getOrDefault(true)` | May present button whose handling is not safely verified | Use conservative action-capability policy and real intent handling with safe failure disclosure |
| Hardcoded demo ticket/card details | `MovieReferenceContent` uses `Evening screening`, `Cinema 2 · 19:30`, `Seat G12` | Fixture may look like real ticket if leaked | Explicit fixture-only data, production source-backed payload and gating |
| Equal visual priority across contexts | Other state-specific composables use independent button styles | Inconsistent visual family, 'entry-level' UI | Shared action tokens and state-specific exceptions, not generic copied rows |

## Reference interpretation

- **Incoming call**: two prominent red/green circular controls at the trailing side; these control real ringing lifecycle. Do not show them in a completed-call summary.
- **WhatsApp notification**: a high-contrast, app-aware action row with real Reply and conditional read/other source actions. Do not invent a `Mark as read` API for generic notifications.
- **TikTok/media**: playback/open/like glyph language demonstrates icon consistency and hierarchy. Only expose actions posted by the app or media session—never add fake Like or Pause.
- **Navigation**: no noisy default CTA; leading maneuver and strong destination/time communicate enough.
- **Black capsules and accents**: selective green/red/blue/pink based on action semantics; not a rainbow outline on every state.

## Non-regression requirements

The revised primitives must preserve exact direct-Figma reference geometry where applicable; fix internal optical composition without moving outer shell unnecessarily. For derived `NO_SOURCE` states, height should be content-aware with no permanent blank filler. Keep 60 states audited; preserve notification/multi-island/call/privacy behavior.

## How to evaluate independently

Compare settled state on hardware against source reference (for DIRECT) or family-approved composition (for derived), at matched viewport. Record action capabilities, visual bounds (outer shell and internal components), text/icon optical alignment, contrast, touch target and tap result. An outer size delta green label is not visual acceptance.
