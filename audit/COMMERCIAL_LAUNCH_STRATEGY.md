# AURORA DYNAMIC ISLAND — COMMERCIAL LAUNCH STRATEGY
## Monetization Architecture, Pricing Economics, Funnels & Retention Playbook

**Product:** Aurora Dynamic Island  
**Publisher:** Surya Developer (`suryadevelopers07@gmail.com`)  
**Target Markets:** Primary: India (Tier-3 Domestic); Secondary: US, UK, Europe (Tier-1 Global)  
**Document Version:** 1.0 (Release Readiness Commercial Strategy)

---

## 1. Executive Summary & Dual Monetization Engine

Aurora Dynamic Island operates a hybrid commercial model combining **Google Play Subscriptions / Lifetime purchases** with **AdMob Next-Gen Programmatic Advertising**, governed by the following non-negotiable principle:

> **Zero Overlay Monetization:** Advertisements are strictly prohibited in the floating Dynamic Island overlay, during active telephone calls, over third-party apps, or on the lock screen. In-app ads are restricted to non-intrusive container surfaces (Settings, Feature Discovery, Dialogs) and are **100% suppressed upon Paid Pro activation**.

```
                           ┌──────────────────────────┐
                           │      New App Install     │
                           └─────────────┬────────────┘
                                         │
                                         ▼
                           ┌──────────────────────────┐
                           │ 7-Day Automatic Trial    │
                           │  - All Pro Features ON   │
                           │  - In-App Ads Visible    │
                           └─────────────┬────────────┘
                                         │
                     ┌───────────────────┴───────────────────┐
                     ▼                                       ▼
       ┌──────────────────────────┐            ┌──────────────────────────┐
       │     Converted to Pro     │            │      Trial Expired       │
       │  - Subscriptions / Life  │            │  - Essential Island Free │
       │  - ZERO ADS (100% Pure)  │            └─────────────┬────────────┘
       └──────────────────────────┘                          │
                                             ┌───────────────┴───────────────┐
                                             ▼                               ▼
                               ┌──────────────────────────┐    ┌──────────────────────────┐
                               │ Rewarded Pass (+24h/ad)  │    │      Free Baseline       │
                               │  - 15s cooldown          │    │  - Banner / Native Ads   │
                               │  - High eCPM engagement  │    │  - Interstitial on Nav   │
                               └──────────────────────────┘    └──────────────────────────┘
```

---

## 2. Market Segmentation & Pricing Architecture

### A. India Domestic Pricing Catalog:
| Plan Tier | Store Identifier | Price (INR) | Effective Monthly | Commercial Positioning |
| :--- | :--- | :---: | :---: | :--- |
| **Weekly** | `dynamic_island_weekly` | **₹39** | ₹156 | Low barrier testing price |
| **Monthly** | `dynamic_island_monthly` | **₹59** | ₹59 | Short-term access |
| **Yearly** | `dynamic_island_yearly` | **₹99** | **₹8.25** | **"Most Popular" Value Anchor (Featured in UI)** |
| **Lifetime** | `dynamic_island_lifetime` | **₹149** | N/A | High-conversion impulse purchase |

### B. Strategic Defense of the ₹149 Lifetime Price Point (`AUR-P1-013`):
The audit noted that ₹149 is only 1.5x the annual price of ₹99. However, in the Indian Android utility market:
1. **Recurring Mandate Friction:** Over 70% of Indian Android users do not have active recurring auto-debit mandates on UPI / Google Play, leading to renewal failure rates exceeding 90% after Month 1.
2. **Impulse Micropayments:** A ₹149 one-time payment falls squarely within the sub-₹200 impulse threshold (comparable to a single cinema beverage or quick meal).
3. **Cannibalization vs Realization:** Capturing a guaranteed ₹107 net from ₹149 is vastly superior to capturing ₹0 from a user who declines an annual subscription due to recurring commitment hesitation.
4. **Competitor Anchoring:** Competitors such as dynamicSpot and Dynamic Notch price their non-consumable licenses between ₹120 and ₹180 in India. A ₹149 price point maximizes paid conversion velocity.

### C. Tier-1 Global Catalog (USD):
For Tier-1 English-speaking markets (US, UK, CA, AU):
- Weekly: **$1.99**
- Monthly: **$3.99**
- Yearly: **$14.99** (Anchored as Best Value)
- Lifetime: **$29.99** (Ensures non-cannibalizing 2.0x multiple over annual)

---

## 3. Advertising Architecture & Placement Inventory

| Ad Surface / Format | Trigger & Lifecycle | Cooldown & Capping | Ad-Free Behavior | ILAR Tracking |
| :--- | :--- | :--- | :---: | :---: |
| **App Open Ad** | App returns to foreground after backgrounding | Min 4 hours between impressions; skipped on cold launch | Suppressed on Paid Pro | Real-time `ad_impression` via GA4 |
| **Adaptive Banner** | Pinned to bottom of Settings Vault & Feature sub-screens | Managed by AdMob adaptive refresh (30-60s) | Suppressed on Paid Pro | Real-time `ad_impression` via GA4 |
| **Native Ad Card** | Integrated into Settings list and Exit dialog | Single-use registration per scroll view | Suppressed on Paid Pro | Real-time `ad_impression` via GA4 |
| **Interstitial Ad** | Tab navigation transitions (Features ↔ Live) | Min 3 screen navigations; 6-second loading timeout | Suppressed on Paid Pro | Real-time `ad_impression` via GA4 |
| **Rewarded Video** | User taps "Watch Ad (+1 Day)" / Expiry Island | 15-second cooldown; stackable (+24h/view) | N/A (User opted-in) | Real-time `ad_impression` via GA4 |

---

## 4. Financial Unit Economics & Monetization Modeling

### Baseline Metrics & Industry Benchmarks:
- **India eCPMs:** Rewarded Video: \$1.20–\$1.80 | Interstitial: \$0.80–\$1.20 | Native: \$0.30–\$0.50 | Banner: \$0.12–\$0.20
- **Tier-1 eCPMs:** Rewarded Video: \$16.00–\$24.00 | Interstitial: \$10.00–\$15.00 | Native: \$3.50–\$6.00 | Banner: \$1.50–\$2.50
- **Store Fees:** Google Play 15% tier (under \$1M/year) + applicable local taxes (18% GST in India).

### Revenue Scenarios per 10,000 Active Installs (30-Day Window):

| Revenue Component | Conservative (100% India Traffic) | Moderate (85% India, 15% Global) | Optimistic (70% India, 30% Global) |
| :--- | :---: | :---: | :---: |
| **Paid Conversions (Sub + Life)** | 1.2% (120 users @ ₹149 avg) = **₹17,880** | 2.5% (250 users @ ₹210 blended) = **₹52,500** | 4.0% (400 users @ ₹320 blended) = **₹128,000** |
| **Rewarded Ad Passes** | 25,000 impressions @ \$1.20 eCPM = **₹2,500** | 35,000 impressions @ \$3.50 eCPM = **₹10,200** | 50,000 impressions @ \$6.80 eCPM = **₹28,500** |
| **In-App Display (Banner/Native/Inter)**| 120,000 imp @ \$0.40 eCPM = **₹4,000** | 150,000 imp @ \$1.80 eCPM = **₹22,500** | 200,000 imp @ \$3.20 eCPM = **₹53,700** |
| **Gross Monthly Revenue** | **₹24,380** | **₹85,200** | **₹210,200** |
| **Net Developer Earnings (Post-Store)** | **₹17,550** | **₹61,340** | **₹151,340** |

---

## 5. Practical Roadmap to Revenue Milestones

### Milestone 1: First ₹1,000 Revenue (Target: Day 1–7)
- **Required Installs:** ~400–600 organic installs.
- **Formula:** 7 Lifetime sales @ ₹149 = ₹1,043 gross.
- **Execution:** Enable 7-Day trial immediately on install. Feature the interactive Dynamic Island preview on the Home screen to demonstrate high perceived value. Highlight the ₹99 annual and ₹149 lifetime plans in onboarding.

### Milestone 2: First ₹5,000 Revenue (Target: Day 14–21)
- **Required Installs:** ~2,000–2,500 installs.
- **Formula:** 25 Lifetime sales (₹3,725) + 12 Annual subscriptions (₹1,188) + AdMob ad impressions (₹350) = ₹5,263.
- **Execution:** Trigger post-call summary actions and media playback HUD to establish daily habits. When the 7-day trial expires, surface the Expiry Island with 1-tap options ("Watch Ad" vs "Keep Pro").

### Milestone 3: First ₹10,000 Revenue (Target: Day 30)
- **Required Installs:** ~4,500–5,000 installs.
- **Formula:** 55 Lifetime sales (₹8,195) + 20 Annual subscriptions (₹1,980) + AdMob ILAR (₹1,200) = ₹11,375.
- **Execution:** Encourage happy users to share the app via the "Share the app" button in Settings. Maintain high crash-free rates (>99.5%) through rate-limited Crashlytics error monitoring.

---

## 6. Retention & Engagement Playbook

| User Journey Phase | Critical Retention Trigger | Experience Delivered | Engagement Goal |
| :--- | :--- | :--- | :--- |
| **Day 0: First 5 Minutes** | Onboarding & 1-tap notch alignment | User selects notch preset (Center Hole, Left Hole, Monobrow); floating island instantly snaps to camera cutout | 100% activation without permission confusion |
| **Day 1: First Daily Utility** | Media playback & volume adjustments | Music playback displays album art and waveform; volume rocker shows glassmorphic HUD | Habit formation around daily phone usage |
| **Day 3: Social & Caller Value** | Post-call summary & WhatsApp chip | Completed phone calls show summary dialog with 1-tap direct dial or WhatsApp chat | Daily reliance on call utility |
| **Day 7: Trial Transition** | Expiry Island & Rewarded Access | Pro features smoothly lapse; Expiry Island appears with "Watch Ad (+1 Day)" and "Buy Pro" | Conversion to paid or active rewarded viewer |
| **Day 30+: Long-Term Habit** | Live Activities & zero battery drain | Delivery trackers, timers, charging alerts; 0.0 mAh idle battery drain | Permanent installation; high Store rating |
