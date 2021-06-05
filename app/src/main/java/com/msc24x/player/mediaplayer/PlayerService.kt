package com.msc24x.player.mediaplayer

import Helpers.*
import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import com.msc24x.player.R


class PlayerService : Service() {
    private val mmr = MediaMetadataRetriever()
    private var isInterrupted = false
    private lateinit var audioRequest: AudioFocusRequest

    companion object {
        private lateinit var player: MediaPlayer
        private var playerInit = false
        private lateinit var trackUri: Uri
        private lateinit var trackTitle: String
        private lateinit var trackArtist: String
        private var trackLen: Int = -1

        fun getCurrentPlayerPos(): Int {
            if (playerInit) {
                return player.currentPosition
            } else {
                return 0
            }
        }

        fun getTrackTitle(): String = trackTitle
        fun getTrackArtist(): String = trackArtist
        fun isInitialized(): Boolean = playerInit
        fun getCurrentUri(): Uri = trackUri
        fun getSongLength(): Int = trackLen
        fun isPlaying() = player.isPlaying
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        safeInit()
        when (intent!!.action) {
            PLAY_SONG -> playSong(intent)
            PLAY -> play()
            PAUSE -> pause()
            SEEK_TO -> seekTo(intent)
            INIT -> safeInit()
        }

        setupNotification()
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        TODO("Not yet implemented")
    }

    private fun setupNotification() {
        val pendingIntent: PendingIntent =
            Intent(this, PlayerService::class.java).let { notificationIntent ->
                PendingIntent.getActivity(this, 0, notificationIntent, 0)
            }

        val channelId = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            createNotificationChannel("channelId", "channelName")
        } else {
            TODO("VERSION.SDK_INT < O")
        }

        val notification: Notification = Notification.Builder(this, channelId)
            .setContentTitle("BOP")
            .setContentText("Songs")
            .setSmallIcon(R.drawable.ic_play_indicator)
            .setContentIntent(pendingIntent)
            .setTicker("this is ticker")
            .build()

        startForeground(54165, notification)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(channelId: String, channelName: String): String {
        val chan = NotificationChannel(
            channelId,
            channelName, NotificationManager.IMPORTANCE_NONE
        )
        chan.lightColor = Color.BLUE
        chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        val service = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        service.createNotificationChannel(chan)
        return channelId
    }

    private fun playSong(intent: Intent) {
        pause()
        setUri(intent)
        play()
    }

    private fun play() {
        if (!player.isPlaying) {
            requestAudioFocus(true)
            player.start()
        }
    }

    private fun pause() {
        if (player.isPlaying) {
            player.pause()
            requestAudioFocus(false)
        }
    }

    private fun seekTo(intent: Intent) {
        val pos = intent.getIntExtra(SEEK_TO, -1)
        if (pos != -1)
            player.seekTo(pos)
    }

    private fun safeInit() {
        if (playerInit)
            return
        player = MediaPlayer()
        player.isLooping = true
        playerInit = true
    }

    private fun requestAudioFocus(reqFocus: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val mAudioManager = getSystemService(AUDIO_SERVICE) as AudioManager

            // Abandon the focus if not requesting focus with the exact same audioRequest
            if (!reqFocus)
                mAudioManager.abandonAudioFocusRequest(audioRequest)
            else {
                audioRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build()
                    )
                    .setAcceptsDelayedFocusGain(true)
                    .setOnAudioFocusChangeListener {
                        when (it) {
                            AudioManager.AUDIOFOCUS_GAIN -> {
                                if (isInterrupted) {
                                    isInterrupted = false
                                    play()
                                }
                            }
                            else -> {
                                if (isPlaying()) {
                                    isInterrupted = true
                                    pause()
                                }
                            }
                        }
                    }.build()
                mAudioManager.requestAudioFocus(audioRequest)
            }
        }
    }


    private fun setUri(intent: Intent) {
        player.reset()
        trackUri = Uri.parse(intent.getStringExtra(TRACK_URI))
        player = MediaPlayer.create(applicationContext, trackUri)
        trackLen = player.duration
        mmr.setDataSource(this, trackUri)
        trackTitle = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE).toString()
        trackArtist = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST).toString()
        player.isLooping = true
    }

}
