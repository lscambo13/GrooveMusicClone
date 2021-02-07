package com.msc24x.player

import android.content.ContentUris
import android.content.Context
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


class SongsFragment(context: Context) : Fragment(R.layout.fragment_songs) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fragmentSongsPlaceholderText.text = "Hmm so empty here"
        Toast.makeText(context, "haa", Toast.LENGTH_SHORT).show()

        // populate the songs list
        val songslist = mutableListOf(
            Songs(null, "Song Name 1", "Artist Name 1", 0)
        )

        // show the song list in recycler view
        val adapter = SongAdapter(songslist)
        rvSongs.adapter = adapter
        rvSongs.layoutManager = LinearLayoutManager(context)


        fragmentSongsPlaceholderText.setOnClickListener {
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

            val artist = "xxx"
            val duration = 0

            query?.use { cursor ->
                // Cache column indices.
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)
                //val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                //val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)

                //Toast.makeText(context, "queried", Toast.LENGTH_SHORT).show()
                //songslist.add(Songs(null, "Song Name 2", "Artist Name 2", 0))
                //adapter.notifyItemInserted(songslist.size - 1)

                while (cursor.moveToNext()) {

                    fragmentSongsPlaceholderText.visibility = View.GONE
                    //Toast.makeText(context, "moveToNext", Toast.LENGTH_SHORT).show()

                    // Get values of columns for a given video.
                    val id = cursor.getLong(idColumn)
                    val song = cursor.getString(nameColumn)
                    //val duration = cursor.getInt(durationColumn)
                    //var artist = cursor.getString(artistColumn)
                    val contentUri: Uri = ContentUris.withAppendedId(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        id
                    )

                    // Stores column values and the contentUri in a local object
                    // that represents the media file.
                    songslist.add(Songs(contentUri, song, artist, duration))

                }
                adapter.notifyDataSetChanged()
                //adapter.notifyItemInserted(songslist.size - 1)
            }
        }

    }

}