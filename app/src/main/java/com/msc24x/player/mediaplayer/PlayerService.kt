package com.msc24x.player.mediaplayer

import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.IBinder


class PlayerService : Service() {

    val CHANNEL_ID = "NowPlayingChannel"
    lateinit var player: MediaPlayer
    var safeThread = true

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        //Toast.makeText(applicationContext, "service started", Toast.LENGTH_SHORT).show()
        return START_STICKY
    }

    override fun onBind(intent: Intent): IBinder {
        TODO("Return the communication channel to the service.")
    }

    fun play(uri: Uri) {
        player.pause()
        player.reset()
        player = MediaPlayer.create(applicationContext, uri)
        player.start()
    }

}
