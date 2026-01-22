package com.app.gemvox.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import androidx.annotation.OptIn
import androidx.core.app.NotificationCompat
import androidx.media3.common.ForwardingPlayer
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.session.MediaStyleNotificationHelper
import com.app.gemvox.MainActivity
import com.app.gemvox.R
import com.app.gemvox.data.audio.AudioRecorder
import com.app.gemvox.domain.repository.VoiceRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject
@AndroidEntryPoint
class GemVoxService : MediaSessionService() {
    @Inject lateinit var repository: VoiceRepository
    @Inject lateinit var audioRecorder: AudioRecorder
    @Inject lateinit var playerManager: PlayerManager
    private var mediaSession: MediaSession? = null
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var recordingJob: Job? = null
    private var playbackJob: Job? = null

    private var isGeminiSpeaking = false

    companion object {
        const val CHANNEL_ID = "GemVoxChannel"
        const val NOTIFICATION_ID = 101
        const val ACTION_START = "ACTION_START"
        const val ACTION_STOP = "ACTION_STOP"
    }

    @OptIn(UnstableApi::class)
    override fun onCreate() {
        super.onCreate()
        playerManager.initializePlayer()
        val realPlayer = playerManager.getPlayer()
        val forwardingPlayer = GemVoxPlayer(realPlayer)
        mediaSession = MediaSession.Builder(this, forwardingPlayer)
            .setCallback(CustomMediaSessionCallback())
            .build()
        createNotificationChannel()
        startObservingIncomingAudio()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        when (intent?.action) {
            ACTION_START -> {
                startForegroundService()
                serviceScope.launch(Dispatchers.IO) {
                    repository.connect()
                }
            }
            ACTION_STOP -> {
                playerManager.release()
                stopSelf()
            }
        }
        return START_STICKY
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    override fun onDestroy() {
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        serviceScope.cancel()
        CoroutineScope(Dispatchers.IO).launch {
            repository.disconnect()
        }
        super.onDestroy()
    }
    private fun startForegroundService() {
        val notification = buildNotification("Tap Play to Speak")
        startForeground(NOTIFICATION_ID, notification)
    }
    private fun startObservingIncomingAudio() {
        playbackJob?.cancel()
        playbackJob = serviceScope.launch(Dispatchers.IO) {
            repository.getIncomingAudioStream().collectLatest { pcmData ->
                if (!isGeminiSpeaking) {
                    isGeminiSpeaking = true
                    launch(Dispatchers.Main) {
                        updateNotification("Gemini Speaking...")
                    }
                }
                launch(Dispatchers.Main) {
                    playerManager.playPcmChunk(pcmData)
                }
            }
        }
    }
    private fun startMicCapture() {
        if (recordingJob?.isActive == true) return
        isGeminiSpeaking = false
        playerManager.getPlayer().pause()
        playerManager.getPlayer().clearMediaItems()
        updateNotification("Listening...")
        recordingJob = serviceScope.launch(Dispatchers.IO) {
            audioRecorder.startRecording().collect { buffer ->
                repository.sendAudio(buffer)
            }
        }
    }
    private fun stopMicCapture() {
        recordingJob?.cancel()
        recordingJob = null
        updateNotification("Thinking...")
    }

    @SuppressLint("UnsafeOptInUsageError")
    private inner class GemVoxPlayer(player: Player) : ForwardingPlayer(player) {
        override fun play() {
            startMicCapture()
        }
        override fun pause() {
            stopMicCapture()
            super.pause()
        }
    }

    private inner class CustomMediaSessionCallback : MediaSession.Callback {
        override fun onConnect(
            session: MediaSession,
            controller: MediaSession.ControllerInfo
        ): MediaSession.ConnectionResult {
            val connectionResult = super.onConnect(session, controller)
            val availableSessionCommands = connectionResult.availableSessionCommands.buildUpon()
            return MediaSession.ConnectionResult.accept(
                availableSessionCommands.build(),
                connectionResult.availablePlayerCommands
            )
        }
    }

    @OptIn(UnstableApi::class)
    private fun buildNotification(text: String): Notification {
        val stopIntent = Intent(this, GemVoxService::class.java).apply { action = ACTION_STOP }
        val pendingStop = PendingIntent.getService(this, 0, stopIntent, PendingIntent.FLAG_IMMUTABLE)
        val openIntent = Intent(this, MainActivity::class.java)
        val pendingOpen = PendingIntent.getActivity(this, 0, openIntent, PendingIntent.FLAG_IMMUTABLE)
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("GemVox AI")
            .setContentText(text)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingOpen)
            .setOngoing(true)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Kill App", pendingStop)
        mediaSession?.let {
            builder.setStyle(MediaStyleNotificationHelper.MediaStyle(it))
        }
        return builder.build()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "GemVox Service",
            NotificationManager.IMPORTANCE_LOW
        )
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    private fun updateNotification(text: String) {
        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(NOTIFICATION_ID, buildNotification(text))
    }
}