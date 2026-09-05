package ai.emots.kishan_dynamic.service

import android.media.session.MediaController
import android.media.session.MediaSession
import android.media.MediaMetadata
import android.media.session.PlaybackState
import ai.emots.kishan_dynamic.data.media.MediaSeekPolicy
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class MediaPlaybackSnapshot(
    val packageName: String,
    val title: String,
    val artist: String,
    val durationMs: Long,
    val positionMs: Long,
    val isPlaying: Boolean,
    val albumArtUri: String?
)

/** Platform command boundary for the media actions rendered by the island. */
object MediaPlaybackRegistry {
    private var controller: MediaController? = null
    private var callback: MediaController.Callback? = null
    private val refreshScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var tickerJob: kotlinx.coroutines.Job? = null
    private val _snapshot = MutableStateFlow<MediaPlaybackSnapshot?>(null)
    val snapshot: StateFlow<MediaPlaybackSnapshot?> = _snapshot.asStateFlow()

    fun replace(token: MediaSession.Token) {
        val next = runCatching { MediaControllerHolder.create(token) }.getOrNull() ?: return
        if (controller?.sessionToken == next.sessionToken) {
            publishSnapshot()
            return
        }
        clearController()
        controller = next
        callback = object : MediaController.Callback() {
            override fun onPlaybackStateChanged(state: PlaybackState?) {
                publishSnapshot()
            }

            override fun onMetadataChanged(metadata: MediaMetadata?) {
                publishSnapshot()
            }

            override fun onSessionDestroyed() {
                clearController()
            }
        }.also { next.registerCallback(it) }
        publishSnapshot()
        tickerJob?.cancel()
        tickerJob = refreshScope.launch {
            while (isActive && controller === next) {
                publishSnapshot()
                delay(if (_snapshot.value?.isPlaying == true) 1000L else 3000L)
            }
        }
    }

    fun playPause() {
        val active = controller ?: return
        runCatching {
            if (active.playbackState?.state == android.media.session.PlaybackState.STATE_PLAYING) {
                active.transportControls.pause()
            } else {
                active.transportControls.play()
            }
        }
    }

    fun skipNext() {
        runCatching { controller?.transportControls?.skipToNext() }
    }

    fun skipPrevious() {
        runCatching { controller?.transportControls?.skipToPrevious() }
    }

    fun seekTo(positionMs: Long) {
        runCatching { controller?.transportControls?.seekTo(positionMs.coerceAtLeast(0L)) }
    }

    fun seekBy(deltaMs: Long) {
        val snapshot = _snapshot.value ?: return
        seekTo(MediaSeekPolicy.targetPosition(snapshot.positionMs, snapshot.durationMs, deltaMs))
    }

    fun stopIfPackage(packageName: String) {
        if (controller?.packageName == packageName) clearController()
    }

    fun clear() {
        clearController()
    }

    private fun publishSnapshot() {
        val active = controller ?: run {
            _snapshot.value = null
            return
        }
        val metadata = active.metadata
        val title = metadata?.getString(MediaMetadata.METADATA_KEY_TITLE).orEmpty()
        if (title.isBlank()) {
            _snapshot.value = null
            return
        }
        val playbackState = active.playbackState
        _snapshot.value = MediaPlaybackSnapshot(
            packageName = active.packageName,
            title = title,
            artist = metadata?.getString(MediaMetadata.METADATA_KEY_ARTIST).orEmpty(),
            durationMs = metadata?.getLong(MediaMetadata.METADATA_KEY_DURATION) ?: 0L,
            positionMs = playbackState?.position?.coerceAtLeast(0L) ?: 0L,
            isPlaying = playbackState?.state == PlaybackState.STATE_PLAYING,
            albumArtUri = metadata?.getString(MediaMetadata.METADATA_KEY_ART_URI)
        )
    }

    private fun clearController() {
        val previous = controller
        val previousCallback = callback
        if (previous != null && previousCallback != null) {
            runCatching { previous.unregisterCallback(previousCallback) }
        }
        controller = null
        callback = null
        tickerJob?.cancel()
        tickerJob = null
        _snapshot.value = null
    }

    private object MediaControllerHolder {
        private lateinit var context: android.content.Context

        fun initialize(context: android.content.Context) {
            this.context = context.applicationContext
        }

        fun create(token: MediaSession.Token): MediaController {
            check(::context.isInitialized) { "MediaPlaybackRegistry is not initialized" }
            return MediaController(context, token)
        }
    }

    fun initialize(context: android.content.Context) {
        MediaControllerHolder.initialize(context)
    }
}
