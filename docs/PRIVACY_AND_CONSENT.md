# Privacy, Consent & Google Consent Mode Architecture

## 1. Zero-PII Collection Contract

The **Android Dynamic Island** application implements a strict zero-PII (Personally Identifiable Information) privacy boundary:

> [!CAUTION]
> Under no circumstances may notification text, message bodies, sender names, caller IDs, contact names, phone numbers, one-time passwords (OTPs), financial data, passwords, keystrokes, or contents of third-party applications be logged, stored, or transmitted to any analytics, diagnostics, or advertising endpoint.

### Strict Redaction Boundaries:
1. **Notification Listener Service (`IslandNotificationListener`):**
   - Notification text, message bodies, sender identities, and notification action payloads are parsed in volatile memory only for local rendering in the accessibility overlay.
   - Zero notification body content is forwarded to GA4, Crashlytics, or external network servers.
2. **Contact Shortcuts & Quick Dial:**
   - Contact numbers and photos chosen by the user in `ActionIslandRepository` remain strictly in local Android storage (`AuroraPreferences`).
   - Interaction events log only generic identifiers (e.g., `tile="call_shortcut"` or `action="phone"`), never the user's phone number or contact name.
3. **Island Interaction Events:**
   - All analytics parameters are limited to categorical states: `island_expand`, `island_swipe_dismiss`, `media_control_used` (`"musicplaypause"`, `"musicnext"`).
   - No track title, artist name, or media stream URL is forwarded to analytics.

---

## 2. Google Consent Mode v2 Integration

The application synchronizes User Messaging Platform (UMP) consent states with Google Analytics 4 and Google AdMob using `AnalyticsConsentManager`.

### Consent Types Managed:
- `FirebaseAnalytics.ConsentType.ANALYTICS_STORAGE`
- `FirebaseAnalytics.ConsentType.AD_STORAGE`
- `FirebaseAnalytics.ConsentType.AD_USER_DATA`
- `FirebaseAnalytics.ConsentType.AD_PERSONALIZATION`

### Synchronization Logic:
```kotlin
fun updateConsent(
    analyticsStorageGranted: Boolean,
    adStorageGranted: Boolean,
    adUserDataGranted: Boolean = adStorageGranted,
    adPersonalizationGranted: Boolean = adStorageGranted
) {
    val consentMap = mapOf(
        FirebaseAnalytics.ConsentType.ANALYTICS_STORAGE to if (analyticsStorageGranted) {
            FirebaseAnalytics.ConsentStatus.GRANTED
        } else {
            FirebaseAnalytics.ConsentStatus.DENIED
        },
        FirebaseAnalytics.ConsentType.AD_STORAGE to if (adStorageGranted) {
            FirebaseAnalytics.ConsentStatus.GRANTED
        } else {
            FirebaseAnalytics.ConsentStatus.DENIED
        },
        FirebaseAnalytics.ConsentType.AD_USER_DATA to if (adUserDataGranted) {
            FirebaseAnalytics.ConsentStatus.GRANTED
        } else {
            FirebaseAnalytics.ConsentStatus.DENIED
        },
        FirebaseAnalytics.ConsentType.AD_PERSONALIZATION to if (adPersonalizationGranted) {
            FirebaseAnalytics.ConsentStatus.GRANTED
        } else {
            FirebaseAnalytics.ConsentStatus.DENIED
        }
    )
    firebaseAnalytics?.setConsent(consentMap)
}
```

---

## 3. User Messaging Platform (UMP) Integration

1. **GDPR / EEA Compliance:**
   - In EEA regions or when `debugGeography = DEBUG_GEOGRAPHY_EEA`, `AppAdConsentRuntime` queries Google UMP.
   - If consent is required, `UserMessagingPlatform.loadAndShowConsentFormIfRequired` presents the official Google consent form before requesting ads.
2. **Non-Consent Fallback:**
   - If the user declines ad consent, `canRequestAds` returns `false`.
   - AdMob SDK is instructed not to make ad requests.
   - All in-app ad surfaces collapse to `0.dp` to prevent empty UI boxes.
3. **Resetting Consent:**
   - In Settings Vault > Privacy & Consent, the user can re-trigger `loadAndShowConsentFormIfRequired` to adjust their tracking preferences at any time.
