package com.msc24x.player.tabs

import android.content.ContentUris
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.view.get
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.msc24x.player.CommonViewModel
import com.msc24x.player.Helpers.Constants.PLAY_SONG
import com.msc24x.player.Helpers.Constants.TRACK_URI
import com.msc24x.player.R
import com.msc24x.player.adapters.SongAdapter
import com.msc24x.player.data.database.Track
import com.msc24x.player.mediaplayer.PlayerService
import kotlinx.android.synthetic.main.fragment_songs.*
import kotlinx.android.synthetic.main.fragment_songs.view.*
import java.util.concurrent.TimeUnit


class SongsFragment : Fragment(), SongAdapter.OnItemClickListener {

    private val viewModel: CommonViewModel by activityViewModels()

    // populate the songs list
    private var trackList = mutableListOf<Track>()
    private val playlistName = "songs_playlist"

    private val adapter = SongAdapter(trackList, this)

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
        MediaStore.Audio.Media._ID,
        MediaStore.Audio.Media.TITLE,
        MediaStore.Audio.Media.DISPLAY_NAME,
        MediaStore.Audio.Media.ALBUM,
        MediaStore.Audio.Media.DURATION,
        MediaStore.Audio.Media.ALBUM_ARTIST
    )

    // Show only videos that are at least 5 minutes in duration.
    @RequiresApi(Build.VERSION_CODES.Q)
    private val selection = "${MediaStore.Video.Media.DURATION} >= ?"
    private val selectionArgs = arrayOf(
        TimeUnit.MILLISECONDS.convert(2, TimeUnit.MINUTES).toString()
    )

    // Display videos in alphabetical order based on their display name.
    private val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"

    @RequiresApi(Build.VERSION_CODES.R)
    private fun loadMedia() {
        var trackId: Int = 0
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
                val fileName = cursor.getString(fileNameColumn)
                val song = cursor.getString(nameColumn) ?: fileName
                val album = cursor.getString(albumColumn) ?: "Unknown"
                val duration = cursor.getInt(durationColumn)
                val artist = cursor.getString(artistColumn) ?: "Unknown"
                val contentUri: Uri = ContentUris.withAppendedId(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    id
                )
                // Stores column values and the contentUri in a local object
                // that represents the media file.
                trackList.add(
                    Track(
                        trackId++,
                        contentUri.toString(),
                        song,
                        artist,
                        album,
                        duration.toLong()
                    )
                )//Songs(contentUri, song, artist, album, duration))
            }
        }
        adapter.notifyDataSetChanged()
    }


    override fun onItemClick(position: Int) {
        viewModel.isPlaying.value = true
        viewModel.currentTrack.value = trackList[position]

        PlayerService.setTrackPlaylist(trackList, playlistName)
        /*
        updateSong(position)
        updateArtist(position)
        updateDuration(position)
        updateUri(position)*/
        adapter.notifyItemChanged(position)
        playSelectedSong()
    }

    fun updatePlayIndicator(id: Int) {
        rvSongs[id].isActivated = true
        rvSongs[id].isSelected = true
        //TODO - - Play indicator
    }

    /*private fun updateDuration(id: Int) {
        viewModel.songLength.value = trackList[id].duration.toInt()
    }

    private fun updateSong(id: Int) {
        val song = trackList[id].title
        viewModel.currentSong.value = song
    }

    private fun updateArtist(id: Int) {
        val artist = trackList[id].artist_name
        viewModel.currentArtist.value = artist
    }

    private fun updateUri(id: Int) {
        viewModel.currentUri.value = Uri.parse(trackList[id].uri)
    }*/

    private fun playSelectedSong() {
        val intent = Intent(context, PlayerService::class.java)
        intent.action = PLAY_SONG
        intent.putExtra(TRACK_URI, viewModel.currentTrack.value?.uri)
        requireActivity().startService(intent)
        viewModel.isPlaying.value = true
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_songs, container, false)
        view.fragmentSongsPlaceholderText.visibility = GONE
        view.rvSongs.adapter = adapter
        view.rvSongs.layoutManager = LinearLayoutManager(context)


        viewModel.searchQuery.observe(viewLifecycleOwner, Observer {
            adapter.songs = refineList(trackList, it)
            adapter.notifyDataSetChanged()
        })

        return view
    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadMedia()
    }

    private fun refineList(list: List<Track>, search: String): List<Track> {
        val refinedList = mutableListOf<Track>()
        list.forEach {
            if (it.title.contains(search, true) || it.artist_name.contains(
                    search,
                    true
                ) || it.album_name.contains(search, true)
            )
                refinedList.add(it)
        }
        println(refinedList.size)
        return refinedList
    }
}