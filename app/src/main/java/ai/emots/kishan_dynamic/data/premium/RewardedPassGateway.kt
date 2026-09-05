package ai.emots.kishan_dynamic.data.premium

import android.app.Activity
import kotlinx.coroutines.flow.StateFlow

sealed interface RewardedPassResult {
    data class Unavailable(val message: String) : RewardedPassResult
    data class Started(val message: String) : RewardedPassResult
}

/** UI-facing boundary for a rewarded pass; it never grants access synchronously. */
interface RewardedPassGateway {
    val status: StateFlow<String?>

    fun preload()

    fun show(activity: Activity): RewardedPassResult

    fun close()
}
