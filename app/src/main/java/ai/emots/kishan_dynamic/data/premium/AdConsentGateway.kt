package ai.emots.kishan_dynamic.data.premium

import android.app.Activity
import android.content.Context
import com.google.android.ump.ConsentDebugSettings
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class AdConsentStatus {
    NOT_REQUESTED,
    LOADING,
    READY,
    FAILED
}

data class AdConsentSnapshot(
    val status: AdConsentStatus = AdConsentStatus.NOT_REQUESTED,
    val canRequestAds: Boolean = false,
    val privacyOptionsRequired: Boolean = false,
    val errorMessage: String? = null
)

interface AdConsentGateway : AutoCloseable {
    val state: StateFlow<AdConsentSnapshot>
    fun request(activity: Activity)
}

/** Pure gate shared by rewarded, banner, native, and future full-screen ads. */
object AdConsentPolicy {
    fun canRequestAds(snapshot: AdConsentSnapshot): Boolean =
        snapshot.status == AdConsentStatus.READY && snapshot.canRequestAds
}

/** UMP adapter kept outside Compose and outside individual ad gateways. */
class UmpAdConsentGateway(
    context: Context,
    private val debugGeography: Int? = null
) : AdConsentGateway {
    private val appContext = context.applicationContext
    private val consentInformation: ConsentInformation =
        UserMessagingPlatform.getConsentInformation(appContext)
    private val _state = MutableStateFlow(AdConsentSnapshot())
    override val state: StateFlow<AdConsentSnapshot> = _state.asStateFlow()

    private var requestInProgress = false
    private var closed = false

    override fun request(activity: Activity) {
        if (closed || requestInProgress || _state.value.status == AdConsentStatus.LOADING) return
        if (_state.value.status == AdConsentStatus.READY) return

        requestInProgress = true
        _state.value = _state.value.copy(
            status = AdConsentStatus.LOADING,
            errorMessage = null
        )

        val parameters = ConsentRequestParameters.Builder().apply {
            debugGeography?.let { geography ->
                setConsentDebugSettings(
                    ConsentDebugSettings.Builder(activity)
                        .setDebugGeography(geography)
                        .build()
                )
            }
        }.build()

        consentInformation.requestConsentInfoUpdate(
            activity,
            parameters,
            {
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { formError ->
                    requestInProgress = false
                    if (formError != null) {
                        publishFailure("[${formError.errorCode}] ${formError.message}")
                    } else {
                        publishReady()
                    }
                }
            },
            { requestError ->
                requestInProgress = false
                publishFailure("[${requestError.errorCode}] ${requestError.message}")
            }
        )
    }

    override fun close() {
        closed = true
        requestInProgress = false
    }

    private fun publishReady() {
        _state.value = AdConsentSnapshot(
            status = AdConsentStatus.READY,
            canRequestAds = consentInformation.canRequestAds(),
            privacyOptionsRequired = consentInformation.privacyOptionsRequirementStatus ==
                ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED
        )
    }

    private fun publishFailure(message: String) {
        _state.value = AdConsentSnapshot(
            status = AdConsentStatus.FAILED,
            canRequestAds = false,
            errorMessage = message
        )
    }
}

/** Process-wide consent session shared by every screen-owned ad surface. */
object AppAdConsentRuntime {
    @Volatile
    private var sharedGateway: UmpAdConsentGateway? = null

    @Synchronized
    fun gateway(context: Context, debugGeography: Int? = null): UmpAdConsentGateway {
        sharedGateway?.let { return it }
        return UmpAdConsentGateway(context, debugGeography).also { sharedGateway = it }
    }
}
