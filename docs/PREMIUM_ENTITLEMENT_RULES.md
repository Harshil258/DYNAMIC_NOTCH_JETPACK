# Aurora Dynamic Island — Premium Entitlement Rules & Hierarchy

## 1. Executive Summary & Purpose
This document establishes the permanent architectural standard, tier precedence, tampering mitigation, and single source of truth for the Aurora Dynamic Island entitlement engine (`EntitlementManager`).

Entitlements control feature availability and advertising behavior across all app layers, including floating overlay services, background monitors, settings interfaces, and paywalls.

---

## 2. Entitlement Hierarchy & Priority Order
Aurora Dynamic Island implements a **7-tier entitlement hierarchy** resolved through strict order of precedence:

```
[ Tier 1: LIFETIME ]  ➔ Highest Priority (Non-expiring permanent Pro & zero ads)
        ▼
[ Tier 2: YEARLY ]    ➔ Active recurring Play subscription (Pro & zero ads)
        ▼
[ Tier 3: MONTHLY ]   ➔ Active recurring Play subscription (Pro & zero ads)
        ▼
[ Tier 4: WEEKLY ]    ➔ Active recurring Play subscription (Pro & zero ads)
        ▼
[ Tier 5: TEMPORARY REWARDED ACCESS / INSTALL TRIAL ]
        ├─ If both active, latest expiration date takes precedence
        ├─ Unlocks all Pro island features & customization
        └─ Retains standard AdMob ad placement rules (unless paid)
        ▼
[ Tier 6: FREE ]      ➔ Base tier: full notch alignment, basic notifications,
                         music playback, charging, Bluetooth, standard AdMob ads
```

### Precedence Resolution Matrix:
| Entitlement Source | `isPro` | `isAdFree` | Expiry Policy | Priority Rank |
| :--- | :---: | :---: | :--- | :---: |
| `LIFETIME` | **Yes** | **Yes** | `null` (Permanent / Non-expiring) | **1** |
| `YEARLY` | **Yes** | **Yes** | Timestamp from Google Play Billing | **2** |
| `MONTHLY` | **Yes** | **Yes** | Timestamp from Google Play Billing | **3** |
| `WEEKLY` | **Yes** | **Yes** | Timestamp from Google Play Billing | **4** |
| `REWARDED_ACCESS` | **Yes** | **No** | Stackable (+24h/ad), stored in DataStore | **5a** |
| `INSTALL_TRIAL` | **Yes** | **No** | 7-day initial clock rollback protected | **5b** |
| `FREE` | **No** | **No** | Permanent fallback | **6** |

---

## 3. DataStore Schema & Persistence
Entitlements are persisted locally using Jetpack DataStore (`auroraDataStore`):

- `KEY_PRO_ACTIVE` (`Boolean`): Set to `true` when a valid Google Play purchase (Weekly, Monthly, Yearly, Lifetime) is verified.
- `KEY_PRO_EXPIRES_AT` (`Long`): Unix epoch in milliseconds for subscription expiration. Set to `0` or `null` for Lifetime purchases.
- `KEY_PRO_SOURCE` (`String`): Identifier of the plan purchased (`"weekly"`, `"monthly"`, `"yearly"`, `"lifetime"`).
- `KEY_TRIAL_START_TIME` (`Long`): Timestamp of first app initialization.
- `KEY_TRIAL_EXPIRES_AT` (`Long`): Timestamp when the 7-day trial expires.
- `KEY_TRIAL_DURATION_DAYS` (`Int`): Remote Config configurable duration (default `7`).
- `KEY_LAST_OBSERVED_TIMESTAMP` (`Long`): Monotonically increasing clock record for rollback detection.
- `KEY_REWARD_PREMIUM_EXPIRY` (`Long`): Stackable expiry for rewarded ad passes.
- `KEY_LAST_AD_WATCH_TIMESTAMP` (`Long`): Timestamp of most recent rewarded ad completion (for 15s cooldown).
- `KEY_AD_REWARD_DAYS_JSON` (`String`): JSON dictionary representing 7-day calendar checkmark progress.

---

## 4. Clock Rollback & Tampering Mitigation
To prevent users from rolling back their system clock to prolong trials or reward passes, `TrialManager` enforces monotonic timestamp validation:

1. On every app launch and foreground transition, `System.currentTimeMillis()` is compared against `KEY_LAST_OBSERVED_TIMESTAMP`.
2. If `currentTimeMillis < lastObservedTimestamp - 5 minutes`, clock tampering is detected.
3. The trial expiration is clamped or invalidated to prevent fraudulent extensions.
4. `lastObservedTimestamp` only updates forward: `maxOf(currentTimeMillis, lastObservedTimestamp)`.

---

## 5. Architectural Contract for Future Agents
1. **Never create local `isPro` variables**: Every composable, service, or policy must derive Pro status from `EntitlementManager.getInstance(context).entitlement` or `AuroraPreferences.isProActive`.
2. **Never gate universal free features**: Basic notch alignment, manual position adjustments, notification banners, charging animations, and privacy controls must remain 100% accessible to free users.
3. **Zero overlay advertisements**: Ads are strictly quarantined within in-app activities and sheets. Under no circumstances may an ad appear inside `IslandOverlayService`.
