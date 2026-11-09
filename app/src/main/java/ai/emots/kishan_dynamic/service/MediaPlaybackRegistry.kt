package ai.emots.kishan_dynamic.service

import android.media.session.MediaController
import android.media.session.MediaSession
import android.media.MediaMetadata
import android.media.session.PlaybackState
import ai.emots.kishan_dynamic.data.media.MediaSeekPolicy
import ai.emots.kishan_dynamic.data.media.MediaPlaybackPositionPolicy
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
    val albumArtUri: String?,
    val supportedActions: Long = 0L
)

/** Platform command boundary for the media actions rendered by the island. */
object MediaPlaybackRegistry {
    private var controller: MediaController? = null
    private var callback: MediaController.Callback? = null
    private val refreshScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var tickerJob: kotlinx.coroutines.Job? = null
    private var pauseTimeoutJob: kotlinx.coroutines.Job? = null
    private var lastSessionSwitchTimestamp: Long = 0L
    private val _snapshot = MutableStateFlow<MediaPlaybackSnapshot?>(null)
    val snapshot: StateFlow<MediaPlaybackSnapshot?> = _snapshot.asStateFlow()

    const val PAUSE_AUTO_COLLAPSE_DELAY_MS = 3500L
    private const val SESSION_SWITCH_DEBOUNCE_MS = 500L

    val audioOnlyPackages = setOf(
        "com.spotify.music",
        "com.google.android.apps.youtube.music",
        "com.apple.android.music",
        "com.amazon.mp3",
        "deezer.android.app",
        "com.tidal.mobile",
        "com.soundcloud.android",
        "com.jio.media.jiobeats",
        "com.bsbportal.music",
        "com.gaana"
    )

    val videoStreamingPackages = setOf(
        "com.google.android.youtube",
        "com.netflix.mediaclient",
        "com.amazon.avod.thirdpartyclient",
        "in.startv.hotstar",
        "com.mxtech.videoplayer.ad",
        "com.mxtech.videoplayer.pro",
        "org.videolan.vlc",
        "tv.twitch.android.app"
    )

    fun isAudioFirstApp(packageName: String): Boolean =
        audioOnlyPackages.any { packageName.contains(it, ignoreCase = true) }

    fun isVideoStreamingApp(packageName: String): Boolean =
        videoStreamingPackages.any { packageName.contains(it, ignoreCase = true) }

    fun canTakeOwnership(candidatePackage: String, candidatePlaying: Boolean): Boolean {
        val current = controller ?: return true
        val currentPkg = current.packageName
        if (currentPkg.equals(candidatePackage, ignoreCase = true)) return true

        val currentPlaying = current.playbackState?.state == PlaybackState.STATE_PLAYING
        val isCurrentAudio = isAudioFirstApp(currentPkg)
        val isCandidateVideo = isVideoStreamingApp(candidatePackage)

        // AUR-P2-018: Audio-first stream has primary ownership lock over video sessions
        if (isCurrentAudio && currentPlaying && isCandidateVideo && !candidatePlaying) {
            return false
        }
        if (isCurrentAudio && currentPlaying && isCandidateVideo) {
            return false
        }

        // Debounce rapid switching between two media players (< 500ms)
        val now = android.os.SystemClock.elapsedRealtime()
        if (now - lastSessionSwitchTimestamp < SESSION_SWITCH_DEBOUNCE_MS && !candidatePlaying) {
            return false
        }

        return true
    }

    fun replace(token: MediaSession.Token) {
        val next = runCatching { MediaControllerHolder.create(token) }.getOrNull() ?: return
        if (controller?.sessionToken == next.sessionToken) {
            publishSnapshot()
            return
        }

        val nextPkg = next.packageName
        val nextPlaying = next.playbackState?.state == PlaybackState.STATE_PLAYING
        if (!canTakeOwnership(nextPkg, nextPlaying)) {
            return
        }

        lastSessionSwitchTimestamp = android.os.SystemClock.elapsedRealtime()
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
            pauseTimeoutJob?.cancel()
            pauseTimeoutJob = null
            _snapshot.value = null
            updateTicker(false)
            return
        }
        val metadata = active.metadata
        val title = metadata?.getString(MediaMetadata.METADATA_KEY_TITLE).orEmpty()
        if (title.isBlank()) {
            pauseTimeoutJob?.cancel()
            pauseTimeoutJob = null
            _snapshot.value = null
            updateTicker(false)
            return
        }
        val playbackState = active.playbackState
        val isPlaying = playbackState?.state == PlaybackState.STATE_PLAYING

        if (isPlaying) {
            pauseTimeoutJob?.cancel()
            pauseTimeoutJob = null
        } else {
            // AUR-P2-017: Auto-collapse paused media after 3.5s of paused state
            if (pauseTimeoutJob == null && _snapshot.value != null) {
                val currentController = controller
                pauseTimeoutJob = refreshScope.launch {
                    delay(PAUSE_AUTO_COLLAPSE_DELAY_MS)
                    if (controller === currentController) {
                        _snapshot.value = null
                        updateTicker(false)
                    }
                }
            }
        }

        _snapshot.value = MediaPlaybackSnapshot(
            packageName = active.packageName,
            title = title,
            artist = metadata?.getString(MediaMetadata.METADATA_KEY_ARTIST).orEmpty(),
            durationMs = metadata?.getLong(MediaMetadata.METADATA_KEY_DURATION) ?: 0L,
            positionMs = MediaPlaybackPositionPolicy.position(
                playbackState?.position ?: 0L,
                metadata?.getLong(MediaMetadata.METADATA_KEY_DURATION) ?: 0L,
                isPlaying,
                playbackState?.playbackSpeed ?: 0f,
                playbackState?.lastPositionUpdateTime ?: 0L,
                android.os.SystemClock.elapsedRealtime()
            ),
            isPlaying = isPlaying,
            albumArtUri = metadata?.getString(MediaMetadata.METADATA_KEY_ART_URI),
            supportedActions = playbackState?.actions ?: 0L
        )
        updateTicker(isPlaying)
    }

    private fun updateTicker(isPlaying: Boolean) {
        if (!isPlaying) {
            tickerJob?.cancel()
            tickerJob = null
            return
        }
        if (tickerJob?.isActive == true) return
        val currentController = controller ?: return
        tickerJob = refreshScope.launch {
            while (isActive && controller === currentController) {
                delay(1000L)
                publishSnapshot()
            }
        }
    }

    private fun clearController() {
        pauseTimeoutJob?.cancel()
        pauseTimeoutJob = null
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
