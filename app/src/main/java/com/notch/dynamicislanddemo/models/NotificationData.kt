package com.notch.dynamicislanddemo.models

/**
 * Simple notification data model
 */
data class NotificationData(
    val packageName: String,
    val title: String,
    val text: String,
    val appName: String,
    val postTime: Long,
    val icon: ByteArray? = null,
    val largeIcon: ByteArray? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as NotificationData

        if (packageName != other.packageName) return false
        if (title != other.title) return false
        if (postTime != other.postTime) return false

        return true
    }

    override fun hashCode(): Int {
        var result = packageName.hashCode()
        result = 31 * result + title.hashCode()
        result = 31 * result + postTime.hashCode()
        return result
    }
}
