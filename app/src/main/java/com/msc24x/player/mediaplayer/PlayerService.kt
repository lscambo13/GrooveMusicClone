package com.msc24x.player.mediaplayer

import Helpers.*
import android.app.*
import android.app.Notification.MediaStyle
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.Icon
import android.media.*
import android.media.session.MediaSession
import android.media.session.PlaybackState
import android.net.Uri
import android.os.Build
import android.os.IBinder
import androidx.annotation.RequiresApi
import androidx.palette.graphics.Palette
import com.msc24x.player.MainActivity
import com.msc24x.player.R


class PlayerService : Service() {

    private lateinit var endSessionIntent: PendingIntent
    private lateinit var playIntent: PendingIntent
    private lateinit var pauseIntent: PendingIntent

    companion object {
        private lateinit var player: MediaPlayer
        private var playerInit = false
        private lateinit var trackUri: Uri
        private lateinit var trackTitle: String
        private lateinit var trackArtist: String
        private var trackLen: Int = -1
        private lateinit var trackBitmap: Bitmap
        private var trackColor: Int = 909088
        private lateinit var playbackState: PlaybackState

        private val mmr = MediaMetadataRetriever()
        private var isInterrupted = false
        private lateinit var audioRequest: AudioFocusRequest
        private lateinit var mediaSession: MediaSession
        private lateinit var mediaStyle: MediaStyle

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

    override fun onCreate() {
        super.onCreate()
        mediaSession = MediaSession(this, "MediaSession")
        mediaStyle = MediaStyle().setMediaSession(mediaSession.sessionToken)
        endSessionIntent = PendingIntent.getService(
            this, 1, Intent(
                this,
                PlayerService::class.java
            ).setAction(END_SESSION), 0
        )

        playIntent = PendingIntent.getService(
            this, 1, Intent(
                this,
                PlayerService::class.java
            ).setAction(PLAY), 0
        )

        pauseIntent = PendingIntent.getService(
            this, 1, Intent(
                this,
                PlayerService::class.java
            ).setAction(PAUSE), 0
        )
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        safeInit()
        when (intent!!.action) {
            PLAY_SONG -> playSong(intent)
            PLAY -> play()
            PAUSE -> pause()
            SEEK_TO -> seekTo(intent)
            END_SESSION -> stopSelf()
            INIT -> safeInit()
        }

        setupNotification()
        return START_STICKY
    }

    override fun onDestroy() {
        playerInit = false
        mediaSession.release()
        player.release()
        super.onDestroy()
    }


    override fun onBind(intent: Intent?): IBinder? {
        TODO("Not yet implemented")
    }

    private fun setupNotification() {
        val pendingIntent: PendingIntent =
            Intent(this, MainActivity::class.java).let { notificationIntent ->
                PendingIntent.getActivity(this, 0, notificationIntent, 0)
            }

        if (Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            createNotificationChannel("channelId", "channelName")
        } else {
            TODO("VERSION.SDK_INT < O")
        }


        val actionEndSession = Notification.Action.Builder(
            Icon.createWithResource(
                this,
                R.drawable.ic_cross
            ), "end session", endSessionIntent
        ).build()

        val actionPause = Notification.Action.Builder(
            Icon.createWithResource(
                this,
                R.drawable.ic_pausebtn
            ), "pause", pauseIntent
        ).build()

        val actionPlay = Notification.Action.Builder(
            Icon.createWithResource(
                this,
                R.drawable.ic_playbtn
            ), "play", playIntent
        ).build()

        val notification: Notification = Notification.Builder(this, "channelId")
            .setContentTitle(trackTitle)
            .setContentText(trackArtist)
            .setStyle(mediaStyle.setShowActionsInCompactView(0, 1, 2, 3))
            .setSmallIcon(R.drawable.ic_music)
            .setContentIntent(pendingIntent)
            .setAutoCancel(false)
            .setActions(actionEndSession, actionPlay, actionPause)
            .build()

        mediaSession.setMetadata(
            MediaMetadata.Builder()
                .putString(MediaMetadata.METADATA_KEY_TITLE, trackTitle)
                .putString(MediaMetadata.METADATA_KEY_ARTIST, trackArtist)
                .putBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART, trackBitmap)
                .putLong(MediaMetadata.METADATA_KEY_DURATION, trackLen.toLong())
                .build()
        )
/*
        playbackState = PlaybackState.Builder()
            .setState(PlaybackState.STATE_PLAYING, player.currentPosition.toLong(), 1.0F)
            .setActions(PlaybackState.ACTION_SEEK_TO or PlaybackState.ACTION_PLAY or PlaybackState.ACTION_PAUSE)
            .build()

        mediaSession.setPlaybackState(playbackState)*/

        mediaSession.setCallback(object : MediaSession.Callback() {
            override fun onSeekTo(pos: Long) {
                super.onSeekTo(pos)
                seekTo(pos.toInt())
            }
        })


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
            setSessionPlaying(true)
            player.start()
        }
    }

    private fun pause() {
        if (player.isPlaying) {
            player.pause()
            setSessionPlaying(false)
            requestAudioFocus(false)
        }
    }

    private fun seekTo(intent: Intent) {
        val pos = intent.getIntExtra(SEEK_TO, -1)
        if (pos != -1) {
            player.seekTo(pos)
            if (player.isPlaying)
                setSessionPlaying(true)
            else
                setSessionPlaying(false)
        }
    }

    private fun seekTo(pos: Int) {
        if (pos != -1) {
            player.seekTo(pos)
            if (player.isPlaying)
                setSessionPlaying(true)
            else
                setSessionPlaying(false)
        }
    }

    private fun safeInit() {
        if (playerInit)
            return
        player = MediaPlayer()
        player.isLooping = true
        playerInit = true
    }

    private fun setSessionPlaying(isPlaying: Boolean) {
        if (isPlaying) {
            playbackState = PlaybackState.Builder()
                .setState(PlaybackState.STATE_PLAYING, player.currentPosition.toLong(), 1.0F)
                .setActions(PlaybackState.ACTION_SEEK_TO or PlaybackState.ACTION_PLAY or PlaybackState.ACTION_PAUSE)
                .build()
            mediaSession.isActive = true
        } else {
            playbackState = PlaybackState.Builder()
                .setState(PlaybackState.STATE_PAUSED, player.currentPosition.toLong(), 0F)
                .setActions(PlaybackState.ACTION_SEEK_TO or PlaybackState.ACTION_PLAY or PlaybackState.ACTION_PAUSE)
                .build()
            mediaSession.isActive = false
        }
        mediaSession.setPlaybackState(playbackState)
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
        val newTrackUri = Uri.parse(intent.getStringExtra(TRACK_URI))
        try {
            if (newTrackUri == trackUri)
                return
        } catch (e: UninitializedPropertyAccessException) {
        }

        player.reset()
        trackUri = newTrackUri
        player = MediaPlayer.create(applicationContext, trackUri)
        player.isLooping = true

        // MetaData
        mmr.setDataSource(this, trackUri)
        trackLen = player.duration
        trackTitle = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE).toString()
        trackArtist = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST).toString()
        if (trackArtist == "null") trackArtist = "Unknown"
        trackBitmap = extractTrackBitmap(trackUri)
        trackColor = extractMutedColor(trackBitmap)

    }

    private fun extractTrackBitmap(uri: Uri): Bitmap {
        val mmr = MediaMetadataRetriever()
        val art: Bitmap
        val bfo = BitmapFactory.Options()
        mmr.setDataSource(applicationContext, uri)
        val rawArt: ByteArray? = mmr.embeddedPicture

        art = if (rawArt != null) {
            BitmapFactory.decodeByteArray(rawArt, 0, rawArt.size, bfo)
        } else {
            BitmapFactory.decodeResource(
                applicationContext.resources,
                R.drawable.missing_album_art
            )
        }
        return art
    }

    private fun extractMutedColor(art: Bitmap): Int {
        val myPalette = Palette.from(art).generate()
        var muted = myPalette.mutedSwatch
        if (muted == null) muted = myPalette.darkVibrantSwatch
        return muted!!.rgb
    }


    /*private fun extractStrings(uri: Uri) : List<String>{
        val mmr = MediaMetadataRetriever()
        mmr.setDataSource(applicationContext, uri)
        //mmr.extractMetadata(MediaMetadata.)
    }*/
}
