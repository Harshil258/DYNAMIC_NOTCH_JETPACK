# Firebase Crashlytics & Play Billing Diagnostic Architecture

## 1. Overview
Firebase Crashlytics is configured for the **Android Dynamic Island** project (`ai.emots.kishan_dynamic`) to monitor runtime stability, track fatal crashes, and capture non-fatal diagnostic episodes for critical background subsystems—specifically Google Play Billing connection lifecycle issues.

---

## 2. Build Configuration & Mapping Files

### Root `build.gradle.kts`
```kotlin
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    id("com.google.gms.google-services") version "4.4.2" apply false
    id("com.google.firebase.crashlytics") version "3.0.2" apply false
}
```

### Module `app/build.gradle.kts`
```kotlin
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
}

dependencies {
    implementation("com.google.firebase:firebase-crashlytics-ktx")
    implementation(platform("com.google.firebase:firebase-bom:33.7.0"))
}
```

### Release Proguard & Mapping Files
The Crashlytics Gradle plugin automatically generates and injects the mapping file ID via `:app:injectCrashlyticsMappingFileIdRelease` and uploads de-obfuscation mapping files during release builds.

---

## 3. High-Priority Billing Connection Drop Reporting

Google Play Billing connection drops represent critical revenue risks. `BillingCrashlyticsReporter` captures these events with rate-limiting and stable issue grouping.

### Stable Exception Architecture for Crashlytics Grouping
To prevent Crashlytics issue report fragmentation caused by dynamic timestamps, session IDs, or device hashes in exception messages, we use a stable, dedicated exception class:

```kotlin
class BillingConnectionException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause)
```

**Message Format Template:**
`"Google Play Billing connection dropped: $failureType"`
*(Where `failureType` is one of: `service_disconnected`, `service_unavailable`, `developer_error`, `fatal_error`)*.

All dynamic variables (timestamps, retry counts, screen state, app version) are attached strictly via **Crashlytics Custom Keys**.

---

## 4. Crashlytics Diagnostic Custom Keys

When a billing connection issue or failure occurs, the following non-sensitive attributes are set:

| Custom Key | Type | Description | Example |
| :--- | :--- | :--- | :--- |
| `component` | String | Subsystem identifier | `"play_billing"` |
| `failure_type` | String | Categorized failure mode | `"service_disconnected"` |
| `billing_phase` | String | Purchase state when error occurred | `"checkout_active"` / `"idle_background"` |
| `billing_response_code` | Int | Google Play Billing response code | `3` (`BILLING_UNAVAILABLE`) |
| `billing_library_version`| String | Play Billing SDK version | `"8.3.0"` |
| `connection_state` | String | Connection status | `"disconnected"`, `"reconnecting"` |
| `retry_attempt` | Int | Incremental backoff attempt count | `1`, `2`, `3` |
| `auto_reconnect_enabled`| Boolean | Whether SDK auto-reconnect is active | `true` |
| `purchase_flow_active` | Boolean | Whether user was currently in checkout | `true` |
| `app_version` | String | Release version string | `"1.0.0"` |
| `android_sdk_version` | Int | Device Android API level | `34` |

---

## 5. Rate-Limiting & Anti-Spam Policy

To prevent Crashlytics quota exhaustion and report flooding during network flapping or rapid service disconnections:

1. **Episode Deduplication:** Repeated connection drops of the same `failure_type` within a `60,000ms` (1 minute) cooldown window are recorded only as lightweight analytics telemetry events, suppressing repeated non-fatal Crashlytics exception logs.
2. **State Restoration:** Calling `onConnectionRestored()` resets the failure episode state, ensuring subsequent disconnections are accurately captured.
3. **Entitlement Protection:** Billing connection drops **never** revoke or invalidate locally cached active subscriptions. Valid subscriptions remain active until explicit expiry verification succeeds.
