package ai.emots.kishan_dynamic.data.liveactivity

import ai.emots.kishan_dynamic.data.model.LiveActivityInfo
import ai.emots.kishan_dynamic.data.model.LiveActivityKind
import ai.emots.kishan_dynamic.data.model.NotificationInfo

/**
 * Converts only authoritative ongoing notification payloads into live
 * activities. The mapper is deliberately pure so source-specific heuristics
 * stay out of the overlay and are easy to replace with app integrations later.
 */
object NotificationLiveActivityMapper {

    fun map(notification: NotificationInfo): LiveActivityInfo? {
        if (!notification.isOngoing) return null

        val packageName = notification.packageName.lowercase()
        val searchableText = listOf(
            notification.appName,
            notification.title,
            notification.text,
            notification.category
        ).joinToString(" ").lowercase()

        // STRICT SCOPE CONSTRAINT: Strictly Zero Cricket per Rule 31
        if (searchableText.contains("cricket") ||
            searchableText.contains("cricbuzz") ||
            searchableText.contains("ipl") ||
            searchableText.contains("bcci") ||
            packageName.contains("cricbuzz") ||
            packageName.contains("cricket")
        ) {
            return null
        }

        if (notification.category == "timer" || notification.category == "stopwatch") {
            return LiveActivityInfo(
                id = notification.id,
                kind = LiveActivityKind.TIMER,
                title = notification.title.ifBlank {
                    if (notification.category == "stopwatch") "Stopwatch" else "Timer"
                },
                subtitle = notification.text,
                showChronometer = notification.showChronometer,
                chronometerBaseElapsedRealtime = notification.chronometerBaseElapsedRealtime,
                chronometerCountDown = notification.chronometerCountDown,
                isExpanded = false,
                isLiveSource = true
            )
        }

        val isNavigation = notification.category == "navigation" ||
            (navigationPackages.any(packageName::contains) &&
                navigationWords.any(searchableText::contains))

        val isTransit = transitPackages.any(packageName::contains) &&
            transitWords.any(searchableText::contains)
        val isFlight = flightPackages.any(packageName::contains) &&
            flightWords.any(searchableText::contains)
        val isSports = sportsPackages.any(packageName::contains) &&
            (sportsWords.any(searchableText::contains) || Regex("""\b\d+\s*[-–]\s*\d+\b""").containsMatchIn(searchableText))
        val isSatellite = satellitePackages.any(packageName::contains) &&
            satelliteWords.any(searchableText::contains)
        val isFindMy = findMyPackages.any(packageName::contains) &&
            findMyWords.any(searchableText::contains)
        val isHandoff = handoffPackages.any(packageName::contains) &&
            handoffWords.any(searchableText::contains)
        val isScreenMirroring = mirroringPackages.any(packageName::contains) &&
            mirroringWords.any(searchableText::contains)

        if (isSatellite) {
            return LiveActivityInfo(
                id = notification.id,
                kind = LiveActivityKind.SATELLITE,
                title = notification.title.ifBlank { "Satellite connection" },
                subtitle = notification.text.ifBlank { "Emergency connectivity" },
                isExpanded = false,
                isLiveSource = true
            )
        }

        if (isFindMy) {
            return LiveActivityInfo(
                id = notification.id,
                kind = LiveActivityKind.FIND_MY,
                title = notification.title.ifBlank { "Find My" },
                subtitle = notification.text.ifBlank { "Device location update" },
                isExpanded = false,
                isLiveSource = true
            )
        }

        if (isHandoff) {
            return LiveActivityInfo(
                id = notification.id,
                kind = LiveActivityKind.MOVED_TO_IPHONE,
                title = notification.title.ifBlank { "Continue on iPhone" },
                subtitle = notification.text.ifBlank { "Handoff in progress" },
                isExpanded = false,
                isLiveSource = true
            )
        }

        if (isScreenMirroring) {
            return LiveActivityInfo(
                id = notification.id,
                kind = LiveActivityKind.SCREEN_MIRRORING_ALERT,
                title = notification.title.ifBlank { "Screen mirroring" },
                subtitle = notification.text.ifBlank { "Casting is active" },
                isExpanded = false,
                isLiveSource = true
            )
        }

        if (recordingPackages.any(packageName::contains) &&
            recordingWords.any(searchableText::contains)
        ) {
            return LiveActivityInfo(
                id = notification.id,
                kind = LiveActivityKind.SCREEN_RECORDING,
                title = notification.title.ifBlank { "Screen Recording" },
                subtitle = notification.text.ifBlank { "Recording in progress" },
                isExpanded = false,
                isLiveSource = true
            )
        }

        if (voiceRecorderPackages.any(packageName::contains) &&
            voiceRecordingWords.any(searchableText::contains)
        ) {
            return LiveActivityInfo(
                id = notification.id,
                kind = LiveActivityKind.VOICE_MEMO,
                title = notification.title.ifBlank { "Voice Memo" },
                subtitle = notification.text.ifBlank { "Recording in progress" },
                isExpanded = false,
                isLiveSource = true
            )
        }

        if (sharePackages.any(packageName::contains) &&
            shareWords.any(searchableText::contains)
        ) {
            return LiveActivityInfo(
                id = notification.id,
                kind = LiveActivityKind.AIR_DROP,
                title = notification.title.ifBlank { "Quick Share" },
                subtitle = notification.text.ifBlank { "Transfer in progress" },
                progress = notification.progressFraction(),
                isExpanded = false,
                isLiveSource = true
            )
        }

        if (isTransit) {
            return LiveActivityInfo(
                id = notification.id,
                kind = LiveActivityKind.TRANSIT_ROUTE_ALERT,
                title = notification.title.ifBlank { "Transit route" },
                subtitle = notification.text.ifBlank { "Transit in progress" },
                progress = notification.progressFraction(),
                isExpanded = false,
                isLiveSource = true
            )
        }

        if (isFlight) {
            val statusText = when {
                "landed" in searchableText -> "LANDED"
                "boarding" in searchableText -> "BOARDING"
                "delayed" in searchableText -> "DELAYED"
                else -> "ON TIME"
            }
            val flightNumber = Regex("""\b([A-Z]{2}|[A-Z]\d|\d[A-Z])\s*\d{2,4}\b""", RegexOption.IGNORE_CASE)
                .find(notification.title)?.value?.uppercase()
                ?: notification.title.substringBefore("to").removePrefix("Flight").trim().takeIf { it.isNotBlank() }
                ?: notification.title.ifBlank { "Flight" }

            return LiveActivityInfo(
                id = notification.id,
                kind = LiveActivityKind.FLIGHT,
                title = notification.title.ifBlank { "Flight in progress" },
                subtitle = notification.text.ifBlank { "Flight status" },
                progress = notification.progressFraction(),
                isExpanded = false,
                entityName = flightNumber,
                secondaryText = notification.text,
                statusBadgeText = statusText,
                isLiveSource = true
            )
        }

        if (isSports) {
            val scoreMatch = Regex("""\b\d+\s*[-–]\s*\d+\b""").find(notification.title)
                ?: Regex("""\b\d+\s*[-–]\s*\d+\b""").find(notification.text)
            val score = scoreMatch?.value ?: "0 - 0"
            return LiveActivityInfo(
                id = notification.id,
                kind = LiveActivityKind.SPORTS,
                title = notification.title.ifBlank { "Live match" },
                subtitle = notification.text.ifBlank { "Score update" },
                progress = notification.progressFraction(),
                isExpanded = false,
                entityName = notification.title,
                secondaryText = notification.text,
                statusBadgeText = "LIVE",
                primaryMetric = score,
                isLiveSource = true
            )
        }

        if (isNavigation) {
            val maneuver = extractNavigationManeuver(searchableText)
            val distance = extractDistance(notification.title) ?: extractDistance(notification.text) ?: "500m"
            val road = notification.text.ifBlank { notification.title }

            return LiveActivityInfo(
                id = notification.id,
                kind = LiveActivityKind.NAVIGATION,
                title = notification.title.ifBlank { notification.appName },
                subtitle = notification.text.ifBlank { "Navigation in progress" },
                progress = notification.progressFraction(),
                isExpanded = false,
                entityName = notification.appName,
                secondaryText = notification.expandedText,
                statusBadgeText = "IN ${distance.uppercase()}",
                primaryMetric = distance,
                secondaryMetric = road,
                maneuverType = maneuver,
                isLiveSource = true
            )
        }

        val isDelivery = deliveryPackages.any(packageName::contains) &&
            deliveryWords.any(searchableText::contains)
        if (isDelivery) {
            val status = extractDeliveryStatus(searchableText)
            val eta = extractDeliveryEta(searchableText) ?: "Arriving soon"
            val phoneAction = notification.actions.firstOrNull { it.label.contains("call", ignoreCase = true) }?.id
            val progress = if (status == "DELIVERED") 1.0f else notification.progressFraction()
            val restaurantName = notification.title.substringBefore("order").trim().takeIf { it.isNotBlank() }
                ?: notification.appName

            return LiveActivityInfo(
                id = notification.id,
                kind = LiveActivityKind.DELIVERY,
                title = notification.title.ifBlank { notification.appName },
                subtitle = notification.text.ifBlank { "Delivery in progress" },
                progress = progress,
                isExpanded = false,
                entityName = restaurantName,
                secondaryText = notification.text,
                statusBadgeText = status,
                primaryMetric = eta,
                secondaryMetric = notification.subText ?: "Delivery Partner",
                actionPhoneNumber = phoneAction,
                isLiveSource = true
            )
        }

        return null
    }

    private fun NotificationInfo.progressFraction(): Float? =
        progressMax.takeIf { it > 0 }
            ?.let { (progress.toFloat() / it.toFloat()).coerceIn(0f, 1f) }

    private val navigationPackages = setOf(
        "com.google.android.apps.maps",
        "com.waze",
        "com.here.app.maps"
    )

    private val navigationWords = setOf(
        "turn-by-turn",
        "navigation",
        "directions",
        "continue straight",
        "route"
    )

    private val transitPackages = setOf(
        "com.google.android.apps.maps",
        "com.citymapper.app.release",
        "com.thetransitapp.droid",
        "de.hafas.android.db"
    )

    private val transitWords = setOf(
        "transit",
        "bus",
        "train",
        "metro",
        "subway",
        "tram",
        "platform",
        "departure",
        "arrives"
    )

    private val flightPackages = setOf(
        "com.google.android.googlequicksearchbox",
        "com.flightaware.android.flightaware",
        "com.flightradar24free",
        "com.flightyapp.flighty",
        "delta",
        "united",
        "american",
        "airline",
        "flight"
    )

    private val flightWords = setOf(
        "flight",
        "boarding",
        "boarding pass",
        "gate",
        "takeoff",
        "take off",
        "landing",
        "departing",
        "arriving"
    )

    private val sportsPackages = setOf(
        "com.espn.score_center",
        "com.google.android.googlequicksearchbox",
        "com.onefootball",
        "com.fotmob.android",
        "fotmob",
        "sofascore",
        "espn",
        "onefootball"
    )

    private val sportsWords = setOf(
        "live score",
        "match is live",
        "halftime",
        "quarter",
        "inning",
        "final score",
        " vs ",
        "score update"
    )

    private val recordingWords = setOf(
        "screen recording",
        "screen recorder",
        "recording screen"
    )

    private val recordingPackages = setOf(
        "android",
        "com.android.systemui",
        "com.google.android.apps.recorder",
        "com.samsung.android.app.smartcapture",
        "com.miui.screenrecorder"
    )

    private val voiceRecorderPackages = setOf(
        "com.google.android.apps.recorder",
        "com.sec.android.app.voicenote",
        "com.miui.voicerecorder",
        "com.android.soundrecorder"
    )

    private val voiceRecordingWords = setOf(
        "recording",
        "recording audio",
        "voice memo",
        "voice recorder"
    )

    private val sharePackages = setOf(
        "com.google.android.gms",
        "com.samsung.android.app.sharelive"
    )

    private val shareWords = setOf(
        "nearby share",
        "quick share"
    )

    private val satellitePackages = setOf(
        "com.android.phone",
        "com.android.systemui",
        "com.google.android.dialer",
        "com.samsung.android.dialer"
    )

    private val satelliteWords = setOf(
        "satellite",
        "emergency satellite",
        "satellite connection",
        "via satellite"
    )

    private val findMyPackages = setOf(
        "com.google.android.apps.adm",
        "com.google.android.gms",
        "com.samsung.android.fmm"
    )

    private val findMyWords = setOf(
        "find my device",
        "device located",
        "device location",
        "nearby tracker",
        "item found"
    )

    private val handoffPackages = setOf(
        "com.microsoft.appmanager",
        "com.microsoft.windowsintune.companyportal",
        "com.samsung.android.mdx.quickboard",
        "com.samsung.android.bixby.agent"
    )

    private val handoffWords = setOf(
        "continue on phone",
        "continue on iphone",
        "moved to iphone",
        "pick up on your phone",
        "handoff"
    )

    private val mirroringPackages = setOf(
        "com.android.systemui",
        "com.google.android.gms",
        "com.google.android.apps.chromecast.app",
        "com.samsung.android.smartmirroring",
        "com.samsung.android.smartview"
    )

    private val mirroringWords = setOf(
        "screen mirroring",
        "screen cast",
        "screencast",
        "casting screen",
        "cast screen",
        "stop casting",
        "screen sharing"
    )

    private val deliveryPackages = setOf(
        "swiggy",
        "zomato",
        "ubercab",
        "ubereats",
        "doordash",
        "wolt"
    )

    private val deliveryWords = setOf(
        "delivery in progress",
        "out for delivery",
        "arriving soon",
        "order is on the way",
        "delivered",
        "arriving",
        "order",
        "meal"
    )

    private fun extractNavigationManeuver(text: String): String {
        val lower = text.lowercase()
        return when {
            "turn right" in lower || "sharp right" in lower || "bear right" in lower || "keep right" in lower || "exit right" in lower -> "right"
            "turn left" in lower || "sharp left" in lower || "bear left" in lower || "keep left" in lower || "exit left" in lower -> "left"
            "roundabout" in lower || "rotary" in lower -> "roundabout"
            "u-turn" in lower || "uturn" in lower -> "uturn"
            "merge" in lower -> "merge"
            else -> "straight"
        }
    }

    private fun extractDistance(text: String): String? {
        val match = Regex("""\b(\d+(?:\.\d+)?)\s*(m|km|ft|mi|miles|meters|feet)\b""", RegexOption.IGNORE_CASE).find(text)
        return match?.value
    }

    private fun extractDeliveryEta(text: String): String? {
        val match = Regex("""\b(arriving in \d+\s*(?:mins|min|m|minutes)?|\d+\s*(?:mins|min|m|minutes))\b""", RegexOption.IGNORE_CASE).find(text)
        return match?.value
    }

    private fun extractDeliveryStatus(text: String): String {
        val lower = text.lowercase()
        return when {
            "delivered" in lower -> "DELIVERED"
            "out for delivery" in lower -> "OUT FOR DELIVERY"
            "almost there" in lower || "arriving soon" in lower || "arriving" in lower -> "ARRIVING"
            "picked up" in lower -> "PICKED UP"
            "preparing" in lower || "kitchen" in lower -> "PREPARING"
            else -> "ON THE WAY"
        }
    }
}

