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
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import androidx.palette.graphics.Palette
import com.google.android.material.tabs.TabLayoutMediator
import com.msc24x.player.CommonViewModel
import com.msc24x.player.R
import com.msc24x.player.mediaplayer.PlayerService
import kotlinx.android.synthetic.main.fragment_view_pager.view.*

class ViewPagerFragment : Fragment() {

    lateinit var songUri: Uri
    private val viewModel: CommonViewModel by activityViewModels()
    var safeThread = true

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



        view.mainSongInfo.setOnClickListener {

            if (viewModel.currentUri.value != null)
                Navigation.findNavController(requireActivity(), R.id.fragment)
                    .navigate(R.id.action_viewPagerFragment_to_nowPlayingFragment)
        }

        viewModel.currentUri.observe(viewLifecycleOwner, Observer {
            println("change detected uri- main")
            songUri = viewModel.currentUri.value!!
            if (viewModel.songLength.value != null) {
                view.seekbar.max = viewModel.songLength.value!!
                view.tvTrackLength.text = Utils.progressToString(view.seekbar.max)
            }

            when (viewModel.busy.value) {
                true -> {
                    println("player is playing")
                    play()
                    view.tvTrackLength.text =
                        Utils.progressToString(viewModel.songLength.value!!)
                    view.iconPlay.visibility = View.INVISIBLE
                    view.iconPause.visibility = View.VISIBLE
                }
                false -> {
                    println("player wasn't playing")
                    view.tvTrackLength.text =
                        Utils.progressToString(viewModel.songLength.value!!)
                    play()
                    view.iconPlay.visibility = View.INVISIBLE
                    view.iconPause.visibility = View.VISIBLE
                    viewModel.busy.value = true
                }

            }


            val mmr = MediaMetadataRetriever()
            var art: Bitmap
            val bfo = BitmapFactory.Options()
            mmr.setDataSource(requireContext(), it)
            val rawArt: ByteArray? = mmr.embeddedPicture

            if (rawArt != null) {
                art = BitmapFactory.decodeByteArray(rawArt, 0, rawArt.size, bfo)
                viewModel.decodedArt.value = art
                val myPalette = createPaletteSync(art)
                var muted = myPalette.mutedSwatch
                if (muted == null) {
                    muted = myPalette.darkVibrantSwatch
                }
                viewModel.mutedColor.value = muted?.rgb
            } else {
                art = BitmapFactory.decodeResource(
                    requireActivity().resources,
                    R.drawable.missing_album_art
                )
                viewModel.decodedArt.value = art
                val myPalette = createPaletteSync(art)
                val muted = myPalette.mutedSwatch
                viewModel.mutedColor.value = muted?.rgb
            }

            view.tvSongName.text = viewModel.currentSong.value
            view.tvArtistName.text = viewModel.currentArtist.value
        })


        viewModel.mutedColor.observe(viewLifecycleOwner, Observer {
            println("woah! new color")

            if (it != null) {
                view.ContainerMiniPlayer.setBackgroundColor(it)
                requireActivity().window.navigationBarColor = it
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    requireActivity().window.navigationBarDividerColor = it
                }
            }
        })

        view.btnOutline.setOnClickListener {
            if (viewModel.busy.value == true) {
                pause()
                println("click is playing")
                view.iconPlay.visibility = View.VISIBLE
                view.iconPause.visibility = View.INVISIBLE
                viewModel.busy.value = false
            } else {
                view.iconPlay.visibility = View.INVISIBLE
                view.iconPause.visibility = View.VISIBLE
                play()
                println("click wasn't playing")
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
                        seekTo(progress)
                        //player.seekTo(progress)
                        view.tvTimeCode.text = Utils.progressToString(progress)
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            }
        )

        viewModel.currentPosition.observe(viewLifecycleOwner, Observer {
            view.seekbar.progress = viewModel.currentPosition.value!!
            view.tvTimeCode.text = Utils.progressToString(viewModel.currentPosition.value!!)
        })


/*        @SuppressLint("HandlerLeak")
        var handler = object : Handler() {
            override fun handleMessage(msg: Message) {
                viewModel.currentPosition.value = msg.what
                savedInstanceState?.putInt("progressPos", msg.what)
            }
        }

        var msg = Message()
        var trackPlayerPos = Thread {
            while (viewModel.busy.value == true) {
                msg.what = viewModel.currentPosition.value!!//player.currentPosition
                //Preferences(this).setCurrentProgress(msg.what)
                handler.sendMessage(msg)
                Thread.sleep(100)
            }
        }
        trackPlayerPos.start()*/



        return view
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

    override fun onDestroy() {
        super.onDestroy()
        safeThread = false
    }

    private fun createPaletteSync(bitmap: Bitmap): Palette = Palette.from(bitmap).generate()

}