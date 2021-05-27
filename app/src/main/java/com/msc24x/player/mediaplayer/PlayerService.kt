package com.msc24x.player.mediaplayer

import Helpers.*
import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.IBinder
import android.util.Log


class PlayerService : Service() {
    companion object {
        private lateinit var player: MediaPlayer
        private var playerInit = false

        fun getCurrentPlayerPos(): Int {
            if (playerInit) {
                return player.currentPosition
            } else {
                return 0
            }
        }
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

        return START_STICKY
    }


    override fun onBind(intent: Intent): IBinder {
        TODO("Return the communication channel to the service.")
    }

    private fun playSong(intent: Intent) {
        setUri(intent)
        play()
    }

    private fun play() {
        Log.v("play", player.isPlaying.toString())
        if (!player.isPlaying) {
            player.start()
        }
        broadcastBusy(true)
    }

    private fun pause() {
        if (player.isPlaying)
            player.pause()
        broadcastBusy(false)
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
        broadcastBusy(false)
        playerInit = true
    }

    private fun setUri(intent: Intent) {
        player.reset()
        val uri = Uri.parse(intent.getStringExtra(TRACK_URI))
        player = MediaPlayer.create(applicationContext, uri!!)
        player.isLooping = true
        //player.prepare()
    }


    private fun broadcastBusy(busy: Boolean) {
        //CommonViewModel().busy.value = busy
    }

    private fun broadcastPos() {

    }

}
