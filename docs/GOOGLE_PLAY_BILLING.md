# Aurora Dynamic Island — Google Play Billing & Monetization Architecture

## 1. Overview
Aurora Dynamic Island integrates **Google Play Billing Library v7** through `PlayBillingPremiumAccessGateway`. 

The system provides localized prices, supports pending transactions gracefully, guarantees purchase idempotency, and eliminates visual clutter during the payment flow.

---

## 2. Product Catalog & SKU Specifications

| Product ID | Type | Fallback Display Price | Billing Period | Conversion Hook |
| :--- | :---: | :---: | :---: | :--- |
| `weekly` | Subscription | ₹39 | 1 Week | Short commitment / trial extension |
| `monthly` | Subscription | ₹59 | 1 Month | Standard monthly recurring |
| `yearly` | Subscription | ₹99 | 1 Year | Best recurring value (Save 70%) |
| `lifetime` | In-App Product (One-time) | ₹149 | Permanent | Best overall value / Never pay again |

### Localized Price Formatting
- Fallback prices (₹39, ₹59, ₹99, ₹149) are used **only** when Play Store services are initializing or disconnected.
- When Google Play Billing connects, `queryProductDetailsAsync` retrieves live localized prices formatted in the user's local currency (`productDetails.subscriptionOfferDetails` or `productDetails.oneTimePurchaseOfferDetails`).

---

## 3. Purchase Lifecycle & Idempotency Engine
```
   [ User Selects Plan & Taps 'Continue' ]
                 │
                 ▼
   [ Paywall Launches BillingFlowParams ]
                 │
                 ▼
     [ onPurchasesUpdated Callback ]
                 │
      ┌──────────┴───────────────┐
      ▼                          ▼
 [ PURCHASED ]             [ PENDING ]
      │                          │
      ├─ Acknowledge Purchase    └─ Update snapshot: isPendingPayment = true
      ├─ Store in DataStore         Notify user: "Payment pending verification"
      └─ Route to Tracker
                 │
                 ▼
 [ SubscriptionConversionTracker.trackPurchase ]
      ├─ Check if purchaseToken already tracked
      ├─ Record purchase to GA4 ('purchase' event)
      ├─ Report Google Ads Conversion ('subscription_purchase')
      └─ Update EntitlementManager (Immediate Pro Activation)
```

### Double-Counting & Duplicate Event Protection
- `SubscriptionConversionTracker` caches processed purchase tokens in local storage.
- If a purchase token has already been recorded, repeat callbacks (e.g. during app resume or network reconnect) are ignored, preserving Google Ads ROAS accuracy.

---

## 4. Overlay Suppression During Purchases
> [!IMPORTANT]
> **Zero Floating Overlay Collisions on Paywalls**
> While any paywall or purchase flow is visible, the dynamic island floating overlay is completely suppressed:
> `IslandStateManager.setPaywallActive(true)` on launch, and `setPaywallActive(false)` on dismiss.
> This guarantees that Google Play's bottom sheet purchase dialog is never occluded or intercepted by floating UI.

---

## 5. Subscription Management & Restoration
- **Manage Subscriptions**: Directly routes users to `https://play.google.com/store/account/subscriptions` with `package=ai.emots.kishan_dynamic`.
- **Restore Purchases**: Queries both active in-app purchases (`BillingClient.ProductType.INAPP`) and active subscriptions (`BillingClient.ProductType.SUBS`). Verified purchases are automatically restored to DataStore.
