package com.example.service

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import com.example.model.FrequencyChannel
import com.example.model.PlaybackState
import com.example.model.RadioChannels
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RadioController(private val context: Context) {

    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private var service: RadioPlaybackService? = null
    private var isBound = false

    private val _playbackState = MutableStateFlow(PlaybackState.IDLE)
    val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()

    private val _currentChannel = MutableStateFlow<FrequencyChannel>(RadioChannels.CHANNELS.first())
    val currentChannel: StateFlow<FrequencyChannel> = _currentChannel.asStateFlow()

    private val _audioAmplitudes = MutableStateFlow(List(16) { 0.1f })
    val audioAmplitudes: StateFlow<List<Float>> = _audioAmplitudes.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            val localBinder = binder as? RadioPlaybackService.LocalBinder
            service = localBinder?.getService()
            isBound = true

            service?.let { svc ->
                scope.launch {
                    svc.playbackState.collect { _playbackState.value = it }
                }
                scope.launch {
                    svc.currentChannel.collect { _currentChannel.value = it }
                }
                scope.launch {
                    svc.audioAmplitudes.collect { _audioAmplitudes.value = it }
                }
                scope.launch {
                    svc.errorMessage.collect { _errorMessage.value = it }
                }
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            service = null
            isBound = false
        }
    }

    fun bind() {
        val intent = Intent(context, RadioPlaybackService::class.java)
        context.bindService(intent, connection, Context.BIND_AUTO_CREATE)
    }

    fun unbind() {
        if (isBound) {
            try {
                context.unbindService(connection)
            } catch (_: Exception) {}
            isBound = false
        }
    }

    fun play(channel: FrequencyChannel = _currentChannel.value) {
        _currentChannel.value = channel
        _playbackState.value = PlaybackState.BUFFERING
        _errorMessage.value = null
        RadioPlaybackService.startPlay(context, channel.id)
    }

    fun pause() {
        _playbackState.value = PlaybackState.PAUSED
        RadioPlaybackService.pause(context)
    }

    fun stop() {
        _playbackState.value = PlaybackState.IDLE
        RadioPlaybackService.stop(context)
    }

    fun turnOn(channel: FrequencyChannel = _currentChannel.value) {
        play(channel)
    }

    fun turnOff() {
        _playbackState.value = PlaybackState.IDLE
        RadioPlaybackService.stop(context)
    }

    fun togglePlayPause() {
        if (_playbackState.value == PlaybackState.PLAYING || _playbackState.value == PlaybackState.BUFFERING) {
            turnOff()
        } else {
            turnOn(_currentChannel.value)
        }
    }

    fun setVolume(volume: Float) {
        service?.setVolume(volume)
    }

    fun setPitchOffset(offset: Float) {
        service?.setPitchOffset(offset)
    }
}
