package ai.emots.kishan_dynamic.data.model

/**
 * Keeps persisted app shortcuts aligned with launchable packages while
 * preserving the user's remaining order.
 */
fun reconcilePinnedApps(
    pinnedApps: List<ActionAppShortcut>,
    availablePackages: Set<String>
): List<ActionAppShortcut> {
    if (availablePackages.isEmpty()) return pinnedApps

    return pinnedApps
        .filter { it.packageName in availablePackages }
        .mapIndexed { index, app -> app.copy(order = index) }
}
