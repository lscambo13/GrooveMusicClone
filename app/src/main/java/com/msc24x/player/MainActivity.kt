package com.msc24x.player

import android.Manifest
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.core.app.ActivityCompat
import androidx.navigation.Navigation
import com.msc24x.player.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private var mainActivityBinding: ActivityMainBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        mainActivityBinding = binding
        setContentView(binding.root)


        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
            111
        )

        setSupportActionBar(binding.materialToolbar)
        binding.materialToolbar.setNavigationOnClickListener {
            binding.drawerLayout.open()
        }

        binding.navigationView.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.menuHome -> {
                    Navigation.findNavController(this, R.id.fragment)
                        .navigate(R.id.action_settingsFragment_to_viewPagerFragment)
                    binding.materialToolbar.title = getString(R.string.title_activity_main)
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
                    binding.materialToolbar.title = getString(R.string.title_fragment_settings)
                }
            }
            binding.drawerLayout.close()
            true
        }
    }


    override fun onBackPressed() {
        val miniPlayer = findViewById<MotionLayout>(R.id.included_motion_miniplayer)
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


