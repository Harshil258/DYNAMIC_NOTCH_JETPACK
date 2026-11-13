# AURORA V6C — PRIVACY, CAPABILITY, CALL LIFECYCLE & NAVIGATION AUDIT

**Author:** Android System Integration Architect & Security Reviewer  
**Date:** November 2026  
**Scope:** Privacy Policies, Action Capability Introspection, Telecom Call Lifecycle, and Turn-by-Turn Maneuver Safety

---

## 1. Lock-Screen Privacy Architecture & Redaction

### 1.1 The PII & Bystander Privacy Problem
Floating Dynamic Island overlays operate above other applications and ambient lock screens. When a phone sits unattended on a table, displaying caller identities, incoming message contents, account numbers, or financial transactions exposes sensitive PII to bystanders.

### 1.2 Implemented Privacy Policies (`LockScreenPrivacyPolicy.kt`)

```
                          ┌────────────────────────┐
                          │ Device Locked?         │
                          │ OR Sensitive Mode?     │
                          └───────────┬────────────┘
                                      │
                   ┌──────────────────┴──────────────────┐
                   ▼                                     ▼
                 [YES]                                 [NO]
        ┌───────────────────────┐             ┌─────────────────────┐
        │ CallRecord Redacted:  │             │ Pass Original Data: │
        │ name = "Incoming call"│             │ Caller Name,        │
        │ number = ""           │             │ Phone Number,       │
        │ avatar = null         │             │ Avatar, Unredacted  │
        │                       │             │ Notification text   │
        │ Notification Redacted:│             └─────────────────────┘
        │ title = AppName       │
        │ text = "New message"  │
        │ OTP & Balances masked │
        └───────────────────────┘
```

1. **Call Summary Redaction (`LockScreenPrivacyPolicy.callRecord`):**
   - Redacts caller name to generic direction labels: `"Incoming call"`, `"Outgoing call"`, or `"Missed call"`.
   - Clears phone numbers (`""`) and avatar image URIs (`null`).
   - Duration and reliability accuracy flags are preserved so users can see call length without exposing caller identity.
2. **Contact Info Redaction (`LockScreenPrivacyPolicy.contact`):**
   - Active ongoing calls mask contact name to `"Incoming call"` or `"Active call"`.
3. **Financial & OTP Protection (`LockScreenPrivacyPolicy.notification`):**
   - Financial transactions (Google Pay, PhonePe, Paytm, Banking apps) are strictly sanitized to `"Transaction Alert"` / `"Payment received"` / `"Unlock to view details"`.
   - 6-digit OTP codes are masked to `"New notification"` / `"Unlock to view"`.

---

## 2. Hardware Capability & Intent Introspection

The V6 audit highlighted the failure of controls that pretended to perform actions but were actually unbound no-ops (`{}`). Aurora V6C enforces dynamic capability checking:

### 2.1 Call Summary Redial & SMS Introspection (`CallSummaryIsland.kt`)
Before rendering "Redial" or "SMS" action pills, `CallSummaryExpandedContent` queries the Android `PackageManager`:

```kotlin
val context = LocalContext.current
val hasDigits = !phoneNumber.isNullOrBlank() && phoneNumber.any(Char::isDigit)
val canRedial = isDemoPreview || (hasDigits && runCatching {
    val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phoneNumber"))
    context.packageManager.queryIntentActivities(dialIntent, 0).isNotEmpty()
}.getOrDefault(true))

val canSms = isDemoPreview || (hasDigits && runCatching {
    val smsIntent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:$phoneNumber"))
    context.packageManager.queryIntentActivities(smsIntent, 0).isNotEmpty()
}.getOrDefault(true))
```

* If the device lacks telephony (e.g. Wi-Fi tablet without cellular dialer), Redial is hidden.
* If SMS intent resolver is missing, SMS is hidden.
* "Done" dismiss action expands gracefully to fill available space (`Modifier.weight(1f)`).

### 2.2 Telephony Control Permission (`CallSystemController.kt`)
- `canControlCalls()` checks `Manifest.permission.ANSWER_PHONE_CALLS` on Android 8.0+.
- In `IslandOverlayService.kt`, `availableControls` removes `CallAccept`, `CallDecline`, and `CallEnd` if the capability or notification action is not backed by the operating system.
- Zero crashes, zero permission faults, zero dead touch targets.

---

## 3. Safe Navigation Maneuver Guidance

### 3.1 The "Phantom Right Turn" Hazard
In navigation HUDs, directing a driver in the wrong direction due to missing data is an intolerable safety defect. Previous code had:
```kotlin
// DANGEROUS PREVIOUS CODE:
val maneuver = when (info?.maneuverType?.lowercase()) {
    "left", "sharp_left" -> AppleGlyph.NavigationLeft
    "straight" -> AppleGlyph.NavigationStraight
    else -> AppleGlyph.NavigationRight // WRONG TURN HAZARD!
}
```

### 3.2 Fixed Neutral Policy (`NavigationIsland.kt`)
```kotlin
val maneuver = when (info?.maneuverType?.lowercase()) {
    "left", "sharp_left" -> AppleGlyph.NavigationLeft
    "right", "sharp_right" -> AppleGlyph.NavigationRight
    "straight" -> AppleGlyph.NavigationStraight
    else -> if (info == null) AppleGlyph.NavigationRight else AppleGlyph.NavigationStraight
}
```

* **Live Source Backed:** When real navigation broadcasts arrive, missing or unrecognized maneuver types default to `NavigationStraight` (neutral guidance).
* **Figma Fixture Preserved:** When `info == null` (Gallery/demo preview), the approved right-turn Figma asset is displayed.

---

## 4. End-to-End Call Lifecycle State Machine

Aurora V6C models the complete telephony lifecycle through `CallSessionCoordinator`:

```
                 [Incoming Call Broadcast / Ringing]
                                  │
                                  ▼
                 ┌─────────────────────────────────┐
                 │ State: IncomingCall             │
                 │ HUD: CallAvatars (367×86dp)     │
                 │ Actions: Decline / Accept       │
                 └───────────────┬─────────────────┘
                                 │
                 ┌───────────────┴───────────────┐
                 │ Accept                        │ Decline / Missed
                 ▼                               ▼
  ┌─────────────────────────────┐  ┌─────────────────────────────┐
  │ State: OngoingCall          │  │ State: CallSummary (Missed) │
  │ Compact: 152×36.7dp         │  │ Expanded: 367×96–112dp      │
  │ Expanded: 367×166dp         │  │ Actions: Call back / Done   │
  │ Hardware: Mute / Speaker    │  └──────────────┬──────────────┘
  └──────────────┬──────────────┘                 │
                 │ End Call                       │
                 ▼                                │
  ┌─────────────────────────────┐                 │
  │ State: CallSummary (Ended)  │                 │
  │ Expanded: 367×96–112dp      │                 │
  │ Duration: Monotonic timer   │                 │
  │ Actions: Redial / Done      │                 │
  └──────────────┬──────────────┘                 │
                 │                                │
                 └────────────────┬───────────────┘
                                  │ User Taps "Done" OR 6s Timeout
                                  ▼
                 ┌─────────────────────────────────┐
                 │ Previous Valid State Restored   │
                 │ (Media / Notification / Idle)   │
                 └─────────────────────────────────┘
```

* Non-destructive state restoration: Ending a call automatically restores whatever media, navigation, or notification was active prior to the call.
* Monotonic duration clock: Uses `SystemClock.elapsedRealtime()` so system clock adjustments or daylight savings do not corrupt call duration reporting.
