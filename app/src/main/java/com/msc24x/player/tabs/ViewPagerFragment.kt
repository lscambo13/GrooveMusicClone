package com.msc24x.player.tabs

import Helpers.PAUSE
import Helpers.PLAY
import Helpers.SEEK_TO
import Helpers.Utils
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.*
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.toDrawable
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.palette.graphics.Palette
import com.google.android.material.tabs.TabLayoutMediator
import com.msc24x.player.CommonViewModel
import com.msc24x.player.R
import com.msc24x.player.mediaplayer.PlayerService
import kotlinx.android.synthetic.main.fragment_view_pager.view.*
import kotlinx.android.synthetic.main.motion_miniplayer.*
import kotlinx.android.synthetic.main.motion_miniplayer.view.*

class ViewPagerFragment : Fragment() {

    private val viewModel: CommonViewModel by activityViewModels()
    private var fetchedDataFromService: Boolean = false

    override fun onStart() {
        super.onStart()
        if (PlayerService.isInitialized()) {
            fetchedDataFromService = true
            viewModel.songLength.value = PlayerService.getSongLength()
            viewModel.busy.value = PlayerService.isPlaying()
            viewModel.currentArtist.value = PlayerService.getTrackArtist()
            viewModel.currentSong.value = PlayerService.getTrackTitle()
            viewModel.currentPosition.value = PlayerService.getCurrentPlayerPos()
            /* THIS WILL PLAY THE SONG WHEN WE GET BACK INSIDE THE APP EVEN IF THE SERVICE WAS NOT
            * PLAYING IT EARLIER TODO("onStart bug") */
            viewModel.currentUri.value = PlayerService.getCurrentUri()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_view_pager, container, false)

        val fragmentList = arrayListOf<Fragment>(
            SongsFragment(),
            ArtistsFragment(),
            AlbumsFragment()
        )

        val tabTitles = arrayListOf<String>(
            "Songs",
            "Artists",
            "Albums"
        )

        val adapter = ViewPagerAdapter(
            fragmentList, childFragmentManager,
            lifecycle
        )

        val tabLayout = view.tabLayout
        val viewPager = view.viewPagerMain
        view.viewPagerMain.adapter = adapter

        TabLayoutMediator(
            tabLayout,
            viewPager
        ) { tab, position ->
            tab.text = tabTitles[position]
            viewPager.setCurrentItem(tab.position, true)
        }.attach()

        viewModel.currentUri.observe(viewLifecycleOwner, Observer {

            // Display main song info
            motion_miniplayer.visibility = View.VISIBLE
            view.tvSongName.text = viewModel.currentSong.value
            view.tvArtistName.text = viewModel.currentArtist.value
            view.seekbar.max = viewModel.songLength.value!!
            view.tvTrackLength.text = Utils.progressToString(view.seekbar.max)

            // Handle Uri change
            when (viewModel.busy.value) {
                true -> {
                    view.tvTrackLength.text = Utils.progressToString(viewModel.songLength.value!!)
                    view.iconPlay.visibility = View.INVISIBLE
                    view.iconPause.visibility = View.VISIBLE
                    viewModel.busy.value = true
                }
                false -> {
                    view.tvTrackLength.text = Utils.progressToString(viewModel.songLength.value!!)
                    if (fetchedDataFromService) {
                        fetchedDataFromService = false
                        view.iconPlay.visibility = View.VISIBLE
                        view.iconPause.visibility = View.INVISIBLE
                    } else {
                        view.iconPlay.visibility = View.INVISIBLE
                        view.iconPause.visibility = View.VISIBLE
                        viewModel.busy.value = true
                    }
                }
            }

            // Update Song Art (Image and color)
            viewModel.decodedArt.value = extractTrackBitmap(it)
            viewModel.mutedColor.value = extractMutedColor(viewModel.decodedArt.value!!).rgb
            view.imgCoverArt.setImageBitmap(viewModel.decodedArt.value)

            // Handle muted color change
            if (viewModel.mutedColor.value != null) {
                updateUI_color(viewModel.mutedColor.value!!)
            }

            // Keep track position thread alive
            if (!trackPlayerPos.isAlive)
                trackPlayerPos.start()
        })

        view.btnOutline.setOnClickListener {
            if (viewModel.busy.value == true) {
                pause()
                view.iconPlay.visibility = View.VISIBLE
                view.iconPause.visibility = View.INVISIBLE
                viewModel.busy.value = false
            } else {
                view.iconPlay.visibility = View.INVISIBLE
                view.iconPause.visibility = View.VISIBLE
                play()
                viewModel.busy.value = true
            }
        }

        view.seekbar.setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    if (fromUser) {
                        view.tvTimeCode.text = Utils.progressToString(progress)
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {
                    seekBar!!.isSelected = true
                }

                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    seekBar!!.isSelected = false
                    seekTo(seekbar.progress)
                }
            }
        )

        viewModel.currentPosition.observe(viewLifecycleOwner, Observer {
            if (!seekbar.isSelected) {
                view.seekbar.progress = viewModel.currentPosition.value!!
                view.tvTimeCode.text = Utils.progressToString(viewModel.currentPosition.value!!)
            }
        })
        return view
    }


    private var handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            viewModel.currentPosition.value = msg.what
        }
    }

    private var trackPlayerPos = Thread {
        while (true) {
            var msg = Message()
            msg.what = PlayerService.getCurrentPlayerPos()
            handler.sendMessage(msg)
            Thread.sleep(100)
        }
    }

    private fun updateUI_color(color: Int) {
        ContainerMiniPlayer.setBackgroundColor(color)
        requireActivity().window.navigationBarColor = color
        requireActivity().window.statusBarColor = color
        (activity as AppCompatActivity?)!!.supportActionBar!!.setBackgroundDrawable(
            color.toDrawable()
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            requireActivity().window.navigationBarDividerColor = color
        }
    }

    private fun extractTrackBitmap(uri: Uri): Bitmap {
        val mmr = MediaMetadataRetriever()
        val art: Bitmap
        val bfo = BitmapFactory.Options()
        mmr.setDataSource(requireContext(), uri)
        val rawArt: ByteArray? = mmr.embeddedPicture

        art = if (rawArt != null) {
            BitmapFactory.decodeByteArray(rawArt, 0, rawArt.size, bfo)
        } else {
            BitmapFactory.decodeResource(
                requireActivity().resources,
                R.drawable.missing_album_art
            )
        }
        return art
    }

    private fun extractMutedColor(art: Bitmap): Palette.Swatch {
        val myPalette = Palette.from(art).generate()
        var muted = myPalette.mutedSwatch
        if (muted == null) muted = myPalette.darkVibrantSwatch
        return muted!!
    }

    private fun play() {
        val intent = Intent(context, PlayerService::class.java)
        intent.action = PLAY
        requireActivity().startService(intent)
    }

    private fun pause() {
        val intent = Intent(context, PlayerService::class.java)
        intent.action = PAUSE
        requireActivity().startService(intent)
    }

    private fun seekTo(pos: Int) {
        val intent = Intent(context, PlayerService::class.java)
        intent.action = SEEK_TO
        intent.putExtra(SEEK_TO, pos)
        requireActivity().startService(intent)
    }

}

