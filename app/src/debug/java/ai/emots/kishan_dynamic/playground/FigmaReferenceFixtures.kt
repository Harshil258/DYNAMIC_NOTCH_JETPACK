package ai.emots.kishan_dynamic.playground

import android.content.Context
import ai.emots.kishan_dynamic.R
import ai.emots.kishan_dynamic.data.model.*

/** Original reference content for visual comparison only. Excluded from release builds. */
object FigmaReferenceFixtures {
    const val packageName = "figma.reference"
    fun resourceUri(context: Context, id: Int) = "android.resource://${context.packageName}/$id"
    fun messages(context: Context) = listOf(
        NotificationInfo("figma-previous", packageName, "Figma", "Design team", "The previous update is ready.",
            timestamp = 1, picturePath = resourceUri(context, R.drawable.figma_reference_message)),
        NotificationInfo("figma-selected", packageName, "Figma", "Mamad", "Haji ina chie zadi\nto baz dahane mano\nservis kardi",
            timestamp = 2, picturePath = resourceUri(context, R.drawable.figma_reference_message)),
        NotificationInfo("figma-next", packageName, "Figma", "Project update", "The next update is ready.",
            timestamp = 3, picturePath = resourceUri(context, R.drawable.figma_reference_message))
    )
    fun caller(context: Context) = ContactInfo("Tamia Castillo", "", resourceUri(context, R.drawable.figma_reference_caller))
    fun music(context: Context) = MusicTrack("Heat Waves", "Grass Animals", packageName,
        durationMs = 241000, positionMs = 50000, albumArtUri = resourceUri(context, R.drawable.music_album_art))
}
