package ai.emots.kishan_dynamic.data.premium

import android.content.Context
import ai.emots.kishan_dynamic.data.preferences.AuroraPreferences
import ai.emots.kishan_dynamic.data.preferences.BillingEntitlementRecord
import ai.emots.kishan_dynamic.data.release.ReleaseRuntimeConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

/**
 * Entitlement tier source identifying how Pro status was granted.
 */
enum class EntitlementSource(val serializedName: String) {
    FREE("free"),
    INSTALL_TRIAL("install_trial"),
    REWARDED_ACCESS("rewarded_access"),
    WEEKLY("weekly"),
    MONTHLY("monthly"),
    YEARLY("yearly"),
    LIFETIME("lifetime");

    companion object {
        fun fromString(value: String): EntitlementSource {
            return entries.firstOrNull { it.serializedName.equals(value, ignoreCase = true) } ?: FREE
        }
    }
}

/**
 * Verified user entitlement snapshot.
 * Authoritative single source of truth for free vs pro status.
 */
data class EntitlementSnapshot(
    val source: EntitlementSource = EntitlementSource.FREE,
    val isPro: Boolean = false,
    val isAdFree: Boolean = false,
    val expiresAtMillis: Long? = null,
    val isPendingPayment: Boolean = false,
    val isTrial: Boolean = false,
    val isRewarded: Boolean = false,
    val remainingDays: Int = 0,
    val planDisplayName: String = "Free",
    val isAutoRenewing: Boolean = false,
    val reconciliationState: String = "verified"
) {
    val isExpired: Boolean
        get() = expiresAtMillis != null && System.currentTimeMillis() > expiresAtMillis

    val isActive: Boolean
        get() = isPro && !isExpired && !isPendingPayment

    val isAdFreePaidUser: Boolean
        get() = isAdFree && isPro && !isExpired && !isPendingPayment

    fun shouldShowExpiryIsland(): Boolean = !isActive
}

/**
 * Canonical feature keys for the feature entitlement matrix.
 */
object EntitlementFeatures {
    // Universal core capabilities — strictly NEVER gated
    const val CORE_ISLAND_ACTIVATION = "core_island_activation"
    const val BASIC_NOTIFICATIONS = "basic_notifications"
    const val BASIC_MUSIC_CONTROLS = "basic_music_controls"
    const val BASIC_NOTCH_ALIGNMENT = "basic_notch_alignment"
    const val CALL_PRESENTATION = "call_presentation"
    const val CHARGING_PRESENTATION = "charging_presentation"
    const val BLUETOOTH_PRESENTATION = "bluetooth_presentation"
    const val PRIVACY_CONTROLS = "privacy_controls"
    const val PERMISSIONS_MANAGEMENT = "permissions_management"
    const val RESTORE_PURCHASES = "restore_purchases"

    // Premium Pro capabilities
    const val THEME_SETTINGS = "theme_settings"
    const val DISPLAY_HORIZONTAL_OFFSET = "display_horizontal_offset"
    const val SOUND_SETTINGS = "sound_settings"
    const val CALL_END_DISABLE = "call_end_disable"
    const val COMPACT_MUSIC = "compact_music"
    const val NOTIFICATION_DURATION_EXTENDED = "notification_duration"
    const val QUICK_CONTROL_UNLIMITED = "quick_control"
    const val AD_FREE = "ad_free"
}

/**
 * Pure policy evaluating feature accessibility against an entitlement snapshot.
 * Essential recovery, privacy, and core island features are never locked.
 */
object FeatureEntitlementPolicy {
    private val essentialFreeFeatures = setOf(
        EntitlementFeatures.CORE_ISLAND_ACTIVATION,
        EntitlementFeatures.BASIC_NOTIFICATIONS,
        EntitlementFeatures.BASIC_MUSIC_CONTROLS,
        EntitlementFeatures.BASIC_NOTCH_ALIGNMENT,
        EntitlementFeatures.CALL_PRESENTATION,
        EntitlementFeatures.CHARGING_PRESENTATION,
        EntitlementFeatures.BLUETOOTH_PRESENTATION,
        EntitlementFeatures.PRIVACY_CONTROLS,
        EntitlementFeatures.PERMISSIONS_MANAGEMENT,
        EntitlementFeatures.RESTORE_PURCHASES
    )

    private val proFeatureSet = setOf(
        EntitlementFeatures.THEME_SETTINGS,
        EntitlementFeatures.DISPLAY_HORIZONTAL_OFFSET,
        EntitlementFeatures.SOUND_SETTINGS,
        EntitlementFeatures.CALL_END_DISABLE,
        EntitlementFeatures.COMPACT_MUSIC,
        EntitlementFeatures.NOTIFICATION_DURATION_EXTENDED,
        EntitlementFeatures.QUICK_CONTROL_UNLIMITED,
        EntitlementFeatures.AD_FREE
    )

    fun canUseFeature(featureId: String, snapshot: EntitlementSnapshot): Boolean {
        if (featureId in essentialFreeFeatures) return true
        if (featureId == EntitlementFeatures.AD_FREE) return snapshot.isAdFree
        if (featureId in proFeatureSet) return snapshot.isActive
        // Default to accessible unless explicitly gated as Pro
        return true
    }

    fun isFeatureLocked(featureId: String, snapshot: EntitlementSnapshot): Boolean {
        return !canUseFeature(featureId, snapshot)
    }

    fun canUseFeature(featureId: String, isProActive: Boolean): Boolean {
        return canUseFeature(featureId, EntitlementSnapshot(isPro = isProActive))
    }
}

/**
 * Centralized Entitlement Manager coordinating DataStore, Google Play Billing,
 * 7-day install trial, and rewarded ad passes into a single reactive StateFlow.
 */
class EntitlementManager(
    context: Context,
    scope: CoroutineScope = CoroutineScope(Dispatchers.Default)
) {
    companion object {
        @Volatile
        private var INSTANCE: EntitlementManager? = null

        fun getInstance(context: Context): EntitlementManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: EntitlementManager(context.applicationContext).also {
                    INSTANCE = it
                }
            }
        }

        fun reconcile(
            billing: BillingEntitlementRecord,
            trialSnapshot: TrialSnapshot,
            rewardedSnapshot: RewardedAccessSnapshot
        ): EntitlementSnapshot {
            val now = System.currentTimeMillis()

            // 1. Paid subscriptions and Lifetime purchases have absolute highest priority
            if (billing.isPaidPro) {
                val isExplicitLifetime = billing.isLifetime ||
                    billing.planId?.equals("lifetime", ignoreCase = true) == true ||
                    billing.productId?.contains("lifetime", ignoreCase = true) == true

                val isSubscriptionExpired = !isExplicitLifetime && billing.proExpiresAt != null && billing.proExpiresAt <= now

                if (!isSubscriptionExpired) {
                    val source = when {
                        isExplicitLifetime -> EntitlementSource.LIFETIME
                        billing.planId?.contains("yearly", ignoreCase = true) == true || billing.productId?.contains("yearly", ignoreCase = true) == true -> EntitlementSource.YEARLY
                        billing.planId?.contains("monthly", ignoreCase = true) == true || billing.productId?.contains("monthly", ignoreCase = true) == true -> EntitlementSource.MONTHLY
                        billing.planId?.contains("weekly", ignoreCase = true) == true || billing.productId?.contains("weekly", ignoreCase = true) == true -> EntitlementSource.WEEKLY
                        else -> EntitlementSource.WEEKLY
                    }
                    val displayName = when (source) {
                        EntitlementSource.LIFETIME -> "Lifetime"
                        EntitlementSource.YEARLY -> "Yearly"
                        EntitlementSource.MONTHLY -> "Monthly"
                        EntitlementSource.WEEKLY -> "Weekly"
                        else -> "Pro"
                    }

                    val remainingDays = when {
                        isExplicitLifetime -> 9999
                        billing.proExpiresAt != null -> (((billing.proExpiresAt - now) / (24 * 60 * 60 * 1000L)).toInt()).coerceAtLeast(0)
                        billing.lastVerifiedAt != null -> {
                            val cacheExpiry = billing.lastVerifiedAt + (7 * 24 * 60 * 60 * 1000L)
                            (((cacheExpiry - now) / (24 * 60 * 60 * 1000L)).toInt()).coerceAtLeast(0)
                        }
                        else -> 0
                    }

                    val effectiveExpiresAt = if (isExplicitLifetime) null else (billing.proExpiresAt ?: (billing.lastVerifiedAt?.let { it + 7 * 86400000L }))

                    return EntitlementSnapshot(
                        source = source,
                        isPro = true,
                        isAdFree = true,
                        expiresAtMillis = effectiveExpiresAt,
                        isPendingPayment = false,
                        isTrial = false,
                        isRewarded = false,
                        remainingDays = remainingDays,
                        planDisplayName = displayName,
                        isAutoRenewing = billing.isAutoRenewing,
                        reconciliationState = billing.reconciliationState
                    )
                }
            }

            // 2. Check temporary access: Trial vs Rewarded Pass
            val trialActive = trialSnapshot.isActive
            val rewardActive = rewardedSnapshot.isActive

            if (trialActive && rewardActive) {
                // Whichever has the later expiry date wins
                return if (rewardedSnapshot.expiresAtMillis >= trialSnapshot.expiresAtMillis) {
                    EntitlementSnapshot(
                        source = EntitlementSource.REWARDED_ACCESS,
                        isPro = true,
                        isAdFree = false,
                        expiresAtMillis = rewardedSnapshot.expiresAtMillis,
                        isPendingPayment = false,
                        isTrial = false,
                        isRewarded = true,
                        remainingDays = rewardedSnapshot.remainingDays,
                        planDisplayName = "Reward Pass",
                        reconciliationState = billing.reconciliationState
                    )
                } else {
                    EntitlementSnapshot(
                        source = EntitlementSource.INSTALL_TRIAL,
                        isPro = true,
                        isAdFree = false,
                        expiresAtMillis = trialSnapshot.expiresAtMillis,
                        isPendingPayment = false,
                        isTrial = true,
                        isRewarded = false,
                        remainingDays = trialSnapshot.remainingDays,
                        planDisplayName = "7-Day Trial",
                        reconciliationState = billing.reconciliationState
                    )
                }
            } else if (trialActive) {
                return EntitlementSnapshot(
                    source = EntitlementSource.INSTALL_TRIAL,
                    isPro = true,
                    isAdFree = false,
                    expiresAtMillis = trialSnapshot.expiresAtMillis,
                    isPendingPayment = false,
                    isTrial = true,
                    isRewarded = false,
                    remainingDays = trialSnapshot.remainingDays,
                    planDisplayName = "7-Day Trial",
                    reconciliationState = billing.reconciliationState
                )
            } else if (rewardActive) {
                return EntitlementSnapshot(
                    source = EntitlementSource.REWARDED_ACCESS,
                    isPro = true,
                    isAdFree = false,
                    expiresAtMillis = rewardedSnapshot.expiresAtMillis,
                    isPendingPayment = false,
                    isTrial = false,
                    isRewarded = true,
                    remainingDays = rewardedSnapshot.remainingDays,
                    planDisplayName = "Reward Pass",
                    reconciliationState = billing.reconciliationState
                )
            }

            // 3. Base tier: Free
            return EntitlementSnapshot(
                source = EntitlementSource.FREE,
                isPro = false,
                isAdFree = false,
                expiresAtMillis = null,
                isPendingPayment = false,
                isTrial = false,
                isRewarded = false,
                remainingDays = 0,
                planDisplayName = "Free",
                reconciliationState = billing.reconciliationState
            )
        }

        fun reconcile(
            isPaidPro: Boolean,
            proExpiresAt: Long?,
            planId: String?,
            trialSnapshot: TrialSnapshot,
            rewardedSnapshot: RewardedAccessSnapshot,
            isLifetime: Boolean = false,
            lastVerifiedAt: Long? = null,
            isAutoRenewing: Boolean = false,
            reconciliationState: String = "verified"
        ): EntitlementSnapshot = reconcile(
            billing = BillingEntitlementRecord(
                isPaidPro = isPaidPro,
                proExpiresAt = proExpiresAt,
                proSource = planId ?: "none",
                planId = planId,
                productId = null,
                isLifetime = isLifetime,
                lastVerifiedAt = lastVerifiedAt,
                isAutoRenewing = isAutoRenewing,
                reconciliationState = reconciliationState
            ),
            trialSnapshot = trialSnapshot,
            rewardedSnapshot = rewardedSnapshot
        )
    }

    private val appContext = context.applicationContext
    private val preferences = AuroraPreferences(appContext)
    val trialManager = TrialManager(appContext, preferences)
    val rewardedAccessManager = RewardedAccessManager(appContext, preferences)

    val entitlement: StateFlow<EntitlementSnapshot> = combine(
        preferences.billingRecord,
        trialManager.trialSnapshotFlow,
        rewardedAccessManager.rewardedSnapshotFlow
    ) { billing: BillingEntitlementRecord, trialSnapshot: TrialSnapshot, rewardedSnapshot: RewardedAccessSnapshot ->
        reconcile(billing, trialSnapshot, rewardedSnapshot)
    }.stateIn(
        scope = scope,
        started = SharingStarted.Eagerly,
        initialValue = EntitlementSnapshot()
    )

    internal fun reconcileEntitlement(
        billing: BillingEntitlementRecord,
        trialSnapshot: TrialSnapshot,
        rewardedSnapshot: RewardedAccessSnapshot
    ): EntitlementSnapshot = reconcile(billing, trialSnapshot, rewardedSnapshot)

    internal fun reconcileEntitlement(
        isPaidPro: Boolean,
        proExpiresAt: Long?,
        planId: String?,
        trialSnapshot: TrialSnapshot,
        rewardedSnapshot: RewardedAccessSnapshot,
        isLifetime: Boolean = false,
        lastVerifiedAt: Long? = null,
        isAutoRenewing: Boolean = false,
        reconciliationState: String = "verified"
    ): EntitlementSnapshot = reconcile(
        isPaidPro = isPaidPro,
        proExpiresAt = proExpiresAt,
        planId = planId,
        trialSnapshot = trialSnapshot,
        rewardedSnapshot = rewardedSnapshot,
        isLifetime = isLifetime,
        lastVerifiedAt = lastVerifiedAt,
        isAutoRenewing = isAutoRenewing,
        reconciliationState = reconciliationState
    )

    fun canUse(featureId: String): Boolean {
        return FeatureEntitlementPolicy.canUseFeature(featureId, entitlement.value)
    }

    fun isLocked(featureId: String): Boolean {
        return FeatureEntitlementPolicy.isFeatureLocked(featureId, entitlement.value)
    }

    fun isAdFree(): Boolean {
        return entitlement.value.isAdFree
    }

    fun shouldShowExpiryIsland(): Boolean {
        return !entitlement.value.isActive
    }

    fun getSnapshot(): EntitlementSnapshot = entitlement.value
}
