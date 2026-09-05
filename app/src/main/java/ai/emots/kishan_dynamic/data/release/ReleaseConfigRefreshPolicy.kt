package ai.emots.kishan_dynamic.data.release

/**
 * Decides when a remote release-config failure deserves user-facing recovery.
 *
 * A blank endpoint is the intentional offline/default configuration, so it
 * must never produce a scary network dialog. A configured endpoint failing is
 * different: the user may need to retry before ads or entitlement switches
 * can be refreshed.
 */
object ReleaseConfigRefreshPolicy {
    fun shouldPrompt(endpoint: String, error: String?): Boolean =
        endpoint.isNotBlank() && !error.isNullOrBlank()
}
