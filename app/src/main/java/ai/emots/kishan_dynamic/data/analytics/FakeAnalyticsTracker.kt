package ai.emots.kishan_dynamic.data.analytics

data class LoggedEvent(
    val name: String,
    val parameters: Map<String, Any> = emptyMap()
)

data class LoggedScreen(
    val name: String,
    val className: String
)

/**
 * In-memory test implementation of AnalyticsTracker for deterministic unit testing.
 */
class FakeAnalyticsTracker(
    var throwOnValidationError: Boolean = false
) : AnalyticsTracker {
    val events = mutableListOf<LoggedEvent>()
    val screens = mutableListOf<LoggedScreen>()
    val userProperties = mutableMapOf<String, String>()
    var currentUserId: String? = null

    override fun logEvent(name: String, parameters: Map<String, Any>) {
        val sanitized = AnalyticsSchemaValidator.validateAndSanitize(name, parameters, throwOnError = throwOnValidationError)
        events.add(LoggedEvent(name, sanitized))
    }

    override fun logScreen(screenName: String, screenClass: String) {
        screens.add(LoggedScreen(screenName, screenClass))
    }

    override fun setUserProperty(name: String, value: String) {
        userProperties[name] = value
    }

    override fun setUserId(userId: String?) {
        currentUserId = userId
    }

    fun hasEvent(name: String): Boolean = events.any { it.name == name }

    fun getEvents(name: String): List<LoggedEvent> = events.filter { it.name == name }

    fun clear() {
        events.clear()
        screens.clear()
        userProperties.clear()
        currentUserId = null
    }
}
