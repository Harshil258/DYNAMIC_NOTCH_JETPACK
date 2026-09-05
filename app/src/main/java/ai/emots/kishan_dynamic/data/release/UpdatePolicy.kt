package ai.emots.kishan_dynamic.data.release

enum class UpdateType {
    NONE,
    SOFT,
    FORCE
}

data class UpdateDecision(
    val type: UpdateType,
    val currentVersionCode: Int,
    val latestVersionCode: Int,
    val notes: List<String> = emptyList()
)

/** Pure version decisioning; transport and UI stay outside this policy. */
object UpdatePolicy {
    fun decide(
        currentVersionCode: Int,
        latestVersionCode: Int,
        minimumVersionCode: Int,
        enabled: Boolean,
        notes: List<String> = emptyList()
    ): UpdateDecision {
        val type = when {
            !enabled || latestVersionCode <= currentVersionCode -> UpdateType.NONE
            minimumVersionCode > currentVersionCode -> UpdateType.FORCE
            else -> UpdateType.SOFT
        }
        return UpdateDecision(
            type = type,
            currentVersionCode = currentVersionCode,
            latestVersionCode = latestVersionCode,
            notes = notes
        )
    }
}
