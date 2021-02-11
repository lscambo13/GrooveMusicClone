package com.msc24x.player

import android.content.ContentUris
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_songs.*
import java.util.concurrent.TimeUnit


class SongsFragment : Fragment(R.layout.fragment_songs) {

    // populate the songs list
    val songsList = mutableListOf(
        Songs(null, "Song Name 1", "Artist Name 1", 0)
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fragmentSongsPlaceholderText.text = "Hmm so empty here"
        Toast.makeText(context, "haa", Toast.LENGTH_SHORT).show()

        // show the song list in recycler view
        val adapter = SongAdapter(songsList)
        rvSongs.adapter = adapter
        rvSongs.layoutManager = LinearLayoutManager(context)


        val collection =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Audio.Media.getContentUri(
                    MediaStore.VOLUME_EXTERNAL
                )
            } else {
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            }

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.ALBUM_ARTIST
        )

        // Show only videos that are at least 5 minutes in duration.
        val selection = "${MediaStore.Video.Media.DURATION} >= ?"
        val selectionArgs = arrayOf(
            TimeUnit.MILLISECONDS.convert(2, TimeUnit.MINUTES).toString()
        )

        // Display videos in alphabetical order based on their display name.
        val sortOrder = "${MediaStore.Audio.Media.DISPLAY_NAME} ASC"

        var query = context!!.contentResolver.query(
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
            val fileNameColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)
            val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ARTIST)
            while (cursor.moveToNext()) {
                fragmentSongsPlaceholderText.visibility = View.GONE
                val id = cursor.getLong(idColumn)
                var song = cursor.getString(nameColumn)
                var fileName = cursor.getString(fileNameColumn)
                when (song) {
                    null -> song = fileName
                }
                val duration = cursor.getInt(durationColumn)
                var artist = cursor.getString(artistColumn)
                when (artist) {
                    null -> artist = "Unknown"
                }
                val contentUri: Uri = ContentUris.withAppendedId(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    id
                )

                // Stores column values and the contentUri in a local object
                // that represents the media file.
                songsList.add(Songs(contentUri, song, artist, duration))
            }
            adapter.notifyDataSetChanged()
            //adapter.notifyItemInserted(songsList.size - 1)
        }
    }
}