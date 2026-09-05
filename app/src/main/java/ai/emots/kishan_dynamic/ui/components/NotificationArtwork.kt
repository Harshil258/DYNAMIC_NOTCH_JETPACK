package ai.emots.kishan_dynamic.ui.components

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Loads notification-provided artwork off the main thread and keeps the app icon as a safe
 * fallback for notifications that do not contain a large image.
 */
@Composable
fun NotificationArtwork(
    imagePath: String?,
    packageName: String?,
    modifier: Modifier = Modifier,
    contentDescription: String = "Notification artwork"
) {
    val bitmap by produceState<Bitmap?>(initialValue = null, key1 = imagePath) {
        value = if (imagePath.isNullOrBlank()) {
            null
        } else {
            withContext(Dispatchers.IO) {
                runCatching { BitmapFactory.decodeFile(imagePath) }.getOrNull()
            }
        }
    }

    if (bitmap != null) {
        Image(
            bitmap = bitmap!!.asImageBitmap(),
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = ContentScale.Crop
        )
    } else {
        PackageIcon(
            packageName = packageName,
            modifier = modifier,
            contentDescription = contentDescription
        )
    }
}
