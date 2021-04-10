package com.msc24x.player.tabs

import android.content.ContentUris
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.view.get
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.msc24x.player.CommonViewModel
import com.msc24x.player.R
import com.msc24x.player.SongAdapter
import com.msc24x.player.Songs
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_songs.*
import kotlinx.android.synthetic.main.fragment_songs.view.*
import kotlinx.android.synthetic.main.list_songs.view.*
import java.util.concurrent.TimeUnit


class SongsFragment : Fragment(), SongAdapter.OnItemClickListener {

    //lateinit var viewModel: CommonViewModel
    private val viewModel: CommonViewModel by activityViewModels()

    // populate the songs list
    var songsList = mutableListOf<Songs>()
/*    val songsList = mutableListOf(
        Songs(null, "Song Name 1", "Artist Name 1", "Album Name 1", 0, BitmapFactory.decodeResource(
            context?.resources, R.id.ic_default_art))
    )*/


    private val adapter = SongAdapter(songsList, this)

    private val collection: Uri =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Audio.Media.getContentUri(
                MediaStore.VOLUME_EXTERNAL
            )
        } else {
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        }

    private val projection = arrayOf(
        MediaStore.Audio.Media._ID,
        MediaStore.Audio.Media.TITLE,
        MediaStore.Audio.Media.DISPLAY_NAME,
        MediaStore.Audio.Media.ALBUM,
        MediaStore.Audio.Media.DURATION,
        MediaStore.Audio.Media.ALBUM_ARTIST
    )

    // Show only videos that are at least 5 minutes in duration.
    private val selection = "${MediaStore.Video.Media.DURATION} >= ?"
    private val selectionArgs = arrayOf(
        TimeUnit.MILLISECONDS.convert(2, TimeUnit.MINUTES).toString()
    )

    // Display videos in alphabetical order based on their display name.
    private val sortOrder = "${MediaStore.Audio.Media.DISPLAY_NAME} ASC"

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun loadMedia() {

        val query = requireContext().contentResolver.query(
            collection,
            projection,
            selection,
            selectionArgs,
            sortOrder
        )
        query?.use { cursor ->
            // Cache column indices.
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val albumColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val fileNameColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)
            val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ARTIST)

            while (cursor.moveToNext()) {

                val id = cursor.getLong(idColumn)
                var song = cursor.getString(nameColumn)
                val fileName = cursor.getString(fileNameColumn)
                var album = cursor.getString(albumColumn)
                val duration = cursor.getInt(durationColumn)
                var artist = cursor.getString(artistColumn)

                when (song) {
                    null -> song = fileName
                }
                when (album) {
                    null -> album = "Unknown"
                }

                when (artist) {
                    null -> artist = "Unknown"
                }

                val contentUri: Uri = ContentUris.withAppendedId(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    id
                )
                // Stores column values and the contentUri in a local object
                // that represents the media file.
                songsList.add(Songs(contentUri, song, artist, album, duration))
            }
        }
        //songsList.removeAt(0)
        adapter.notifyDataSetChanged()
    }


    override fun onItemClick(position: Int) {
        val clickedItem = songsList[position]
        updateSong(position)
        updateArtist(position)
        updateUri(position)
        //updatePlayIndicator(position)
        Snackbar.make(requireView(), "item $position, ${clickedItem.uri}", Snackbar.LENGTH_LONG)
            .setAnchorView(requireActivity().blurMiniPlayer)
            .show()
        //Toast.makeText(activity, "item $position, ${clickedItem.uri}", Toast.LENGTH_SHORT).show()
        //rvSongs[position].tvListSongName.setTextColor(context!!.getColor(R.color.variableAccent))
        //rvSongs[position].tvListArtistName.setTextColor(context!!.getColor(R.color.variableAccent))
        adapter.notifyItemChanged(position)
    }

    fun updatePlayIndicator(id: Int) {
        val songInd = rvSongs[id].imgPlayIndicator
        when (songInd.visibility) {
            VISIBLE -> {
                songInd.visibility = GONE
                rvSongs[id].tvListSongName.setPadding(16, 0, 0, 0)
            }
            GONE -> {
                songInd.visibility = VISIBLE
                rvSongs[id].tvListSongName.setPadding(0, 0, 0, 0)
            }
        }

    }

    fun updateSong(id: Int) {
        val song = songsList[id].SongTitle
        viewModel.currentSong.value = song
        //requireActivity().tvSongName.text = viewModel.getSong().value
    }

    fun updateArtist(id: Int) {
        val artist = songsList[id].ArtistName
        viewModel.currentArtist.value = artist
        //requireActivity().tvArtistName.text = viewModel.getArtist().value

    }

    fun updateUri(id: Int) {
        val uri = songsList[id].uri
        viewModel.currentUri.value = uri!!
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_songs, container, false)

        val tPad = requireActivity().blurAppBar.layoutParams.height
        val bPad = requireActivity().blurMiniPlayer.layoutParams.height
        view.rvSongs.setPadding(0, 0, 0, bPad)

        view.fragmentSongsPlaceholderText.visibility = GONE

        view.rvSongs.adapter = adapter
        view.rvSongs.layoutManager = LinearLayoutManager(context)
        return view
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadMedia()
        println("loaded songs")
    }

}