package com.msc24x.player

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.list_songs.view.*

class SongAdapter(
    var songs: List<Songs>
) : RecyclerView.Adapter<SongAdapter.SongViewHolder>() {
    inner class SongViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_songs, parent, false)
        return SongViewHolder(view)
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        holder.itemView.apply {
            tvListSongName.text = songs[position].SongTitle
            val listArtist = songs[position].ArtistName + " • " + songs[position].AlbumName
            tvListArtistName.text = listArtist
            tvListDuration.text = MainActivity().progressToString(songs[position].Duration)
        }
    }

    override fun getItemCount(): Int {
        return songs.size
    }
}