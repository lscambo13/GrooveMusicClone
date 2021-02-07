package com.msc24x.player

import android.Manifest
import android.annotation.SuppressLint
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val songsFragment = SongsFragment(this)
        val artistsFragment = ArtistsFragment()
        val albumsFragment = AlbumsFragment()

        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
            111
        )

        switchTab(songsFragment)

        btnPlay.setBackgroundResource(R.drawable.pausebtn)
        val player: MediaPlayer = MediaPlayer.create(this, R.raw.song)

        player.setVolume(0.5f, 0.5f)
        btnPlay.setBackgroundResource(R.drawable.playbtn)
        tvSongName.text = "Gotta Be A Reason"
        tvArtistName.text = "Alec Benjamin"
        tvTrackLength.text = progresstoString(player.duration)

        btnPlay.setOnClickListener {
            if (player.isPlaying) {
                player.pause()
                btnPlay.setBackgroundResource(R.drawable.playbtn)
            } else {
                btnPlay.setBackgroundResource(R.drawable.pausebtn)
                player.start()
            }
        }
        seekbar.max = player.duration
        seekbar.setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    if (fromUser) {
                        player.seekTo(progress)
                        tvTimeCode.text = progresstoString(progress)
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            }
        )

        @SuppressLint("HandlerLeak")
        var handler = object : Handler() {
            override fun handleMessage(msg: Message) {
                seekbar.progress = msg.what
                tvTimeCode.text = progresstoString(msg.what)
            }
        }


        Thread(
            Runnable {
                while (player != null) {
                    try {
                        var msg = Message()
                        msg.what = player.currentPosition
                        handler.sendMessage(msg)
                        Thread.sleep(100)
                    } catch (e: InterruptedException) {
                    }
                }
            }).start()

    }

    private fun switchTab(fragment: Fragment) = supportFragmentManager.beginTransaction().apply {
        replace(R.id.fragmentContainerMain, fragment)
        commit()
    }

    fun progresstoString(p: Int): String {
        var str = ""
        val min = p / 1000 / 60
        val sec = p / 1000 % 60
        if (min < 10) {
            str = "0" + min.toString() + ":"
        } else {
            str = min.toString() + ":"
        }
        if (sec < 10) {
            str = str + "0" + sec.toString()
        } else {
            str += sec.toString()
        }
        return str
    }

}


