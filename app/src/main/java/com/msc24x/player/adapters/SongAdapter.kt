package com.msc24x.player.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.msc24x.player.Helpers.Utils
import com.msc24x.player.R
import com.msc24x.player.data.database.Track
import kotlinx.android.synthetic.main.list_item_songs.view.*


class SongAdapter(
    var songs: List<Track>,
    val listener: OnItemClickListener
) : RecyclerView.Adapter<SongAdapter.SongViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.list_item_songs, parent, false)
        return SongViewHolder(view)
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        holder.itemView.apply {
            tvListSongName.text = songs[position].title
            val listArtist = songs[position].artist_name + " • " + songs[position].album_name
            tvListArtistName.text = listArtist
            tvListDuration.text = Utils.progressToString(songs[position].duration.toInt())
        }
    }

    override fun getItemCount(): Int {
        return songs.size
    }

    inner class SongViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            val position = adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                listener.onItemClick(position)
            }
        }
    }

    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }


}