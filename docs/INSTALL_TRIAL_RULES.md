# Aurora Dynamic Island — 7-Day Installation Trial Rules & Lifecycle

## 1. Feature Overview
Aurora Dynamic Island provides every new user with an automated, friction-free **7-day full-access trial** immediately upon initial installation. 

No credit card, account registration, or upfront commitment is required.

---

## 2. Trial Duration & Remote Configuration
The default trial duration is **7 days** (168 hours). The duration can be dynamically updated via Firebase Remote Config:

- **Key**: `trial_duration_days` or `premium_install_trial_days`
- **Default Value**: `7`
- **Type**: `Integer`
- **Bounds**: `1` to `30` days

When the app fetches Remote Config via `ReleaseRuntimeConfigProvider`, any custom duration received before the user's initial trial initialization is applied to `trialDurationDays`.

---

## 3. Clock Rollback & Anti-Tampering Engine
To protect product revenue and maintain fair trial enforcement, `TrialManager` implements a monotonic time tracking algorithm:

1. **Monotonic High-Water Mark**: The app maintains `lastObservedTimestamp` in DataStore.
2. **Launch & Resume Validation**: On every startup, `System.currentTimeMillis()` is compared with `lastObservedTimestamp`.
3. **Rollback Detection**: If `currentTimeMillis < lastObservedTimestamp - 5 minutes`:
   - `TrialSnapshot.isTampered` is flagged `true`.
   - The trial expiration timestamp is clamped to prevent artificial extension.
4. **Unidirectional Advance**: `lastObservedTimestamp = maxOf(currentTimeMillis, lastObservedTimestamp)`.
5. **Reboot Survival**: Because timestamps are stored as absolute epoch milliseconds in persistent DataStore, device restarts and power cycles do not reset or corrupt the trial countdown.

---

## 4. Trial Lifecycle & State Machine
```
   [ App First Launch ]
            │
            ▼
   [ Check DataStore: startTime == 0L ]
            │
      ┌─────┴────────────────┐
      ▼                      ▼
  [ New Install ]        [ Existing Install ]
      │                      │
      ├─ Record startTime    ├─ Read startTime & expiresAt
      ├─ Calculate expiresAt ├─ Verify clock integrity
      └─ Set status = ACTIVE └─ Evaluate: now < expiresAt?
                                     │
                             ┌───────┴───────┐
                             ▼               ▼
                        [ ACTIVE ]      [ EXPIRED ]
                             │               │
                             │               ├─ Fallback to Free Tier
                             │               ├─ Prompt Rewarded Pass (+24h)
                             │               └─ Surface Expiry Island on tap
                             ▼
              [ Full Aurora Pro Features Unlocked ]
```

---

## 5. UI Presentation Contract
During an active 7-day trial:
1. **Home Screen**: `HomePremiumStatusCard` displays:
   - "7-Day Trial Active"
   - Countdown badge: `"X days left"`
   - Quick action button: `"Watch Ad (+1 Day)"` (allows users to stack bonus days onto their trial)
   - Secondary action button: `"Keep Pro"` (opens paywall to secure permanent Pro)
2. **Settings Screen**: The Membership card displays `"7-Day Trial Active"` with status pill `"Trial · Xd"`.
3. **Paywall**: Displays clear comparison showing which features will lock when the trial expires, with instant upgrade buttons.
