package com.msc24x.player.tabs

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.msc24x.player.CommonViewModel
import com.msc24x.player.R
import kotlinx.android.synthetic.main.now_playing_fragment.*


class NowPlayingFragment : Fragment() {

    companion object {
        fun newInstance() = NowPlayingFragment()
    }

    private val viewModel: CommonViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.now_playing_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)


        viewModel.currentUri.observe(viewLifecycleOwner, Observer {
            val mmr = MediaMetadataRetriever()
            val rawArt: ByteArray?
            var art: Bitmap
            val bfo = BitmapFactory.Options()
            mmr.setDataSource(requireContext(), it)
            rawArt = mmr.embeddedPicture
            if (rawArt != null) {
                art = BitmapFactory.decodeByteArray(rawArt, 0, rawArt.size, bfo)
                imgCoverArt.setImageBitmap(art)
            }
            tvArtistName.text = viewModel.currentArtist.value
            tvSongName.text = viewModel.currentSong.value
            tvTrackLength.text = viewModel.progressToString(viewModel.songLength.value!!)
        })

        seekbar.max = viewModel.songLength.value!!

        viewModel.currentPosition.observe(viewLifecycleOwner, Observer {
            seekbar.progress = viewModel.currentPosition.value!!
            tvTimeCode.text = viewModel.progressToString(viewModel.currentPosition.value!!)
        })
    }

    override fun onResume() {
        super.onResume()
        (activity as AppCompatActivity?)!!.supportActionBar!!.hide()
    }

    override fun onStop() {
        super.onStop()
        (activity as AppCompatActivity?)!!.supportActionBar!!.show()
    }

}