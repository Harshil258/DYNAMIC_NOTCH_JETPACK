package ai.emots.kishan_dynamic.ui.platform

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri

fun openPlayStore(context: Context) {
    val packageUri = Uri.parse("market://details?id=${context.packageName}")
    val webUri = Uri.parse("https://play.google.com/store/apps/details?id=${context.packageName}")
    runCatching {
        context.startActivity(Intent(Intent.ACTION_VIEW, packageUri))
    }.recoverCatching {
        context.startActivity(Intent(Intent.ACTION_VIEW, webUri))
    }.onFailure { error ->
        if (error is ActivityNotFoundException) return@onFailure
    }
}

fun openUpdateDestination(context: Context, updateUrl: String) {
    if (updateUrl.isBlank()) {
        openPlayStore(context)
        return
    }
    runCatching {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(updateUrl)))
    }.recoverCatching {
        openPlayStore(context)
    }
}
