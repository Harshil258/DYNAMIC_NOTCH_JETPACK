package ai.emots.kishan_dynamic.data.notification

/** A page index alone is stale as soon as a notification is inserted/removed. */
object NotificationPagerSelectionPolicy {
    fun settledIndex(ids: List<String>, settledKey: Any?): Int? =
        if (settledKey !is String) null else ids.indexOf(settledKey).takeIf { it >= 0 }
}
