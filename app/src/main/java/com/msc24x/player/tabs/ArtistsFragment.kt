package com.msc24x.player.tabs

import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.snackbar.Snackbar
import com.msc24x.player.R
import com.msc24x.player.adapters.ArtistAdapter
import com.msc24x.player.databinding.FragmentArtistsBinding

class ArtistsFragment : Fragment(), ArtistAdapter.OnItemClickListener {

    var artistsList = mutableListOf<String>()
    private val adapter = ArtistAdapter(artistsList, this)


    private val collection: Uri =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Audio.Media.getContentUri(
                MediaStore.VOLUME_EXTERNAL
            )
        } else {
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        }

    @RequiresApi(Build.VERSION_CODES.R)
    private val projection = arrayOf(
        MediaStore.Audio.Media.ALBUM_ARTIST
    )

    private val sortOrder = "${MediaStore.Audio.Media.ALBUM_ARTIST} ASC"

    @RequiresApi(Build.VERSION_CODES.R)
    private fun loadArtists() {
        val query = requireContext().contentResolver.query(
            collection,
            projection,
            null,
            null,
            sortOrder
        )
        query?.use { cursor ->
            // Cache column indices.
            val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ARTIST)
            while (cursor.moveToNext()) {
                var artist = cursor.getString(artistColumn)
                when (artist) {
                    null -> artist = "Unknown"
                }

                if (!artistsList.contains(artist))
                    artistsList.add(artist)
            }
        }
        adapter.notifyDataSetChanged()
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = layoutInflater.inflate(R.layout.fragment_artists, container, false)
        val viewPagerMain = activity?.findViewById<ViewPager2>(R.id.viewPagerMain)
        val binding = FragmentArtistsBinding.bind(view)
        binding.fragmentArtistsPlaceholderText.setOnClickListener {
            viewPagerMain?.currentItem = 2
        }

        //val bPad = requireActivity().blurMiniPlayer.layoutParams.height
        //view.rvArtists.setPadding(0, 0, 0, bPad)

        binding.fragmentArtistsPlaceholderText.visibility = View.GONE
        binding.rvArtists.adapter = adapter
        binding.rvArtists.layoutManager = GridLayoutManager(requireContext(), 3)

        return view
    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadArtists()
        println("loaded artists")
    }

    override fun onItemClick(position: Int) {
        Snackbar.make(requireView(), "nice", Snackbar.LENGTH_SHORT).show()
    }
}
