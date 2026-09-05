package ai.emots.kishan_dynamic.data.release

import java.io.Closeable
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

data class AnalyticsEvent(
    val name: String,
    val parameters: Map<String, String> = emptyMap()
)

interface AnalyticsSink {
    fun track(event: AnalyticsEvent)
}

/** Production-safe default: no event leaves the device until configured. */
object NoOpAnalyticsSink : AnalyticsSink {
    override fun track(event: AnalyticsEvent) = Unit
}

/** Deterministic sink for local tests and provider contract verification. */
class InMemoryAnalyticsSink : AnalyticsSink {
    private val events = mutableListOf<AnalyticsEvent>()

    override fun track(event: AnalyticsEvent) {
        events += event
    }

    fun snapshot(): List<AnalyticsEvent> = events.toList()
}

/** Stable JSON encoder kept separate so transport code remains testable. */
object AnalyticsEventJson {
    fun encode(event: AnalyticsEvent): String {
        val parameters = event.parameters.entries.joinToString(",") { (key, value) ->
            "\"${key.escapeJson()}\":\"${value.escapeJson()}\""
        }
        return "{\"name\":\"${event.name.escapeJson()}\",\"parameters\":{$parameters}}"
    }

    private fun String.escapeJson(): String = buildString(length + 8) {
        for (character in this@escapeJson) {
            when (character) {
                '\\' -> append("\\\\")
                '"' -> append("\\\"")
                '\n' -> append("\\n")
                '\r' -> append("\\r")
                '\t' -> append("\\t")
                else -> append(character)
            }
        }
    }
}

fun interface AnalyticsEventPoster {
    suspend fun post(endpoint: String, payload: String)
}

class UrlConnectionAnalyticsEventPoster(
    private val connectTimeoutMillis: Int = 4_000,
    private val readTimeoutMillis: Int = 4_000
) : AnalyticsEventPoster {
    override suspend fun post(endpoint: String, payload: String) {
        val connection = (URL(endpoint).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            doOutput = true
            connectTimeout = connectTimeoutMillis
            readTimeout = readTimeoutMillis
            instanceFollowRedirects = false
            setRequestProperty("Accept", "application/json")
            setRequestProperty("Content-Type", "application/json; charset=utf-8")
        }
        try {
            OutputStreamWriter(connection.outputStream, Charsets.UTF_8).use { writer ->
                writer.write(payload)
            }
            if (connection.responseCode !in 200..299) {
                error("Analytics request failed: HTTP ${connection.responseCode}")
            }
        } finally {
            connection.disconnect()
        }
    }
}

/**
 * Fire-and-forget analytics delivery. A failed request is intentionally
 * dropped; product behavior must never depend on analytics availability.
 */
class HttpAnalyticsSink(
    private val endpoint: String,
    private val poster: AnalyticsEventPoster = UrlConnectionAnalyticsEventPoster(),
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
) : AnalyticsSink, Closeable {
    override fun track(event: AnalyticsEvent) {
        if (endpoint.isBlank()) return
        scope.launch {
            runCatching { poster.post(endpoint, AnalyticsEventJson.encode(event)) }
        }
    }

    override fun close() {
        scope.cancel()
    }
}

/** Process-wide event boundary; disabled or unconfigured builds remain local. */
object AppAnalytics : AnalyticsSink {
    private var configuredKey: String? = null
    private var managedSink: HttpAnalyticsSink? = null
    @Volatile private var delegate: AnalyticsSink = NoOpAnalyticsSink

    @Synchronized
    fun configure(enabled: Boolean, endpoint: String) {
        val normalizedEndpoint = endpoint.trim()
        val key = "$enabled|$normalizedEndpoint"
        if (key == configuredKey) return
        managedSink?.close()
        managedSink = null
        delegate = if (enabled && normalizedEndpoint.startsWith("https://")) {
            HttpAnalyticsSink(normalizedEndpoint).also { managedSink = it }
        } else {
            NoOpAnalyticsSink
        }
        configuredKey = key
    }

    override fun track(event: AnalyticsEvent) {
        delegate.track(event)
    }
}
