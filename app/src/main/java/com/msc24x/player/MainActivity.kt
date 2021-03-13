package com.msc24x.player

import android.Manifest
import android.annotation.SuppressLint
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import eightbitlab.com.blurview.RenderScriptBlur
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    lateinit var player: MediaPlayer
    lateinit var songUri: Uri
    lateinit var busy: String
    var safeThread = true

    //lateinit var viewModel: CommonViewModel
    private val viewModel: CommonViewModel by viewModels()

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


        //viewModel = ViewModelProvider(this).get(CommonViewModel::class.java)

        when (this::player.isInitialized) {
            true -> {

            }
            false -> {
                player = MediaPlayer.create(this, R.raw.song)
                iconPlay.visibility = View.VISIBLE
                iconPause.visibility = View.INVISIBLE
                tvTrackLength.text = viewModel.progressToString(player.duration)
                busy = "false"
            }
        }
        player.isLooping = true


        viewModel.currentSong.observe(this, Observer {
            println("change detected song- main")
            tvSongName.text = viewModel.currentSong.value
        })
        viewModel.currentArtist.observe(this, Observer {
            println("change detected artist- main")
            tvArtistName.text = viewModel.currentArtist.value
        })
        viewModel.currentUri.observe(this, Observer {
            println("change detected uri- main")
            songUri = viewModel.currentUri.value!!
            when (busy) {
                "true" -> {
                    println("player is playing")
                    player.pause()
                    player.reset()
                    player = MediaPlayer.create(this, songUri)
                    tvTrackLength.text = viewModel.progressToString(player.duration)
                    player.start()
                    iconPlay.visibility = View.INVISIBLE
                    iconPause.visibility = View.VISIBLE
                    busy = "true"
                }
                "false" -> {
                    player = MediaPlayer.create(this, songUri)
                    println("player wasn't playing")
                    tvTrackLength.text = viewModel.progressToString(player.duration)
                    player.start()
                    iconPlay.visibility = View.INVISIBLE
                    iconPause.visibility = View.VISIBLE
                    busy = "true"
                }
                else -> println("what happened? busy = $busy")
            }
        })

        //updateFirst()
        //enableBlur()


        //player.setVolume(0.5f, 0.5f)

        //var isPlaying = true
        //var progressPos = Preferences(this).getCurrentProgress()

        //player.seekTo(progressPos)
        //seekbar.progress = progressPos
        //if (isPlaying) {
        //player.start()
        //    btnPlay.setBackgroundResource(R.drawable.pausebtn)
        //}

        btnOutline.setOnClickListener {
            if (player.isPlaying) {
                player.pause()
                println("click is playing")
                iconPlay.visibility = View.VISIBLE
                iconPause.visibility = View.INVISIBLE
                busy = "false"
            } else {
                iconPlay.visibility = View.INVISIBLE
                iconPause.visibility = View.VISIBLE
                player.start()
                println("click wasn't playing")
                busy = "true"
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


