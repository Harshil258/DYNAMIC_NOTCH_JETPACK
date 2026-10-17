package ai.emots.kishan_dynamic.data.model

/** Uses public accessibility window metadata, not private shade APIs or view IDs. */
object OverlaySystemSurfacePolicy {
    fun shouldSuppress(systemWindow: Boolean, focusedPackage: String?, ownOverlay: Boolean): Boolean =
        !ownOverlay && (systemWindow || focusedPackage == "com.android.systemui" ||
            focusedPackage?.endsWith(".permissioncontroller") == true)
}
