package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.PlaybackParams
import android.net.wifi.WifiManager
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.model.FrequencyChannel
import com.example.model.PlaybackState
import com.example.model.RadioChannels
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.IOException

class RadioPlaybackService : Service(), MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
    MediaPlayer.OnBufferingUpdateListener, MediaPlayer.OnCompletionListener, AudioManager.OnAudioFocusChangeListener {

    private val binder = LocalBinder()
    private var mediaPlayer: MediaPlayer? = null
    private var wakeLock: PowerManager.WakeLock? = null
    private var wifiLock: WifiManager.WifiLock? = null
    private var audioManager: AudioManager? = null
    private var audioFocusRequest: AudioFocusRequest? = null

    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())
    private var visualizerJob: Job? = null
    private var connectionWatchdogJob: Job? = null
    private var retryJob: Job? = null

    private val _playbackState = MutableStateFlow(PlaybackState.IDLE)
    val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()

    private val _currentChannel = MutableStateFlow<FrequencyChannel>(RadioChannels.CHANNELS.first())
    val currentChannel: StateFlow<FrequencyChannel> = _currentChannel.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _audioAmplitudes = MutableStateFlow(List(16) { 0.1f })
    val audioAmplitudes: StateFlow<List<Float>> = _audioAmplitudes.asStateFlow()

    private var retryCount = 0
    private val maxRetries = 3

    inner class LocalBinder : Binder() {
        fun getService(): RadioPlaybackService = this@RadioPlaybackService
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onCreate() {
        super.onCreate()
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        createNotificationChannel()

        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "NidaaRadio::WakeLock")

        val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        wifiLock = wifiManager.createWifiLock(WifiManager.WIFI_MODE_FULL_HIGH_PERF, "NidaaRadio::WifiLock")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_PLAY -> {
                val channelId = intent.getStringExtra(EXTRA_CHANNEL_ID)
                val channel = RadioChannels.CHANNELS.find { it.id == channelId } ?: _currentChannel.value
                playChannel(channel)
            }
            ACTION_PAUSE -> pause()
            ACTION_STOP -> stopPlayback()
            ACTION_TOGGLE -> {
                if (_playbackState.value == PlaybackState.PLAYING || _playbackState.value == PlaybackState.BUFFERING) {
                    stopPlayback()
                } else {
                    playChannel(_currentChannel.value)
                }
            }
        }
        return START_NOT_STICKY
    }

    fun playChannel(channel: FrequencyChannel) {
        _currentChannel.value = channel
        _errorMessage.value = null
        retryCount = 0
        prepareAndPlay(channel.streamUrl)
    }

    private fun prepareAndPlay(url: String) {
        connectionWatchdogJob?.cancel()
        retryJob?.cancel()

        if (!requestAudioFocus()) {
            _errorMessage.value = "تعذر الحصول على التركيز الصوتي"
            _playbackState.value = PlaybackState.ERROR
            return
        }

        try {
            _playbackState.value = PlaybackState.BUFFERING
            startForegroundServiceWithNotification()

            try {
                mediaPlayer?.reset()
                mediaPlayer?.release()
            } catch (_: Exception) {}

            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                setDataSource(url)
                setOnPreparedListener(this@RadioPlaybackService)
                setOnErrorListener(this@RadioPlaybackService)
                setOnBufferingUpdateListener(this@RadioPlaybackService)
                setOnCompletionListener(this@RadioPlaybackService)
                prepareAsync()
            }

            // Connection watchdog (timeout after 12 seconds)
            connectionWatchdogJob = serviceScope.launch {
                delay(12000)
                if (isActive && _playbackState.value == PlaybackState.BUFFERING) {
                    if (retryCount < maxRetries) {
                        retryCount++
                        val fallback = _currentChannel.value.backupStreamUrl
                        prepareAndPlay(fallback)
                    } else {
                        handleStreamError("تعذر الاتصال بالبث المباشر. يرجى التحقق من اتصال الإنترنت.")
                    }
                }
            }

            try {
                if (wakeLock?.isHeld == false) wakeLock?.acquire(10 * 60 * 1000L)
                if (wifiLock?.isHeld == false) wifiLock?.acquire()
            } catch (_: Exception) {}

        } catch (e: Exception) {
            e.printStackTrace()
            handleStreamError(e.message ?: "خطأ في تهيئة البث")
        }
    }

    private var currentPitchOffset: Float = 0.05f

    override fun onPrepared(mp: MediaPlayer?) {
        connectionWatchdogJob?.cancel()
        try {
            mp?.start()
            _playbackState.value = PlaybackState.PLAYING
            _errorMessage.value = null
            applyPlaybackParams()
            startVisualizerSimulation()
            startForegroundServiceWithNotification()
        } catch (e: Exception) {
            handleStreamError(e.message ?: "تعذر بدء التشغيل")
        }
    }

    override fun onError(mp: MediaPlayer?, what: Int, extra: Int): Boolean {
        connectionWatchdogJob?.cancel()
        if (retryCount < maxRetries) {
            retryCount++
            retryJob = serviceScope.launch {
                delay(1500)
                val backupUrl = _currentChannel.value.backupStreamUrl
                prepareAndPlay(backupUrl)
            }
        } else {
            handleStreamError("انقطع الاتصال بخادم البث. يرجى المحاولة لاحقاً.")
        }
        return true
    }

    override fun onBufferingUpdate(mp: MediaPlayer?, percent: Int) {
        // Buffering update
    }

    override fun onCompletion(mp: MediaPlayer?) {
        if (_playbackState.value == PlaybackState.PLAYING) {
            prepareAndPlay(_currentChannel.value.streamUrl)
        }
    }

    fun pause() {
        connectionWatchdogJob?.cancel()
        retryJob?.cancel()
        try {
            if (mediaPlayer?.isPlaying == true) {
                mediaPlayer?.pause()
            }
            _playbackState.value = PlaybackState.PAUSED
            stopVisualizer()
            startForegroundServiceWithNotification()
            releaseLocks()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun stopPlayback() {
        connectionWatchdogJob?.cancel()
        retryJob?.cancel()
        try {
            if (mediaPlayer?.isPlaying == true) {
                mediaPlayer?.stop()
            }
            mediaPlayer?.release()
            mediaPlayer = null
            _playbackState.value = PlaybackState.IDLE
            stopVisualizer()
            abandonAudioFocus()
            releaseLocks()
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setVolume(volume: Float) {
        val safeVolume = volume.coerceIn(0f, 1f)
        mediaPlayer?.setVolume(safeVolume, safeVolume)
    }

    fun setPitchOffset(offset: Float) {
        currentPitchOffset = offset.coerceIn(-0.3f, 0.3f)
        applyPlaybackParams()
    }

    private fun applyPlaybackParams() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                val mp = mediaPlayer ?: return
                if (_playbackState.value == PlaybackState.PLAYING) {
                    val params = try {
                        mp.playbackParams
                    } catch (_: Exception) {
                        PlaybackParams()
                    }
                    params.pitch = (1.0f + currentPitchOffset).coerceIn(0.5f, 2.0f)
                    mp.playbackParams = params
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun handleStreamError(message: String) {
        _playbackState.value = PlaybackState.ERROR
        _errorMessage.value = message
        stopVisualizer()
        releaseLocks()
        startForegroundServiceWithNotification()
    }

    private fun startVisualizerSimulation() {
        stopVisualizer()
        visualizerJob = serviceScope.launch {
            while (isActive && _playbackState.value == PlaybackState.PLAYING) {
                val newAmps = List(16) {
                    val base = 0.2f + (kotlin.random.Random.nextFloat() * 0.75f)
                    base
                }
                _audioAmplitudes.value = newAmps
                delay(100)
            }
            _audioAmplitudes.value = List(16) { 0.1f }
        }
    }

    private fun stopVisualizer() {
        visualizerJob?.cancel()
        visualizerJob = null
        _audioAmplitudes.value = List(16) { 0.1f }
    }

    private fun requestAudioFocus(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build()
                )
                .setOnAudioFocusChangeListener(this)
                .build()
            audioManager?.requestAudioFocus(audioFocusRequest!!) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        } else {
            @Suppress("DEPRECATION")
            audioManager?.requestAudioFocus(
                this,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN
            ) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        }
    }

    private fun abandonAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioFocusRequest?.let { audioManager?.abandonAudioFocusRequest(it) }
        } else {
            @Suppress("DEPRECATION")
            audioManager?.abandonAudioFocus(this)
        }
    }

    override fun onAudioFocusChange(focusChange: Int) {
        when (focusChange) {
            AudioManager.AUDIOFOCUS_LOSS -> {
                pause()
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                pause()
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                mediaPlayer?.setVolume(0.2f, 0.2f)
            }
            AudioManager.AUDIOFOCUS_GAIN -> {
                mediaPlayer?.setVolume(1.0f, 1.0f)
                if (_playbackState.value == PlaybackState.PAUSED) {
                    mediaPlayer?.start()
                    _playbackState.value = PlaybackState.PLAYING
                    startVisualizerSimulation()
                }
            }
        }
    }

    private fun releaseLocks() {
        if (wakeLock?.isHeld == true) wakeLock?.release()
        if (wifiLock?.isHeld == true) wifiLock?.release()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "بث إذاعة نداء المعرفة",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "التحكم في البث المباشر لإذاعة نداء المعرفة"
                setShowBadge(false)
            }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager?.createNotificationChannel(channel)
        }
    }

    private fun startForegroundServiceWithNotification() {
        try {
            val notification = buildNotification()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK)
            } else {
                startForeground(NOTIFICATION_ID, notification)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun buildNotification(): Notification {
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val toggleIntent = Intent(this, RadioPlaybackService::class.java).apply {
            action = ACTION_TOGGLE
        }
        val togglePendingIntent = PendingIntent.getService(
            this,
            1,
            toggleIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, RadioPlaybackService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            2,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val isPlaying = _playbackState.value == PlaybackState.PLAYING
        val isBuffering = _playbackState.value == PlaybackState.BUFFERING

        val statusText = when {
            isBuffering -> "جارٍ الاتصال بالبث المباشر..."
            isPlaying -> "البث المباشر يعمل الآن • ${_currentChannel.value.frequencyMhz}"
            _playbackState.value == PlaybackState.PAUSED -> "البث متوقف مؤقتاً"
            _playbackState.value == PlaybackState.ERROR -> "تعذر الاتصال بالبث"
            else -> "جاهز للاستماع"
        }

        val playPauseActionTitle = if (isPlaying) "إيقاف مؤقت" else "تشغيل البث"
        val playPauseIcon = if (isPlaying) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("إذاعة نداء المعرفة (${_currentChannel.value.frequencyMhz})")
            .setContentText(statusText)
            .setSubText(_currentChannel.value.coverageRegion)
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setContentIntent(openAppPendingIntent)
            .setOngoing(isPlaying || isBuffering)
            .addAction(playPauseIcon, playPauseActionTitle, togglePendingIntent)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "إغلاق", stopPendingIntent)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("$statusText\n${_currentChannel.value.coverageRegion}")
            )
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()
    }

    override fun onDestroy() {
        stopPlayback()
        super.onDestroy()
    }

    companion object {
        const val CHANNEL_ID = "nidaa_radio_playback_channel"
        const val NOTIFICATION_ID = 9110

        const val ACTION_PLAY = "com.example.service.ACTION_PLAY"
        const val ACTION_PAUSE = "com.example.service.ACTION_PAUSE"
        const val ACTION_STOP = "com.example.service.ACTION_STOP"
        const val ACTION_TOGGLE = "com.example.service.ACTION_TOGGLE"
        const val EXTRA_CHANNEL_ID = "extra_channel_id"

        fun startPlay(context: Context, channelId: String) {
            val intent = Intent(context, RadioPlaybackService::class.java).apply {
                action = ACTION_PLAY
                putExtra(EXTRA_CHANNEL_ID, channelId)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun pause(context: Context) {
            val intent = Intent(context, RadioPlaybackService::class.java).apply {
                action = ACTION_PAUSE
            }
            context.startService(intent)
        }

        fun stop(context: Context) {
            val intent = Intent(context, RadioPlaybackService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }
}
