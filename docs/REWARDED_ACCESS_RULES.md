# Aurora Dynamic Island — Rewarded Ad Access Rules & Stacking Policy

## 1. Executive Summary
Aurora Dynamic Island empowers non-paying users to enjoy premium features through an ethical, non-intrusive value exchange: **1 completed rewarded ad grants 24 hours of full Aurora Pro access**.

Rewarded passes are completely stackable, protected by a 15-second cooldown timer, and visually gamified via a 7-day calendar checkmark progress system.

---

## 2. Core Mechanics & Mathematical Contract
1. **Pass Duration**: Exactly 24 hours (86,400,000 milliseconds) per completed rewarded ad.
2. **Stackability**:
   - If a pass is currently active: `newExpiry = currentExpiry + 24 hours`.
   - If no pass is active: `newExpiry = System.currentTimeMillis() + 24 hours`.
3. **15-Second Cooldown Timer**:
   - Between consecutive ad viewings, a 15-second cooldown is enforced:
     `canWatch = (System.currentTimeMillis() - lastAdWatchTimestamp) >= 15,000L`.
   - The "Watch Ad" button shows live ticking countdown: `"Please wait (15s)..."` until elapsed.
4. **Calendar Checkmarks (7-Day Progress)**:
   - Progress is persisted as a JSON map of dates (`"yyyy-MM-dd": true`) in `KEY_AD_REWARD_DAYS_JSON`.
   - Watching an ad checks off the next consecutive day in the current 7-day window.
   - Outdated dates older than 7 days are automatically pruned during snapshot recalculation.

---

## 3. AdMob Integration & ILAR Reporting
- **Format**: Google Mobile Ads Next-Gen Rewarded Video Ad (`RewardItem`).
- **Impression-Level Ad Revenue (ILAR)**:
  - Every rewarded ad impression reports real-time revenue to GA4 via `AdMobRevenueTracker.trackAdRevenue`:
    - Event: `ad_impression`
    - Parameters: `ad_platform`, `ad_source`, `ad_unit_name`, `currency`, `value`.
- **Preloading & Caching**: Rewarded ads are preloaded in the background with a 4-hour cache expiry to ensure instantaneous presentation upon user tap.

---

## 4. UI/UX Contract (`AdRewardDialog`)
The rewarded pass interface is rendered as a modal bottom sheet containing:
1. **Header**: "Watch Ads to Unlock Premium" with a 24-hour value callout.
2. **7-Day Progress Track**:
   - 7 circular date nodes showing abbreviation and date number (e.g. `Oct 17`).
   - Active/completed days render an Aurora cyan/purple gradient with an Apple-style checkmark icon.
   - Locked days render a dashed subtle border.
3. **Watch Ad Button**:
   - Primary high-contrast button: `"Watch Ad to Unlock (+24h)"`.
   - When in cooldown: disabled, displaying `"Please wait (Xs)..."`.
4. **Instant Celebration Feedback**:
   - Upon reward grant, a celebration overlay displays a checkmark animation with `"Access Extended by 24h!"`.
5. **Direct Lifetime Pro Upgrade Link**:
   - Below the button: `"Or unlock lifetime Pro without ads →"` navigating directly to `PremiumScreen`.

---

## 5. Non-Negotiable Quarantine Policy
> [!CAUTION]
> **Strict Zero-Ad Dynamic Island Overlay Policy**
> Under no circumstance may any banner, interstitial, or rewarded advertisement appear inside or directly attached to `IslandOverlayService`. Overlays must remain 100% advertising-free across all tiers.
