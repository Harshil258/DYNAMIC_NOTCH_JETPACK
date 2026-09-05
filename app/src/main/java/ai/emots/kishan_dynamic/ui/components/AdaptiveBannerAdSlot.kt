package ai.emots.kishan_dynamic.ui.components

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import ai.emots.kishan_dynamic.data.premium.AdSurfacePolicy
import ai.emots.kishan_dynamic.data.premium.BannerAdConfiguration
import ai.emots.kishan_dynamic.data.premium.BannerAdStatus
import ai.emots.kishan_dynamic.data.premium.MobileAdsBannerGateway

private fun Context.findActivity(): Activity? {
    var current = this
    while (current is ContextWrapper) {
        if (current is Activity) return current
        current = current.baseContext
    }
    return current as? Activity
}

/** Premium-aware adaptive banner slot; no ad view is created for Pro users. */
@Composable
fun AdaptiveBannerAdSlot(
    configuration: BannerAdConfiguration,
    isProActive: Boolean,
    canRequestAds: Boolean,
    modifier: Modifier = Modifier,
    onAdLoaded: () -> Unit = {},
    onAdFailed: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val isPreview = LocalInspectionMode.current
    val screenWidthDp = LocalConfiguration.current.screenWidthDp.coerceAtLeast(320)
    val adaptiveSize = remember(screenWidthDp) {
        com.google.android.libraries.ads.mobile.sdk.banner.AdSize
            .getLargeAnchoredAdaptiveBannerAdSize(context, screenWidthDp)
    }

    if (isPreview || !AdSurfacePolicy.shouldShowBanner(
            adsEnabled = configuration.enabled,
            isProActive = isProActive,
            adUnitConfigured = configuration.adUnitId.isNotBlank(),
            canRequestAds = canRequestAds
        )
    ) return

    val gateway = remember(configuration, adaptiveSize) {
        MobileAdsBannerGateway(context, configuration, adaptiveSize)
    }
    val status by gateway.status.collectAsState()

    DisposableEffect(gateway) {
        onDispose { gateway.close() }
    }
    LaunchedEffect(gateway) {
        gateway.preload()
    }

    when (val current = status) {
        BannerAdStatus.Idle,
        BannerAdStatus.Loading,
        BannerAdStatus.Unavailable -> Unit

        is BannerAdStatus.Failed -> {
            LaunchedEffect(current.message) { onAdFailed(current.message) }
        }

        is BannerAdStatus.Ready -> {
            LaunchedEffect(current.ad) { onAdLoaded() }
            AndroidView(
                modifier = modifier
                    .fillMaxWidth()
                    .height(adaptiveSize.height.dp),
                factory = { viewContext ->
                    com.google.android.libraries.ads.mobile.sdk.banner.AdView(viewContext).also { view ->
                        viewContext.findActivity()?.let { activity -> gateway.attach(view, activity) }
                    }
                },
                update = { view ->
                    view.context.findActivity()?.let { activity -> gateway.attach(view, activity) }
                }
            )
        }
    }
}
