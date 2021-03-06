package com.msc24x.player

import android.Manifest
import android.annotation.SuppressLint
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import eightbitlab.com.blurview.RenderScriptBlur
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    lateinit var player: MediaPlayer
    var safeThread = true
    lateinit var viewModel: CommonViewModel
/*    lateinit var navController: NavController
    lateinit var appBarConfiguration: AppBarConfiguration
    lateinit var drawerLayout: DrawerLayout*/

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
            111
        )

        materialToolbar.setNavigationOnClickListener {
            drawerLayout.open()
        }
        navigationView.setNavigationItemSelectedListener {
            it.isChecked = true
            drawerLayout.close()
            true
        }

        viewModel = ViewModelProvider(this).get(CommonViewModel::class.java)

        viewModel.currentSong.observe(this, Observer {
            println("change detected song- main")
            tvSongName.text = viewModel.currentSong.value
        })
        viewModel.currentArtist.observe(this, Observer {
            println("change detected artist- main")
            tvArtistName.text = viewModel.currentArtist.value
        })

        //updateFirst()
        //enableBlur()
        var songUri = R.raw.song


        player = MediaPlayer.create(this, songUri)
        player.isLooping = true
        tvTrackLength.text = viewModel.progressToString(player.duration)
        player.setVolume(0.5f, 0.5f)
        btnPlay.setBackgroundResource(R.drawable.playbtn)

        //var isPlaying = true
        //var progressPos = Preferences(this).getCurrentProgress()

        //player.seekTo(progressPos)
        //seekbar.progress = progressPos
        //if (isPlaying) {
        //player.start()
        //    btnPlay.setBackgroundResource(R.drawable.pausebtn)
        //}

        btnPlay.setOnClickListener {
            if (player.isPlaying) {
                player.pause()
                btnPlay.setBackgroundResource(R.drawable.playbtn)
                savedInstanceState?.putBoolean("isPlaying", false)
            } else {
                btnPlay.setBackgroundResource(R.drawable.pausebtn)
                player.start()
                savedInstanceState?.putBoolean("isPlaying", true)
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
                        tvTimeCode.text = viewModel.progressToString(progress)
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
                tvTimeCode.text = viewModel.progressToString(msg.what)
                savedInstanceState?.putInt("progressPos", msg.what)
            }
        }

        Thread {
            while (safeThread) {
                var msg = Message()
                msg.what = player.currentPosition
                var curPos = msg.what
                //Preferences(this).setCurrentProgress(msg.what)
                handler.sendMessage(msg)
                Thread.sleep(100)
            }
        }.start()
    }

    // TODO -- FIX BROKEN MENU

    fun switchTab(fragment: Fragment) = supportFragmentManager.beginTransaction().apply {
        //replace(R.id.fragmentContainerMain, fragment)
        //commit()
    }


    private fun enableBlur() {
        val radius = 15f
        val decorView = window.decorView
        //ViewGroup you want to start blur from. Choose root as close to BlurView in hierarchy as possible.
        val rootView = decorView.findViewById(android.R.id.content) as ViewGroup
        //Set drawable to draw in the beginning of each blurred frame (Optional).
        //Can be used in case your layout has a lot of transparent space and your content
        //gets kinda lost after after blur is applied.
        //Set drawable to draw in the beginning of each blurred frame (Optional).
        //Can be used in case your layout has a lot of transparent space and your content
        //gets kinda lost after after blur is applied.
        val windowBackground = decorView.background
        blurMiniPlayer.setupWith(rootView)
            .setFrameClearDrawable(windowBackground)
            .setBlurAlgorithm(RenderScriptBlur(this))
            .setBlurRadius(radius)
            .setBlurAutoUpdate(true)
            .setHasFixedTransformationMatrix(true)
        blurAppBar.setupWith(rootView)
            .setFrameClearDrawable(windowBackground)
            .setBlurAlgorithm(RenderScriptBlur(this))
            .setBlurRadius(radius)
            .setBlurAutoUpdate(true)
            .setHasFixedTransformationMatrix(true)

    }

    override fun onDestroy() {
        super.onDestroy()
        safeThread = false
        player.release()
    }
}


