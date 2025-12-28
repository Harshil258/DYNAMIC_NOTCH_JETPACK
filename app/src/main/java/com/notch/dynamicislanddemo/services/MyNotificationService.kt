package com.notch.dynamicislanddemo.services

import android.annotation.SuppressLint
import android.app.Notification
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.DisplayMetrics
import android.view.WindowManager
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import com.notch.dynamicislanddemo.models.ActionParsable
import com.notch.dynamicislanddemo.utils.MyHelper
import com.notch.dynamicislanddemo.utils.Utils
import java.io.ByteArrayOutputStream
import android.content.ComponentName

class MyNotificationService : NotificationListenerService() {
    var context: Context? = null
    var handler: Handler = Handler()

    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(commandReceiver)
    }

    override fun onCreate() {
        super.onCreate()
        this.context = getApplicationContext()
        instance = this
        
        // Register connection listener to ensure we can access media sessions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
             // Rebind request if needed
        }
        
        setupMediaSessionListener()
    }
    
    private var mediaSessionManager: MediaSessionManager? = null
    private var currentController: MediaController? = null
    
    private val mediaCallback = object : MediaController.Callback() {
        override fun onPlaybackStateChanged(state: PlaybackState?) {
            super.onPlaybackStateChanged(state)
            broadcastMediaUpdate()
        }

        override fun onMetadataChanged(metadata: MediaMetadata?) {
            super.onMetadataChanged(metadata)
            broadcastMediaUpdate()
        }
    }
    
    private fun setupMediaSessionListener() {
        try {
            mediaSessionManager = getSystemService(Context.MEDIA_SESSION_SERVICE) as MediaSessionManager
            val componentName = ComponentName(this, MyNotificationService::class.java)
            
            val sessionsListener = MediaSessionManager.OnActiveSessionsChangedListener { controllers ->
                if (controllers != null && controllers.isNotEmpty()) {
                     val controller = controllers.firstOrNull { 
                        val state = it.playbackState?.state
                        state == PlaybackState.STATE_PLAYING || state == PlaybackState.STATE_BUFFERING
                    } ?: controllers.first()
                    
                    updateCurrentController(controller)
                }
            }
            
            mediaSessionManager?.addOnActiveSessionsChangedListener(sessionsListener, componentName)
            
            // Initial check
            val initialSessions = mediaSessionManager?.getActiveSessions(componentName)
            if (!initialSessions.isNullOrEmpty()) {
                 val controller = initialSessions.firstOrNull { 
                    val state = it.playbackState?.state
                    state == PlaybackState.STATE_PLAYING 
                } ?: initialSessions.first()
                updateCurrentController(controller)
            }
            
            // Register command receiver
            val filter = android.content.IntentFilter("ACTION_MEDIA_CONTROL")
            LocalBroadcastManager.getInstance(this).registerReceiver(commandReceiver, filter)
            
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }
    
    private val commandReceiver = object : android.content.BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "ACTION_MEDIA_CONTROL") {
                val command = intent.getStringExtra("command")
                val controller = currentController ?: return
                
                when (command) {
                    "PLAY_PAUSE" -> {
                        val state = controller.playbackState?.state
                        if (state == PlaybackState.STATE_PLAYING) {
                            controller.transportControls.pause()
                        } else {
                            controller.transportControls.play()
                        }
                    }
                    "NEXT" -> controller.transportControls.skipToNext()
                    "PREVIOUS" -> controller.transportControls.skipToPrevious()
                    "SEEK" -> {
                        val seekTo = intent.getLongExtra("seek_to", 0L)
                        controller.transportControls.seekTo(seekTo)
                    }
                }
            }
        }
    }
    
    private fun updateCurrentController(controller: MediaController) {
        if (currentController?.packageName == controller.packageName) return // Same controller
        
        currentController?.unregisterCallback(mediaCallback)
        currentController = controller
        currentController?.registerCallback(mediaCallback)
        
        // Immediate update
        broadcastMediaUpdate()
    }
    
    private fun broadcastMediaUpdate() {
        val controller = currentController ?: return
        val metadata = controller.metadata ?: return
        val playbackState = controller.playbackState
        
        val isPlaying = playbackState?.state == PlaybackState.STATE_PLAYING
        
        val title = metadata.getString(MediaMetadata.METADATA_KEY_TITLE) ?: "Unknown"
        val artist = metadata.getString(MediaMetadata.METADATA_KEY_ARTIST) ?: "Unknown"
        val albumArt = metadata.getBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART)
        val duration = metadata.getLong(MediaMetadata.METADATA_KEY_DURATION)
        
        val intent = Intent(Utils.FROM_NOTIFICATION_SERVICE + packageName).apply {
            action = "ACTION_MEDIA_UPDATE" // Distinct action for media
            putExtra("is_playing", isPlaying)
            putExtra("title", title)
            putExtra("artist", artist)
            putExtra("duration", duration)
            putExtra("current_time", playbackState?.position ?: 0L)
            
            if (albumArt != null) {
                // Compress bitmap to avoid transaction limits
               putExtra("album_art", getByteArrayFromBitmap(albumArt))
            }
        }
        
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        // NotificationListenerService.requestRebind(new ComponentName(this, NotificationListenerService.class));
    }

    override fun onNotificationPosted(
        statusBarNotification: StatusBarNotification,
        rankingMap: RankingMap?
    ) {
        super.onNotificationPosted(statusBarNotification, rankingMap)
        this.handler.postDelayed(object : Runnable {
            override fun run() {
                sendNotification(statusBarNotification, true)
            }
        }, 100)
    }


    override fun onNotificationPosted(sbn: StatusBarNotification) {
        super.onNotificationPosted(sbn)
        handler.postDelayed(Runnable { sendNotification(sbn, true) }, 100)
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        handler.postDelayed(Runnable { sendNotification(sbn, false) }, 100)
    }

    @SuppressLint("ResourceType")
    private fun sendNotification(sbn: StatusBarNotification, isAdded: Boolean) {
        val notification = sbn.getNotification()
        if (notification == null) return

        val ctx = applicationContext
        val extras = notification.extras
        val empty = ""

        /* ---------------- APP NAME ---------------- */
        var appName: String?
        try {
            appName = getPackageManager()
                .getApplicationLabel(
                    getPackageManager().getApplicationInfo(
                        sbn.getPackageName(), 0
                    )
                )
                .toString()
        } catch (e: Exception) {
            appName = empty
        }

        /* ---------------- BASIC INFO ---------------- */
        val packageName = sbn.getPackageName()
        val category = if (notification.category != null) notification.category else empty
        val ticker =
            if (notification.tickerText != null) notification.tickerText.toString() else null

        val largeIcon = if (notification.getLargeIcon() != null)
            drawableToBmp(ctx, notification.getLargeIcon().loadDrawable(ctx), 50)
        else
            null

        /* ---------------- EXTRAS ---------------- */
        var template = empty
        var infoText = empty
        var title = empty
        var subText = empty
        var text = empty
        var bigText = empty
        var titleBig = empty
        var summaryText = empty
        var substName: String? = empty

        var progressMax = 0
        var progress = 0
        var progressIndeterminate = false
        var showChronometer = false
        var isGroupConversation = false

        if (extras != null) {
            template = getStringExtra(extras, NotificationCompat.EXTRA_TEMPLATE)
            if (!template.isEmpty() && template.contains("$")) {
                template = template.substring(template.indexOf("$") + 1)
            }

            infoText = getStringExtra(extras, NotificationCompat.EXTRA_INFO_TEXT)
            title = getCharSequenceExtra(extras, NotificationCompat.EXTRA_TITLE)
            subText = getCharSequenceExtra(extras, NotificationCompat.EXTRA_SUB_TEXT)
            text = getCharSequenceExtra(extras, NotificationCompat.EXTRA_TEXT)
            bigText = getCharSequenceExtra(extras, NotificationCompat.EXTRA_BIG_TEXT)
            titleBig = getCharSequenceExtra(extras, NotificationCompat.EXTRA_TITLE_BIG)
            summaryText = getCharSequenceExtra(extras, NotificationCompat.EXTRA_SUMMARY_TEXT)
            substName = extras.getString("android.substName", empty)

            progressMax = extras.getInt(NotificationCompat.EXTRA_PROGRESS_MAX, 0)
            progress = extras.getInt(NotificationCompat.EXTRA_PROGRESS, 0)
            progressIndeterminate =
                extras.getBoolean(NotificationCompat.EXTRA_PROGRESS_INDETERMINATE, false)
            showChronometer = extras.getBoolean(NotificationCompat.EXTRA_SHOW_CHRONOMETER, false)

            if (extras.containsKey(NotificationCompat.EXTRA_CONVERSATION_TITLE)) {
                appName = appName + " . " + extras.getCharSequence(
                    NotificationCompat.EXTRA_CONVERSATION_TITLE, empty
                )
            }

            if (Build.VERSION.SDK_INT < 28) {
                isGroupConversation = extras.getBoolean(
                    NotificationCompat.EXTRA_IS_GROUP_CONVERSATION, false
                )
            }
        }

        /* ---------------- ICON ---------------- */
        val iconDrawable: Drawable?
        var iconBitmap: Bitmap? = null
        try {
            iconDrawable = ContextCompat.getDrawable(
                createPackageContext(packageName, 0),
                notification.icon
            )
            if (iconDrawable != null) {
                iconBitmap = drawableToBmp(null, iconDrawable, 20)
            }
        } catch (ignored: Exception) {
        } catch (ignored: OutOfMemoryError) {
        }

        /* ---------------- INTENT ---------------- */
        val intent = Intent(Utils.FROM_NOTIFICATION_SERVICE + ctx.getPackageName())

        intent.putExtra("isGroupConversation", isGroupConversation)
        intent.putExtra("isGroup", sbn.isGroup())
        intent.putExtra("isOngoing", sbn.isOngoing())
        intent.putExtra("isClearable", sbn.isClearable())
        intent.putExtra("isAdded", isAdded)

        if (Build.VERSION.SDK_INT >= 30) {
            intent.putExtra("isAppGroup", sbn.isAppGroup())
        }
        if (Build.VERSION.SDK_INT >= 29) {
            intent.putExtra("uId", sbn.getUid())
        }

        intent.putExtra("tag", sbn.getTag())
        intent.putExtra("category", category)
        intent.putExtra("template", template)
        intent.putExtra("group_key", sbn.getGroupKey())
        intent.putExtra("key", sbn.getKey())
        intent.putExtra("id", sbn.getId().toString())
        intent.putExtra("package", packageName)
        intent.putExtra("ticker", ticker)
        intent.putExtra("appName", appName)
        intent.putExtra("title", title)
        intent.putExtra("postTime", notification.`when`)
        intent.putExtra("color", notification.color)

        intent.putExtra("text", text)
        intent.putExtra("bigText", bigText)
        intent.putExtra("subText", subText)
        intent.putExtra("titleBig", titleBig)
        intent.putExtra("summaryText", summaryText)
        intent.putExtra("info_text", infoText)
        intent.putExtra("substName", substName)

        intent.putExtra("progressMax", progressMax)
        intent.putExtra("progress", progress)
        intent.putExtra("progressIndeterminate", progressIndeterminate)
        intent.putExtra("showChronometer", showChronometer)

        if (largeIcon != null) {
            intent.putExtra("largeIcon", getByteArrayFromBitmap(largeIcon))
        }
        if (iconBitmap != null) {
            intent.putExtra("icon", getByteArrayFromBitmap(iconBitmap))
        }

        if (notification.extras.get(NotificationCompat.EXTRA_PICTURE) is Bitmap) {
            val picture =
                notification.extras.getParcelable<Bitmap?>(NotificationCompat.EXTRA_PICTURE)
            if (picture != null) {
                intent.putExtra("picture", getByteArrayFromBitmap2(picture))
            }
        }

        intent.putExtra("pendingIntent", notification.contentIntent)

        if (notification.actions != null && notification.actions.size > 0) {
            intent.putExtra("actions", getParsableActions(notification.actions))
        }

        LocalBroadcastManager.getInstance(ctx).sendBroadcast(intent)
    }

    /* ---------------- HELPERS ---------------- */
    private fun getStringExtra(b: Bundle, key: String?): String {
        return if (key != null && b.containsKey(key)) {
            b.getString(key).orEmpty()
        } else {
            ""
        }
    }


    private fun getCharSequenceExtra(b: Bundle, key: String?): String {
        return if (b.containsKey(key) && b.getCharSequence(key) != null)
            b.getCharSequence(key).toString()
        else
            ""
    }


    private fun getByteArrayFromBitmap2(bitmap: Bitmap?): ByteArray? {
        if (bitmap == null) {
            return null
        }
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, byteArrayOutputStream)
        return byteArrayOutputStream.toByteArray()
    }

    private fun getByteArrayFromBitmap(bitmap: Bitmap?): ByteArray? {
        if (bitmap == null) {
            return null
        }
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        return byteArrayOutputStream.toByteArray()
    }

    private fun scaleDownImage(
        bitmap: Bitmap,
        maxWidth: Int,
        maxHeight: Int
    ): Bitmap? {
        try {
            val srcWidth = bitmap.width.toFloat()
            val srcHeight = bitmap.height.toFloat()

            // No scaling needed
            if (srcWidth <= maxWidth) return bitmap

            val aspectRatio = srcWidth / srcHeight
            val targetRatio = maxWidth.toFloat() / maxHeight.toFloat()

            var targetWidth = maxWidth.toFloat()
            var targetHeight = maxHeight.toFloat()

            if (srcHeight <= srcWidth) {
                if (targetRatio < 1f) {
                    targetHeight = aspectRatio * targetWidth
                } else {
                    targetWidth = targetHeight / aspectRatio
                }
            } else {
                if (targetRatio > 1f) {
                    targetWidth = aspectRatio * targetHeight
                } else {
                    targetHeight = targetWidth / aspectRatio
                }
            }

            return Bitmap.createScaledBitmap(
                bitmap,
                targetWidth.toInt(),
                targetHeight.toInt(),
                true
            )

        } catch (e: OutOfMemoryError) {
            e.printStackTrace()
            return null
        }
    }

    private fun getCroppedBitmap(drawable: Drawable): Bitmap? {
        try {
            val defaultDisplay = (getSystemService("window") as WindowManager).getDefaultDisplay()
            val displayMetrics = DisplayMetrics()
            defaultDisplay.getRealMetrics(displayMetrics)
            val i = displayMetrics.widthPixels
            val i2 = displayMetrics.heightPixels
            val createBitmap = Bitmap.createBitmap(
                drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(),
                Bitmap.Config.ARGB_4444
            )
            val canvas = Canvas(createBitmap)
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight())
            drawable.draw(canvas)
            if (createBitmap.getWidth() <= i || createBitmap.getHeight() <= i2) {
                return scaleUpImage(createBitmap, i, i2)
            }
            return scaleDownImage(createBitmap, i, i2)
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        } catch (e: OutOfMemoryError) {
            e.printStackTrace()
            return null
        }
    }

    private fun scaleUpImage(bitmap: Bitmap, i: Int, i2: Int): Bitmap? {
        val bitmap2: Bitmap?
        try {
            val width = bitmap.getWidth().toFloat()
            if (bitmap.getWidth() > i) {
                val createBitmap = Bitmap.createBitmap(
                    bitmap,
                    ((width / 2.0f) - ((i / 2).toFloat())).toInt(),
                    0,
                    i,
                    bitmap.getHeight()
                )
                bitmap.recycle()
                return createBitmap
            }
            if (bitmap.getHeight() > i2) {
                Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), i2)
                bitmap.recycle()
            }
            if (bitmap != null) {
                return null
            }
            if (bitmap.getWidth() <= i) {
                bitmap2 = Bitmap.createScaledBitmap(
                    bitmap,
                    i,
                    (bitmap.getHeight() / bitmap.getWidth()) * i,
                    true
                )
            } else {
                bitmap2 = if (bitmap.getHeight() < i2) Bitmap.createScaledBitmap(
                    bitmap,
                    (bitmap.getHeight() / bitmap.getWidth()) * i2,
                    i2,
                    true
                ) else null
            }
            bitmap.recycle()
            return bitmap2
        } catch (e: OutOfMemoryError) {
            e.printStackTrace()
            return null
        }
    }

    fun drawableToBmp(context2: Context?, drawable: Drawable?, i: Int): Bitmap? {
        val bitmap: Bitmap?

        if (drawable == null) {
            return null
        }
        if (drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_4444)
        } else if (i <= 0) {
            return getCroppedBitmap(drawable)
        } else {
            val convertDpToPixel = MyHelper.convertDpToPixel(i.toFloat(), context2).toInt()
            bitmap =
                Bitmap.createBitmap(convertDpToPixel, convertDpToPixel, Bitmap.Config.ARGB_4444)
        }
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight())
        drawable.draw(canvas)
        return bitmap
    }

    companion object {
        var instance: MyNotificationService? = null

        fun getParsableActions(actionArr: Array<Notification.Action?>): ArrayList<ActionParsable?> {
            val arrayList = ArrayList<ActionParsable?>()
            for (action in actionArr) {
                if (action != null) {
                    val z = action.getExtras().getBoolean("android.support.allowGeneratedReplies")
                    val semanticAction =
                        if (Build.VERSION.SDK_INT >= 28) action.getSemanticAction() else 0
                    val i = action.icon
                    val pendingIntent = action.actionIntent
                    arrayList.add(
                        ActionParsable(
                            action.title,
                            action.actionIntent,
                            action.getExtras(),
                            action.getRemoteInputs(),
                            z,
                            semanticAction,
                            if (Build.VERSION.SDK_INT >= 29) action.isContextual() else false,
                            i
                        )
                    )
                }
            }
            return arrayList
        }
    }
}
