package com.msc24x.player

import android.Manifest
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import eightbitlab.com.blurview.RenderScriptBlur
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {



    //lateinit var viewModel: CommonViewModel


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
                    Toast.makeText(applicationContext, "Settings", Toast.LENGTH_SHORT).show()
                    Navigation.findNavController(this, R.id.fragment)
                        .navigate(R.id.action_viewPagerFragment_to_settingsFragment)
                    materialToolbar.title = getString(R.string.title_fragment_settings)
                    //it.isChecked = true
                }
            }
            drawerLayout.close()
            true
        }


        //viewModel = ViewModelProvider(this).get(CommonViewModel::class.java)


        //updateFirst()
        //enableBlur()

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

}


