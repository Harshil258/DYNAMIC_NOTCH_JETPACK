package ai.emots.kishan_dynamic.ui.components

import android.graphics.Color as AndroidColor
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.libraries.ads.mobile.sdk.common.AdChoicesView
import com.google.android.libraries.ads.mobile.sdk.nativead.MediaView
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAd
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAdView
import ai.emots.kishan_dynamic.data.premium.AdSurfacePolicy
import ai.emots.kishan_dynamic.data.premium.NativeAdConfiguration
import ai.emots.kishan_dynamic.data.premium.NativeAdStatus
import ai.emots.kishan_dynamic.data.premium.MobileAdsNativeAdGateway
import ai.emots.kishan_dynamic.ui.kit.AppCard
import ai.emots.kishan_dynamic.ui.theme.AppTheme

/**
 * Compact native-ad card using the app's grouped-card language. The Android
 * view only owns SDK asset registration; placement and visibility stay in the
 * Compose/domain boundary.
 */
@Composable
fun NativeAdCard(
    configuration: NativeAdConfiguration,
    isProActive: Boolean,
    canRequestAds: Boolean,
    placement: String,
    modifier: Modifier = Modifier,
    onAdLoaded: () -> Unit = {},
    onAdFailed: (String) -> Unit = {}
) {
    val context = LocalContext.current
    if (LocalInspectionMode.current || !AdSurfacePolicy.shouldShowNative(
            adsEnabled = configuration.enabled,
            isProActive = isProActive,
            adUnitConfigured = configuration.adUnitId.isNotBlank(),
            canRequestAds = canRequestAds
        )
    ) return

    val gateway = remember(configuration) { MobileAdsNativeAdGateway(context, configuration) }
    val status by gateway.status.collectAsState()

    DisposableEffect(gateway) {
        onDispose { gateway.close() }
    }
    LaunchedEffect(gateway) { gateway.preload() }

    when (val current = status) {
        NativeAdStatus.Idle,
        NativeAdStatus.Loading,
        NativeAdStatus.Unavailable -> Unit

        is NativeAdStatus.Failed -> {
            LaunchedEffect(placement, current.message) { onAdFailed(current.message) }
        }

        is NativeAdStatus.Ready -> {
            LaunchedEffect(placement, current.ad) { onAdLoaded() }
            val backgroundColor = AppTheme.colors.surface.toArgbCompat()
            val textColor = AppTheme.colors.textPrimary.toArgbCompat()
            val secondaryColor = AppTheme.colors.textSecondary.toArgbCompat()
            val accentColor = AppTheme.colors.accent.toArgbCompat()
            AppCard(modifier = modifier) {
                AndroidView(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 96.dp),
                    factory = { viewContext ->
                        buildNativeAdView(
                            context = viewContext,
                            ad = current.ad,
                            backgroundColor = backgroundColor,
                            textColor = textColor,
                            secondaryColor = secondaryColor,
                            accentColor = accentColor
                        )
                    }
                )
            }
        }
    }
}

private fun buildNativeAdView(
    context: android.content.Context,
    ad: NativeAd,
    backgroundColor: Int,
    textColor: Int,
    secondaryColor: Int,
    accentColor: Int
): NativeAdView {
    val density = context.resources.displayMetrics.density
    fun dp(value: Int): Int = (value * density).toInt()

    val nativeAdView = NativeAdView(context)
    nativeAdView.background = GradientDrawable().apply {
        setColor(backgroundColor)
        cornerRadius = dp(18).toFloat()
        setStroke(dp(1), blend(backgroundColor, textColor, 0.16f))
    }

    val root = LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(dp(10), dp(10), dp(10), dp(8))
    }

    val header = LinearLayout(context).apply {
        gravity = Gravity.CENTER_VERTICAL
    }
    val icon = ImageView(context).apply {
        layoutParams = LinearLayout.LayoutParams(dp(42), dp(42)).apply {
            marginEnd = dp(10)
        }
        scaleType = ImageView.ScaleType.CENTER_CROP
        ad.icon?.drawable?.let { setImageDrawable(it) }
        background = GradientDrawable().apply {
            setColor(blend(backgroundColor, textColor, 0.08f))
            cornerRadius = dp(10).toFloat()
        }
    }
    header.addView(icon)

    val copy = LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
    }
    val label = TextView(context).apply {
        text = "Ad  ·  ${ad.advertiser.orEmpty().ifBlank { "Sponsored" }}"
        setTextColor(secondaryColor)
        textSize = 11f
    }
    val headline = TextView(context).apply {
        text = ad.headline.orEmpty()
        setTextColor(textColor)
        textSize = 16f
        typeface = Typeface.create("sans-serif", Typeface.BOLD)
        maxLines = 1
        ellipsize = android.text.TextUtils.TruncateAt.END
    }
    copy.addView(label)
    copy.addView(headline)
    header.addView(copy)

    val adChoices = AdChoicesView(context).apply {
        layoutParams = LinearLayout.LayoutParams(dp(24), dp(24))
    }
    header.addView(adChoices)
    root.addView(header)

    val media = MediaView(context).apply {
        layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            dp(88)
        ).apply { topMargin = dp(8) }
        ad.mediaContent?.let { mediaContent = it }
    }
    root.addView(media)

    val body = TextView(context).apply {
        text = ad.body.orEmpty()
        setTextColor(secondaryColor)
        textSize = 13f
        maxLines = 2
        ellipsize = android.text.TextUtils.TruncateAt.END
        setPadding(0, dp(8), 0, dp(6))
    }
    root.addView(body)

    val callToAction = TextView(context).apply {
        text = ad.callToAction.orEmpty().ifBlank { "Learn more" }
        setTextColor(AndroidColor.WHITE)
        textSize = 14f
        gravity = Gravity.CENTER
        typeface = Typeface.create("sans-serif", Typeface.BOLD)
        background = GradientDrawable(
            GradientDrawable.Orientation.LEFT_RIGHT,
            intArrayOf(accentColor, blend(accentColor, AndroidColor.WHITE, 0.16f))
        ).apply { cornerRadius = dp(12).toFloat() }
        minimumHeight = dp(42)
    }
    root.addView(callToAction)

    nativeAdView.addView(
        root,
        FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        )
    )
    nativeAdView.headlineView = headline
    nativeAdView.bodyView = body
    nativeAdView.callToActionView = callToAction
    nativeAdView.iconView = icon
    nativeAdView.adChoicesView = adChoices
    nativeAdView.registerNativeAd(ad, media)
    return nativeAdView
}

private fun blend(base: Int, overlay: Int, amount: Float): Int {
    val clamped = amount.coerceIn(0f, 1f)
    fun channel(baseChannel: Int, overlayChannel: Int): Int =
        (baseChannel + (overlayChannel - baseChannel) * clamped).toInt()
    return AndroidColor.rgb(
        channel(AndroidColor.red(base), AndroidColor.red(overlay)),
        channel(AndroidColor.green(base), AndroidColor.green(overlay)),
        channel(AndroidColor.blue(base), AndroidColor.blue(overlay))
    )
}

private fun androidx.compose.ui.graphics.Color.toArgbCompat(): Int =
    android.graphics.Color.argb(
        (alpha * 255f).toInt().coerceIn(0, 255),
        (red * 255f).toInt().coerceIn(0, 255),
        (green * 255f).toInt().coerceIn(0, 255),
        (blue * 255f).toInt().coerceIn(0, 255)
    )
