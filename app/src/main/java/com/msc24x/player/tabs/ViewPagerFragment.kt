package com.msc24x.player.tabs

import android.annotation.SuppressLint
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import com.google.android.material.tabs.TabLayoutMediator
import com.msc24x.player.CommonViewModel
import com.msc24x.player.R
import com.msc24x.player.SongAdapter
import kotlinx.android.synthetic.main.fragment_view_pager.view.*

class ViewPagerFragment : Fragment(), SongAdapter.OnItemClickListener {

    lateinit var player: MediaPlayer
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
            viewPager,
            TabLayoutMediator.TabConfigurationStrategy { tab, position ->
                tab.text = tabTitles[position]
                viewPager.setCurrentItem(tab.position, true)
            }).attach()



        view.mainSongInfo.setOnClickListener {

            if (viewModel.currentUri.value != null)
                Navigation.findNavController(requireActivity(), R.id.fragment)
                    .navigate(R.id.action_viewPagerFragment_to_nowPlayingFragment)
        }

        when (this::player.isInitialized) {
            true -> {

            }
            false -> {
                player = MediaPlayer.create(requireActivity(), R.raw.song)
                view.iconPlay.visibility = View.VISIBLE
                view.iconPause.visibility = View.INVISIBLE
                view.tvTrackLength.text = viewModel.progressToString(player.duration)
                viewModel.busy.value = "false"
            }
        }
        player.isLooping = true


//        viewModel.currentSong.observe(viewLifecycleOwner, Observer {
//            println("change detected song- main")
//            view.tvSongName.text = viewModel.currentSong.value
//        })
//        viewModel.currentArtist.observe(viewLifecycleOwner, Observer {
//            println("change detected artist- main")
//            view.tvArtistName.text = viewModel.currentArtist.value
//        })
        viewModel.currentUri.observe(viewLifecycleOwner, Observer {
            println("change detected uri- main")

            songUri = viewModel.currentUri.value!!

            when (viewModel.busy.value) {
                "true" -> {
                    println("player is playing")
                    play()
                    viewModel.songLength.value = player.duration
                    view.tvTrackLength.text =
                        viewModel.progressToString(viewModel.songLength.value!!)
                    view.iconPlay.visibility = View.INVISIBLE
                    view.iconPause.visibility = View.VISIBLE
                    //busy = "true"
                }
                "false" -> {
                    player = MediaPlayer.create(requireActivity(), songUri)
                    println("player wasn't playing")
                    viewModel.songLength.value = player.duration
                    view.tvTrackLength.text =
                        viewModel.progressToString(viewModel.songLength.value!!)
                    player.start()
                    view.iconPlay.visibility = View.INVISIBLE
                    view.iconPause.visibility = View.VISIBLE
                    viewModel.busy.value = "true"
                }
            }

            view.tvSongName.text = viewModel.currentSong.value
            view.tvArtistName.text = viewModel.currentArtist.value
        })



        view.btnOutline.setOnClickListener {
            if (player.isPlaying) {
                player.pause()
                println("click is playing")
                view.iconPlay.visibility = View.VISIBLE
                view.iconPause.visibility = View.INVISIBLE
                viewModel.busy.value = "false"
            } else {
                view.iconPlay.visibility = View.INVISIBLE
                view.iconPause.visibility = View.VISIBLE
                player.start()
                println("click wasn't playing")
                viewModel.busy.value = "true"
            }
        }


        view.seekbar.max = player.duration
        view.seekbar.setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    if (fromUser) {
                        player.seekTo(progress)
                        view.tvTimeCode.text = viewModel.progressToString(progress)
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            }
        )

        @SuppressLint("HandlerLeak")
        var handler = object : Handler() {
            override fun handleMessage(msg: Message) {
                viewModel.currentPosition.value = msg.what
                savedInstanceState?.putInt("progressPos", msg.what)

            }
        }

        viewModel.currentPosition.observe(viewLifecycleOwner, Observer {
            view.seekbar.progress = viewModel.currentPosition.value!!
            view.tvTimeCode.text = viewModel.progressToString(viewModel.currentPosition.value!!)
        })



        Thread {
            while (safeThread) {
                var msg = Message()
                msg.what = player.currentPosition
                //Preferences(this).setCurrentProgress(msg.what)
                handler.sendMessage(msg)
                Thread.sleep(100)
            }
        }.start()


        return view
    }

    fun play() {
        songUri = viewModel.currentUri.value!!
        player.pause()
        player.reset()
        player = MediaPlayer.create(requireActivity(), songUri)
        player.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        safeThread = false
        //player.release()
    }

    override fun onItemClick(position: Int) {

        play()
    }

}