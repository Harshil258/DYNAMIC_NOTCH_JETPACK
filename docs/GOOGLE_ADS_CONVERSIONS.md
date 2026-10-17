# Google Ads & Subscription Conversion Tracking Architecture

## 1. Overview
The **Android Dynamic Island** application implements a strict conversion tracking system aligned with Google Ads and Firebase Analytics (GA4). The business objective is to track the full user acquisition and monetization funnel:

$$\text{Ad Click} \longrightarrow \text{Install} \longrightarrow \text{First Open} \longrightarrow \text{Onboarding} \longrightarrow \text{Permissions} \longrightarrow \text{Activation} \longrightarrow \text{Paywall} \longrightarrow \text{Purchase} \longrightarrow \text{Retention}$$

---

## 2. Key Conversion Events in Google Ads

| Google Ads Conversion Action | GA4 Trigger Event | Value Metric | Primary / Secondary |
| :--- | :--- | :--- | :--- |
| **First Open** | `first_open` | Automated | Primary (Install conversion) |
| **Onboarding Complete** | `onboarding_completed` | Count | Secondary (Engagement) |
| **Island Activated** | `island_activated` | Count | Secondary (Activation milestone) |
| **Paywall Viewed** | `paywall_viewed` | Count | Secondary (Intent milestone) |
| **Begin Checkout** | `begin_checkout` | Plan Price | Secondary (Purchase intent) |
| **In-App Purchase / Subscription** | `purchase` | Live Plan Price (e.g. ₹599, $4.99) | **Primary (ROAS Optimization)** |

---

## 3. Purchase Conversion Idempotency

### The Problem
Google Play Billing frequently triggers duplicate purchase updates:
- When the activity resumes after the Google Play dialog closes.
- During automatic startup purchase restoration (`queryPurchasesAsync`).
- When network reconnects trigger repeated listener calls.

Duplicate purchase events severely distort Google Ads return-on-ad-spend (ROAS) and algorithmic bidding.

### The Solution: `SubscriptionConversionTracker`
Purchases are strictly deduplicated in memory using the transaction ID / purchase token:

```kotlin
@Synchronized
fun trackVerifiedPurchase(
    transactionId: String,
    planId: String,
    formattedPrice: String
): Boolean {
    if (recordedTransactions.contains(transactionId)) {
        return false // Duplicate safely discarded
    }
    recordedTransactions.add(transactionId)

    val (value, currency) = parsePriceAndCurrency(formattedPrice)

    analyticsTracker.logEvent(
        AnalyticsEventNames.PURCHASE,
        mapOf(
            AnalyticsParamNames.TRANSACTION_ID to transactionId,
            AnalyticsParamNames.PLAN_ID to planId,
            AnalyticsParamNames.ITEM_ID to planId,
            AnalyticsParamNames.VALUE to value,
            AnalyticsParamNames.CURRENCY to currency
        )
    )
    return true
}
```

---

## 4. Currency and Price Extraction

To ensure GA4 and Google Ads calculate valid revenue without currency conversion skew:
- Prices from `ProductDetails.subscriptionOfferDetails.pricingPhases` are parsed dynamically.
- Supported currency symbols (`₹` -> `INR`, `$` -> `USD`, `€` -> `EUR`, `£` -> `GBP`, `¥` -> `JPY`) are cleanly mapped to ISO 4217 standard currency codes.
- Numerical values are parsed to standard floating point numbers (e.g., `"₹599.00"` -> `599.0`, `"$4.99"` -> `4.99`).

---

## 5. Google Ads Campaign Attribution Link

1. **Firebase Project Linking:** In Google Ads > Tools & Settings > Linked Accounts > Google Analytics (GA4) & Firebase: Link project `ai.emots.kishan_dynamic`.
2. **Import Conversions:** In Google Ads > Measurement > Conversions: Import the `purchase` event as a Primary conversion action with "Use the value from the event".
3. **Smart Bidding:** Configure Target ROAS or Maximize Conversions bidding based on verified `purchase` values.
