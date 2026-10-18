package ai.emots.kishan_dynamic.data.notification

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.Icon
import android.os.Build
import android.os.Bundle
import androidx.collection.LruCache
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

/**
 * Thread-safe, bounded, asynchronous repository for extracting, downscaling, and caching
 * notification artwork off the main looper thread.
 *
 * Enforces:
 * 1. Zero main-thread bitmap compression or disk operations.
 * 2. Two-tier caching (in-memory LRU cache + bounded disk cache).
 * 3. Stale result protection via monotonic generation tracking.
 * 4. Bounded parallelism (limitedParallelism) to guard against CPU/memory spikes during bursts.
 * 5. Strict privacy protection: sensitive notification pictures are never written to disk.
 */
class NotificationArtworkRepository(
    private val context: Context,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val mainDispatcher: CoroutineDispatcher = try {
        val dispatcher = Dispatchers.Main
        dispatcher.isDispatchNeeded(kotlin.coroutines.EmptyCoroutineContext)
        dispatcher
    } catch (_: Throwable) {
        Dispatchers.Default
    }
) {
    companion object {
        const val MAX_IMAGE_DIMENSION = 512
        const val CACHE_DIR_NAME = "notification_artwork"
        const val MAX_DISK_FILES = 40
        const val PRUNE_INTERVAL_MILLIS = 5 * 60 * 1000L // Prune at most once per 5 minutes
    }

    private val repositoryJob = SupervisorJob()
    // Limit concurrency to 2 background workers so 25-event bursts do not starve device resources
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    private val repositoryScope = CoroutineScope(ioDispatcher.limitedParallelism(2) + repositoryJob)

    // In-memory LRU cache: maps notificationId_avatar and notificationId_picture to cached file path
    private val memoryCache = LruCache<String, String>(64)

    // Monotonic generation tracking per notification ID to drop stale/outdated asynchronous results
    private val activeGenerations = ConcurrentHashMap<String, Long>()
    private val generationSequence = AtomicLong(0L)
    private val activeJobs = ConcurrentHashMap<String, Job>()

    @Volatile
    private var lastPruneTimestamp = 0L

    /**
     * Retrieves the cached avatar path from memory cache if already extracted.
     */
    fun getCachedAvatarPath(notificationId: String): String? {
        return memoryCache.get("${notificationId}_avatar")
    }

    /**
     * Retrieves the cached picture path from memory cache if already extracted.
     */
    fun getCachedPicturePath(notificationId: String): String? {
        return memoryCache.get("${notificationId}_picture")
    }

    /**
     * Asynchronously processes notification avatar and picture off the main looper.
     * Invokes [onArtworkReady] on [Dispatchers.Main] ONLY if the generation is still current
     * and the notification has not been superseded or removed.
     */
    fun processArtworkAsync(
        notificationId: String,
        extras: Bundle,
        isSensitive: Boolean,
        onArtworkReady: (avatarPath: String?, picturePath: String?) -> Unit
    ) {
        val generation = generationSequence.incrementAndGet()
        activeGenerations[notificationId] = generation

        // Cancel previous pending extraction for the same notification ID
        activeJobs.remove(notificationId)?.cancel()

        val job = repositoryScope.launch {
            val (avatarPath, picturePath) = processArtworkInternal(notificationId, extras, isSensitive)

            // Verify generation: if a newer update arrived or notification was dismissed, drop late result!
            if (activeGenerations[notificationId] != generation) {
                return@launch
            }

            if (avatarPath != null || picturePath != null) {
                withContext(mainDispatcher) {
                    if (activeGenerations[notificationId] == generation) {
                        onArtworkReady(avatarPath, picturePath)
                    }
                }
            }
        }
        activeJobs[notificationId] = job
    }

    /**
     * Cancels any pending processing job for the given notification ID and invalidates its generation.
     */
    fun cancel(notificationId: String) {
        activeGenerations.remove(notificationId)
        activeJobs.remove(notificationId)?.cancel()
    }

    /**
     * Invalidates memory and disk cache for a dismissed notification.
     */
    fun removeCachedArtwork(notificationId: String) {
        cancel(notificationId)
        memoryCache.remove("${notificationId}_avatar")
        memoryCache.remove("${notificationId}_picture")
        repositoryScope.launch {
            runCatching {
                val hash = notificationId.hashCode()
                val dir = File(context.cacheDir, CACHE_DIR_NAME)
                File(dir, "${hash}_avatar.jpg").delete()
                File(dir, "${hash}_picture.jpg").delete()
                File(dir, "${hash}_avatar.png").delete()
                File(dir, "${hash}_picture.png").delete()
            }
        }
    }

    private suspend fun processArtworkInternal(
        notificationId: String,
        extras: Bundle,
        isSensitive: Boolean
    ): Pair<String?, String?> = withContext(ioDispatcher) {
        val hash = notificationId.hashCode()

        // 1. Avatar extraction
        val avatarKey = "${notificationId}_avatar"
        var avatarPath = memoryCache.get(avatarKey)
        if (avatarPath == null) {
            val avatarBitmap = runCatching { extractAvatarBitmap(extras) }.getOrNull()
            if (avatarBitmap != null) {
                avatarPath = saveBitmapBounded(avatarBitmap, "${hash}_avatar.jpg")
                if (avatarPath != null) {
                    memoryCache.put(avatarKey, avatarPath)
                }
            }
        }

        // 2. Picture attachment extraction (strictly suppressed if notification content is sensitive)
        val pictureKey = "${notificationId}_picture"
        var picturePath = memoryCache.get(pictureKey)
        if (picturePath == null && !isSensitive) {
            val pictureBitmap = runCatching { extractPictureBitmap(extras) }.getOrNull()
            if (pictureBitmap != null) {
                picturePath = saveBitmapBounded(pictureBitmap, "${hash}_picture.jpg")
                if (picturePath != null) {
                    memoryCache.put(pictureKey, picturePath)
                }
            }
        }

        // 3. Periodic background cleanup
        maybePruneDiskCache()

        Pair(avatarPath, picturePath)
    }

    private fun extractAvatarBitmap(extras: Bundle): Bitmap? {
        return readBitmapExtra(extras, android.app.Notification.EXTRA_LARGE_ICON_BIG)
            ?: readBitmapExtra(extras, android.app.Notification.EXTRA_LARGE_ICON)
            ?: readIconExtra(extras, android.app.Notification.EXTRA_LARGE_ICON_BIG)?.toBoundedBitmap()
            ?: readIconExtra(extras, android.app.Notification.EXTRA_LARGE_ICON)?.toBoundedBitmap()
            ?: extractMessagingSenderIcon(extras)
    }

    private fun extractPictureBitmap(extras: Bundle): Bitmap? {
        return readBitmapExtra(extras, android.app.Notification.EXTRA_PICTURE)
            ?: (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                readIconExtra(extras, android.app.Notification.EXTRA_PICTURE_ICON)?.toBoundedBitmap()
            } else null)
    }

    private fun extractMessagingSenderIcon(extras: Bundle): Bitmap? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) return null
        return runCatching {
            @Suppress("DEPRECATION")
            val person = extras.getParcelable(android.app.Notification.EXTRA_MESSAGING_PERSON) as? android.app.Person
            person?.icon?.loadDrawable(context)?.toBoundedBitmap()
        }.getOrNull()
    }

    @Suppress("DEPRECATION")
    private fun readBitmapExtra(extras: Bundle, key: String): Bitmap? =
        extras.get(key) as? Bitmap

    @Suppress("DEPRECATION")
    private fun readIconExtra(extras: Bundle, key: String): Drawable? {
        val icon = extras.get(key) as? Icon
        return runCatching { icon?.loadDrawable(context) }.getOrNull()
    }

    private fun Drawable.toBoundedBitmap(): Bitmap {
        if (this is BitmapDrawable && this.bitmap != null) {
            return this.bitmap.scaleDownIfNeeded()
        }
        val w = intrinsicWidth.coerceIn(1, MAX_IMAGE_DIMENSION)
        val h = intrinsicHeight.coerceIn(1, MAX_IMAGE_DIMENSION)
        val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        setBounds(0, 0, canvas.width, canvas.height)
        draw(canvas)
        return bitmap
    }

    private fun Bitmap.scaleDownIfNeeded(): Bitmap {
        val maxDim = maxOf(width, height)
        if (maxDim <= MAX_IMAGE_DIMENSION) return this
        val ratio = MAX_IMAGE_DIMENSION.toFloat() / maxDim
        val newW = (width * ratio).toInt().coerceAtLeast(1)
        val newH = (height * ratio).toInt().coerceAtLeast(1)
        return Bitmap.createScaledBitmap(this, newW, newH, true)
    }

    private fun saveBitmapBounded(bitmap: Bitmap, filename: String): String? {
        val scaled = bitmap.scaleDownIfNeeded()
        val dir = File(context.cacheDir, CACHE_DIR_NAME).apply { mkdirs() }
        val file = File(dir, filename)
        return runCatching {
            FileOutputStream(file).use { out ->
                scaled.compress(Bitmap.CompressFormat.JPEG, 85, out)
            }
            file.absolutePath
        }.getOrNull()
    }

    private fun maybePruneDiskCache() {
        val now = System.currentTimeMillis()
        if (now - lastPruneTimestamp < PRUNE_INTERVAL_MILLIS) return
        lastPruneTimestamp = now
        runCatching {
            val dir = File(context.cacheDir, CACHE_DIR_NAME)
            if (dir.exists()) {
                val files = dir.listFiles() ?: return@runCatching
                if (files.size > MAX_DISK_FILES) {
                    files.sortedByDescending { it.lastModified() }
                        .drop(MAX_DISK_FILES)
                        .forEach { it.delete() }
                }
            }
        }
    }
}
