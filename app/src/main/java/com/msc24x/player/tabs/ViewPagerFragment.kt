package com.msc24x.player.tabs

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
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
import androidx.palette.graphics.Palette
import com.google.android.material.tabs.TabLayoutMediator
import com.msc24x.player.CommonViewModel
import com.msc24x.player.Helpers.PAUSE
import com.msc24x.player.Helpers.PLAY
import com.msc24x.player.Helpers.SEEK_TO
import com.msc24x.player.Helpers.Utils
import com.msc24x.player.R
import com.msc24x.player.databinding.FragmentViewPagerBinding
import com.msc24x.player.mediaplayer.PlayerService

class ViewPagerFragment : Fragment() {

    private val viewModel: CommonViewModel by activityViewModels()
    private var fetchedDataFromService: Boolean = false
    private val receiver: BroadcastReceiver = ServiceBroadcastReceiver()
    private val playerServiceIntentFilter = IntentFilter()
    private var fragmentViewPagerBinding: FragmentViewPagerBinding? = null

    inner class ServiceBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent != null) {
                when (intent.action) {
                    PLAY -> play()
                    PAUSE -> pause()
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        if (PlayerService.isInitialized()) {
            fetchedDataFromService = true

            viewModel.songLength.value = PlayerService.getSongLength()
            viewModel.busy.value = PlayerService.isPlaying()
            viewModel.currentArtist.value = PlayerService.getTrackArtist()
            viewModel.currentSong.value = PlayerService.getTrackTitle()
            viewModel.currentPosition.value = PlayerService.getCurrentPlayerPos()
            viewModel.currentUri.value = PlayerService.getCurrentUri()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        playerServiceIntentFilter.addAction(PLAY)
        playerServiceIntentFilter.addAction(PAUSE)
        context?.registerReceiver(receiver, playerServiceIntentFilter)
    }

    override fun onPause() {
        super.onPause()
        context?.unregisterReceiver(receiver)
    }

    override fun onResume() {
        super.onResume()
        context?.registerReceiver(receiver, playerServiceIntentFilter)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_view_pager, container, false)
        val binding = FragmentViewPagerBinding.bind(view)
        fragmentViewPagerBinding = binding

        val fragmentList = arrayListOf(SongsFragment(), ArtistsFragment(), AlbumsFragment())
        val tabTitles = arrayListOf("Songs", "Artists", "Albums")

        val adapter = ViewPagerAdapter(
            fragmentList, childFragmentManager,
            lifecycle
        )

        val tabLayout = binding.tabLayout
        val viewPager = binding.viewPagerMain
        binding.viewPagerMain.adapter = adapter

        TabLayoutMediator(
            tabLayout,
            viewPager
        ) { tab, position ->
            tab.text = tabTitles[position]
            viewPager.setCurrentItem(tab.position, true)
        }.attach()

        viewModel.currentUri.observe(viewLifecycleOwner, {

            // Display main song info
            binding.includedMotionMiniplayer.tvSongName.text = viewModel.currentSong.value
            binding.includedMotionMiniplayer.tvArtistName.text = viewModel.currentArtist.value
            binding.includedMotionMiniplayer.seekbar.max = viewModel.songLength.value!!
            binding.includedMotionMiniplayer.tvTrackLength.text =
                Utils.progressToString(binding.includedMotionMiniplayer.seekbar.max)

            // Handle Uri change
            when (viewModel.busy.value) {
                true -> {
                    binding.includedMotionMiniplayer.tvTrackLength.text =
                        Utils.progressToString(viewModel.songLength.value!!)
                    setPlayBtnVisible(false)
                }
                false -> {
                    binding.includedMotionMiniplayer.tvTrackLength.text =
                        Utils.progressToString(viewModel.songLength.value!!)
                    if (fetchedDataFromService) {
                        fetchedDataFromService = false
                        setPlayBtnVisible(true)
                    } else {
                        setPlayBtnVisible(false)
                    }
                }
            }
            viewModel.busy.value = true

            // Update Song Art (Image and color)
            viewModel.decodedArt.value = extractTrackBitmap(it)
            viewModel.mutedColor.value = extractMutedColor(viewModel.decodedArt.value!!)
            binding.includedMotionMiniplayer.imgCoverArt.setImageBitmap(viewModel.decodedArt.value)

            // Handle muted color change
            if (viewModel.mutedColor.value != null) {
                updateUI(viewModel.mutedColor.value!!)
            }

            // Keep track position thread alive
            if (!trackPositionThread.isAlive) {
                trackPositionThread.start()
            }

        })


        binding.includedMotionMiniplayer.btnOutline.setOnClickListener {
            if (viewModel.busy.value == true) {
                pause()
            } else {
                play()
            }
        }

        binding.includedMotionMiniplayer.seekbar.setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    if (fromUser) {
                        binding.includedMotionMiniplayer.tvTimeCode.text =
                            Utils.progressToString(progress)
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {
                    seekBar!!.isSelected = true
                }

                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    seekTo(binding.includedMotionMiniplayer.seekbar.progress)
                    seekBar!!.isSelected = false
                }
            }
        )

        viewModel.currentPosition.observe(viewLifecycleOwner, {
            if (!binding.includedMotionMiniplayer.seekbar.isSelected) {
                binding.includedMotionMiniplayer.seekbar.progress =
                    viewModel.currentPosition.value!!
                binding.includedMotionMiniplayer.tvTimeCode.text =
                    Utils.progressToString(viewModel.currentPosition.value!!)
            }
        })

        return view
    }

    private fun setPlayBtnVisible(makeVisible: Boolean) {
        if (makeVisible) {
            fragmentViewPagerBinding?.includedMotionMiniplayer?.iconPlay?.visibility = View.VISIBLE
            fragmentViewPagerBinding?.includedMotionMiniplayer?.iconPause?.visibility =
                View.INVISIBLE
        } else {
            fragmentViewPagerBinding?.includedMotionMiniplayer?.iconPlay?.visibility =
                View.INVISIBLE
            fragmentViewPagerBinding?.includedMotionMiniplayer?.iconPause?.visibility = View.VISIBLE
        }
    }


    private var handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            viewModel.currentPosition.value = msg.what
        }
    }

    private var trackPositionThread = Thread {
        while (true) {
            val msg = Message()
            msg.what = PlayerService.getCurrentPlayerPos()
            handler.sendMessage(msg)
            Thread.sleep(100)
        }
    }

    private fun updateUI(color: Int) {
        fragmentViewPagerBinding?.includedMotionMiniplayer?.ContainerMiniPlayer?.setBackgroundColor(
            color
        )
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

    private fun extractMutedColor(art: Bitmap): Int {
        val myPalette = Palette.from(art).generate()
        var muted = myPalette.mutedSwatch
        if (muted == null) muted = myPalette.darkVibrantSwatch
        return muted!!.rgb
    }

    private fun play() {
        setPlayBtnVisible(false)
        val intent = Intent(context, PlayerService::class.java)
        intent.action = PLAY
        requireActivity().startService(intent)
        viewModel.busy.value = true
    }

    private fun pause() {
        setPlayBtnVisible(true)
        val intent = Intent(context, PlayerService::class.java)
        intent.action = PAUSE
        requireActivity().startService(intent)
        viewModel.busy.value = false
    }

    private fun seekTo(pos: Int) {
        val intent = Intent(context, PlayerService::class.java)
        intent.action = SEEK_TO
        intent.putExtra(SEEK_TO, pos)
        requireActivity().startService(intent)
    }

}

