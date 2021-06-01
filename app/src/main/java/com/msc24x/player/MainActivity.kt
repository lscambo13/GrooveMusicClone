package com.msc24x.player

import android.Manifest
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.navigation.Navigation
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
            111
        )

        window.navigationBarColor = resources.getColor(R.color.variableAccent)

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
        val miniPlayer = findViewById<MotionLayout>(R.id.motion_miniplayer)
        if (miniPlayer.currentState == miniPlayer.endState)
            miniPlayer.transitionToStart()
        else
            super.onBackPressed()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menuSearch -> Toast.makeText(applicationContext, "Search", Toast.LENGTH_LONG)
                .show()
        }
        return true
    }
}


