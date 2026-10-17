package ai.emots.kishan_dynamic.data.analytics

import com.google.android.libraries.ads.mobile.sdk.common.AdValue

/**
 * Tracks Impression-Level Ad Revenue (ILAR) from Google AdMob into Firebase Analytics.
 * Conforms to Google Analytics 4 recommended `ad_impression` schema.
 */
class AdMobRevenueTracker(
    private val analyticsTracker: AnalyticsTracker
) {

    fun trackAdRevenue(
        valueMicros: Long,
        currencyCode: String,
        precisionTypeName: String,
        adFormat: String,
        adUnitId: String,
        placement: String
    ) {
        val revenueInCurrencyUnits = valueMicros / 1_000_000.0

        val params = mapOf(
            AnalyticsParamNames.VALUE to revenueInCurrencyUnits,
            AnalyticsParamNames.CURRENCY to currencyCode,
            AnalyticsParamNames.AD_PLATFORM to "admob",
            AnalyticsParamNames.AD_FORMAT to adFormat,
            AnalyticsParamNames.AD_UNIT_ID to adUnitId,
            AnalyticsParamNames.PLACEMENT to placement,
            AnalyticsParamNames.PRECISION_TYPE to precisionTypeName
        )

        analyticsTracker.logEvent(AnalyticsEventNames.AD_IMPRESSION, params)
    }

    fun trackAdRevenue(
        adValue: AdValue,
        adFormat: String,
        adUnitId: String,
        placement: String
    ) {
        trackAdRevenue(
            valueMicros = adValue.valueMicros,
            currencyCode = adValue.currencyCode,
            precisionTypeName = adValue.precisionType.name,
            adFormat = adFormat,
            adUnitId = adUnitId,
            placement = placement
        )
    }
}
