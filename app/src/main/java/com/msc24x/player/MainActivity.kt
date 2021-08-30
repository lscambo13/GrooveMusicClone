package com.msc24x.player

import android.Manifest
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.core.app.ActivityCompat
import androidx.navigation.Navigation
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.motion_miniplayer.*

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val commonViewModel: CommonViewModel by viewModels()
    private lateinit var activityMainSearchView: SearchView


    override fun onStart() {
        super.onStart()
        setMiniPlayerTransitionListener()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
            111
        )

        setSupportActionBar(materialToolbar)
        materialToolbar.setNavigationOnClickListener {
            drawerLayout.open()
        }

        navigationView.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.menuHome -> {
                    Navigation.findNavController(this, R.id.fragment)
                        .navigate(R.id.action_settingsFragment_to_viewPagerFragment)
                    materialToolbar.title = getString(R.string.title_activity_main)
                }
                R.id.menuRecent -> {
                    Toast.makeText(applicationContext, "Recent", Toast.LENGTH_SHORT).show()
                }
                R.id.menuPlaylist -> {
                    Toast.makeText(applicationContext, "Playlist", Toast.LENGTH_SHORT).show()
                }
                R.id.menuSettings -> {
                    Navigation.findNavController(this, R.id.fragment)
                        .navigate(R.id.action_viewPagerFragment_to_settingsFragment)
                    materialToolbar.title = getString(R.string.title_fragment_settings)
                }
            }
            drawerLayout.close()
            true
        }
    }

    override fun onBackPressed() {
        closeSearchVew()

        if (!expandMiniPlayer(false))
            super.onBackPressed()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menuSearch -> {
                println("search clicked")

                expandMiniPlayer(false)

                activityMainSearchView = item.actionView as SearchView

                activityMainSearchView.setOnQueryTextListener(object :
                    SearchView.OnQueryTextListener {
                    override fun onQueryTextSubmit(query: String?): Boolean {
                        closeSearchVew()
                        return true
                    }

                    override fun onQueryTextChange(newText: String?): Boolean {
                        println("query changed")
                        commonViewModel.searchQuery.value = newText.orEmpty()
                        return true
                    }
                })


            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun closeSearchVew() {
        if (this::activityMainSearchView.isInitialized)
            invalidateOptionsMenu()
    }

    private fun expandMiniPlayer(show: Boolean): Boolean {
        val miniPlayer = findViewById<MotionLayout>(R.id.motion_miniplayer)

        when (miniPlayer.currentState) {
            miniPlayer.endState ->
                if (!show) {
                    miniPlayer.transitionToStart()
                } else
                    return false
            miniPlayer.startState ->
                if (show) {
                    miniPlayer.transitionToEnd()
                } else
                    return false
        }

        return true
    }

    private fun setMiniPlayerTransitionListener() {
        /* Motion layout for this must not be null if this method is to be called */

        motion_miniplayer.setTransitionListener(object : MotionLayout.TransitionListener {
            override fun onTransitionCompleted(motionLayout: MotionLayout?, currentId: Int) {
                if (motionLayout != null) {
                    if (motionLayout.currentState == motion_miniplayer.endState) {
                        closeSearchVew()
                    }
                }
            }

            override fun onTransitionStarted(
                motionLayout: MotionLayout?,
                startId: Int,
                endId: Int
            ) {
            }

            override fun onTransitionChange(
                motionLayout: MotionLayout?,
                startId: Int,
                endId: Int,
                progress: Float
            ) {
            }

            override fun onTransitionTrigger(
                motionLayout: MotionLayout?,
                triggerId: Int,
                positive: Boolean,
                progress: Float
            ) {
            }
        })
    }

}


