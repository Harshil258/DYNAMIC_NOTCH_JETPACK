package ai.emots.kishan_dynamic.data.model

/**
 * Setup is only recorded after every required permission is granted.
 * Optional permissions never block the setup milestone.
 */
fun requiredSetupIsComplete(grantedRequiredCount: Int, requiredCount: Int): Boolean =
    requiredCount > 0 && grantedRequiredCount >= requiredCount
