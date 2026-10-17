# Aurora Dynamic Island — Feature Entitlement Matrix & Access Policy

## 1. Architectural Philosophy
Aurora Dynamic Island adheres to a strict consumer-first principle: **core functionality, setup essentials, system safety, and basic notch calibration are never held hostage behind a paywall**.

Premium features focus on deep aesthetic personalization, advanced customization, unlimited shortcuts, and ad removal.

---

## 2. Complete Feature Comparison Matrix

| Feature | Identifier Key | Free Tier | 7-Day Trial | Rewarded Pass (+24h) | Aurora Pro (Paid) |
| :--- | :--- | :---: | :---: | :---: | :---: |
| **Core Island Activation** | `core_island_activation` | **Yes** | **Yes** | **Yes** | **Yes** |
| **Basic Notifications HUD** | `basic_notifications` | **Yes** | **Yes** | **Yes** | **Yes** |
| **Basic Music Playback Controls** | `basic_music_controls` | **Yes** | **Yes** | **Yes** | **Yes** |
| **Basic Notch Alignment** (Vertical & Width) | `basic_notch_alignment` | **Yes** | **Yes** | **Yes** | **Yes** |
| **Camera Cutout Selection** | `basic_notch_alignment` | **Yes** | **Yes** | **Yes** | **Yes** |
| **Incoming Call Banner & Live Timer** | `call_presentation` | **Yes** | **Yes** | **Yes** | **Yes** |
| **Battery Charging Animation** | `charging_presentation` | **Yes** | **Yes** | **Yes** | **Yes** |
| **Bluetooth Accessory HUD** | `bluetooth_presentation` | **Yes** | **Yes** | **Yes** | **Yes** |
| **Privacy & Sensitive Content Hide** | `privacy_controls` | **Yes** | **Yes** | **Yes** | **Yes** |
| **Permission Management** | `permissions_management` | **Yes** | **Yes** | **Yes** | **Yes** |
| **Restore Purchases** | `restore_purchases` | **Yes** | **Yes** | **Yes** | **Yes** |
| **Sub-Pixel Horizontal Offset** | `display_horizontal_offset` | No | **Yes** | **Yes** | **Yes** |
| **Monobrow (Flat-Top) Notch Style** | `display_monobrow` | No | **Yes** | **Yes** | **Yes** |
| **Custom RGB Color Calibration** | `display_custom_colors` | No | **Yes** | **Yes** | **Yes** |
| **Idle Auto-Hide & Timeout Slider** | `display_autohide` | No | **Yes** | **Yes** | **Yes** |
| **Aurora Glow Themes & Waveforms** | `theme_settings` | No | **Yes** | **Yes** | **Yes** |
| **Custom Sound HUD Styles** | `sound_settings` | No | **Yes** | **Yes** | **Yes** |
| **Call-End Summary Dialog** | `call_end_disable` | No | **Yes** | **Yes** | **Yes** |
| **Compact Music Mini-Player** | `compact_music` | No | **Yes** | **Yes** | **Yes** |
| **Extended Notification Durations** (10s, 15s) | `notification_duration` | No (Max 5s) | **Yes** | **Yes** | **Yes** |
| **Unlimited Quick Controls** | `quick_control` | Limit (3 apps, 2 contacts) | **Yes (Unlimited)** | **Yes (Unlimited)** | **Yes (Unlimited)** |
| **100% Ad-Free Experience** | `ad_free` | No | No | No | **Yes (100% Ad-Free)** |

---

## 3. Enforcement Code Structure
The pure evaluation logic lives in `FeatureEntitlementPolicy`:

```kotlin
object FeatureEntitlementPolicy {
    fun canUseFeature(featureId: String, snapshot: EntitlementSnapshot): Boolean {
        if (featureId in essentialFreeFeatures) return true
        if (featureId == EntitlementFeatures.AD_FREE) return snapshot.isAdFree
        if (featureId in proFeatureSet) return snapshot.isActive
        return true
    }
}
```

UI composables and background services call this policy directly rather than checking raw preference flags or hardcoded constants.
