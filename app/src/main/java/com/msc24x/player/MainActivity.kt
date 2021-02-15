package com.msc24x.player

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.res.Configuration
import android.graphics.Color
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.view.*
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    lateinit var player: MediaPlayer
    var safeThread = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // This makes the actionbar flat
        // supportActionBar?.elevation = 0F

        //makeStatusBarTransparent()
        //ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.fragmentContainerMain)) { _, insets ->
        //    findViewById<TextView>(R.id.materialToolbar).setMarginTop(insets.systemWindowInsetTop)
        //    insets.consumeSystemWindowInsets()
        //}


        val songsFragment = SongsFragment()
        val artistsFragment = ArtistsFragment()
        val albumsFragment = AlbumsFragment()
        //switchTab(songsFragment)

        viewPager.adapter = ViewPagerAdapter(songsFragment)

        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
            111
        )

        var songUri = R.raw.song

        player = MediaPlayer.create(this, songUri)
        player.isLooping = true

        tvSongName.text = "Gotta Be A Reason"
        tvArtistName.text = "Alec Benjamin"
        tvTrackLength.text = progressToString(player.duration)
        player.setVolume(0.5f, 0.5f)
        btnPlay.setBackgroundResource(R.drawable.playbtn)

        var isPlaying = true
        var progressPos = Preferences(this).getCurrentProgress()

        player.seekTo(progressPos)
        seekbar.progress = progressPos
        if (isPlaying) {
            //player.start()
            btnPlay.setBackgroundResource(R.drawable.pausebtn)
        }

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
                        tvTimeCode.text = progressToString(progress)
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
                tvTimeCode.text = progressToString(msg.what)
                savedInstanceState?.putInt("progressPos", msg.what)
            }
        }

        Thread {
            while (safeThread) {
                var msg = Message()
                msg.what = player.currentPosition
                Preferences(this).setCurrentProgress(msg.what)
                handler.sendMessage(msg)
                Thread.sleep(100)
            }
        }.start()
    }

    // TODO -- FIX BROKEN MENU

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menuSearch -> {
                println("Searched.")
                Toast.makeText(applicationContext, "Searched.", Toast.LENGTH_SHORT).show()
            }
        }
        return true
    }


    fun switchTab(fragment: Fragment) = supportFragmentManager.beginTransaction().apply {
        replace(R.id.fragmentContainerMain, fragment)
        commit()
    }

    // Experiment for transparent status bar
    private fun Activity.makeStatusBarTransparent() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.apply {
                clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    val mode =
                        context?.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)
                    when (mode) {
                        Configuration.UI_MODE_NIGHT_YES -> {
                            decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        }
                        Configuration.UI_MODE_NIGHT_NO -> {
                            decorView.systemUiVisibility =
                                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
                        }
                        Configuration.UI_MODE_NIGHT_UNDEFINED -> {
                            decorView.systemUiVisibility =
                                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
                        }
                    }
                } else {
                    decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                }
                statusBarColor = Color.TRANSPARENT
            }
        }
    }

    private fun View.setMarginTop(marginTop: Int) {
        val menuLayoutParams = this.layoutParams as ViewGroup.MarginLayoutParams
        menuLayoutParams.setMargins(0, marginTop, 0, 0)
        this.layoutParams = menuLayoutParams
    }

    fun progressToString(p: Int): String {
        var str = ""
        val min = p / 1000 / 60
        val sec = p / 1000 % 60
        if (min == 0) {
            str += "0:"
        } else if (min < 10) {
            str = min.toString() + ":"
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

    fun setTvSongName(song: String) {
        this.tvSongName.text = song
    }

    override fun onDestroy() {
        super.onDestroy()
        safeThread = false
        player.release()
    }
}


