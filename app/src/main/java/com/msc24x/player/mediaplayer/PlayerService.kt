package com.msc24x.player.mediaplayer

import Helpers.*
import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.*
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.palette.graphics.Palette
import com.msc24x.player.MainActivity
import com.msc24x.player.R


class PlayerService : Service() {

    inner class ServiceBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent != null) {
                when (intent.action) {
                    PLAY -> play()
                    PAUSE -> pause()
                }
            }
        }
    }

    private val receiver = ServiceBroadcastReceiver()
    private val notificationIntentFilter = IntentFilter()

    companion object {
        private lateinit var player: MediaPlayer
        private var playerInit = false

        private object Playlist {
            var isSet = false

            lateinit var name: String
            lateinit var currentTrack: Track
            var currentTrackIndex: Int = -1
            lateinit var trackPlaylist: MutableList<Track>
            var size = 0

        }

        private lateinit var trackUri: Uri
        private lateinit var trackTitle: String
        private lateinit var trackArtist: String
        private var trackLen: Int = -1
        private lateinit var trackBitmap: Bitmap
        private var trackColor: Int = 909088

        private val mmr = MediaMetadataRetriever()
        private var isInterrupted = false
        private lateinit var audioRequest: AudioFocusRequest

        private lateinit var mediaSession: MediaSessionCompat
        private lateinit var mediaStyle: androidx.media.app.NotificationCompat.MediaStyle

        private lateinit var endSessionIntent: PendingIntent
        private lateinit var playIntent: PendingIntent
        private lateinit var pauseIntent: PendingIntent


        fun setTrackPlaylist(playlist: MutableList<Track>, name: String) {
            if (Playlist.isSet)
                if (Playlist.name == name)
                    return

            Playlist.trackPlaylist = playlist
            Playlist.name = name
            Playlist.isSet = true
            Playlist.size = playlist.size
        }

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

    override fun onBind(intent: Intent?): IBinder? {
        TODO("Not yet implemented")
    }

    override fun onCreate() {
        super.onCreate()

        if (Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            createNotificationChannel(
                "com.msc24x.player.notificationChannel",
                "com.msc24x.player.notificationChannelName"
            )
        } else {
            TODO("VERSION.SDK_INT < O")
        }

        mediaSession = MediaSessionCompat(this, "com.msc24x.player.mediaSession")
        mediaStyle = androidx.media.app.NotificationCompat.MediaStyle()
            .setMediaSession(mediaSession.sessionToken)

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

        notificationIntentFilter.addAction(PLAY)
        notificationIntentFilter.addAction(PAUSE)

        registerReceiver(receiver, notificationIntentFilter)
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

        return START_STICKY
    }

    override fun onDestroy() {
        playerInit = false
        mediaSession.release()
        player.release()
        unregisterReceiver(receiver)
        super.onDestroy()
    }


    private fun setupNotification(showPLayButton: Boolean) {
        val pendingIntent: PendingIntent =
            Intent(this, MainActivity::class.java).let { notificationIntent ->
                PendingIntent.getActivity(this, 0, notificationIntent, 0)
            }


        val actionEndSession = NotificationCompat.Action.Builder(
            R.drawable.ic_cross, "end session", endSessionIntent
        ).build()

        val actionPlayPause: NotificationCompat.Action = if (showPLayButton)
            NotificationCompat.Action.Builder(
                R.drawable.ic_playbtn,
                "play",
                PendingIntent.getBroadcast(applicationContext, 0, Intent().setAction(PLAY), 0)
            ).build()
        else
            NotificationCompat.Action.Builder(
                R.drawable.ic_pausebtn,
                "pause",
                PendingIntent.getBroadcast(applicationContext, 0, Intent().setAction(PAUSE), 0)
            ).build()

        val notification: Notification =
            NotificationCompat.Builder(this, "com.msc24x.player.notificationChannel")
                .setContentTitle(trackTitle)
                .setContentText(trackArtist)
                .setStyle(mediaStyle.setShowActionsInCompactView(0))
                .setSmallIcon(R.drawable.ic_music)
                .setLargeIcon(trackBitmap)
                .setContentIntent(pendingIntent)
                .setAutoCancel(false)
                .addAction(actionPlayPause)
                .addAction(actionEndSession)
                .build()

        mediaSession.setMetadata(
            MediaMetadataCompat.Builder()
                .putString(MediaMetadata.METADATA_KEY_TITLE, trackTitle)
                .putString(MediaMetadata.METADATA_KEY_ARTIST, trackArtist)
                .putBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART, trackBitmap)
                .putLong(MediaMetadata.METADATA_KEY_DURATION, trackLen.toLong())
                .build()
        )

        mediaSession.setCallback(object : MediaSessionCompat.Callback() {
            override fun onMediaButtonEvent(mediaButtonIntent: Intent): Boolean {
                Toast.makeText(applicationContext, mediaButtonIntent.action, Toast.LENGTH_SHORT)
                    .show()
                return super.onMediaButtonEvent(mediaButtonIntent)
            }

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
        chan.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
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
            setupNotification(false)
        }
    }

    private fun pause() {
        if (player.isPlaying) {
            player.pause()
            setSessionPlaying(false)
            requestAudioFocus(false)
            setupNotification(true)
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
            playbackState = PlaybackStateCompat.Builder()
                .setState(PlaybackStateCompat.STATE_PLAYING, player.currentPosition.toLong(), 1.0F)
                .setActions(PlaybackStateCompat.ACTION_SEEK_TO or PlaybackStateCompat.ACTION_PLAY_PAUSE)
                .build()
            mediaSession.isActive = true
        } else {
            playbackState = PlaybackStateCompat.Builder()
                .setState(PlaybackStateCompat.STATE_PAUSED, player.currentPosition.toLong(), 0F)
                .setActions(PlaybackStateCompat.ACTION_SEEK_TO or PlaybackStateCompat.ACTION_PLAY_PAUSE)
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
