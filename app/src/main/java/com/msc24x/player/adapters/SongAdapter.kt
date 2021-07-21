package com.msc24x.player.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.msc24x.player.Helpers.Utils
import com.msc24x.player.R
import com.msc24x.player.data.Songs
import com.msc24x.player.databinding.ListItemSongsBinding


class SongAdapter(
    var songs: List<Songs>,
    val listener: OnItemClickListener
) : RecyclerView.Adapter<SongAdapter.SongViewHolder>() {

    private lateinit var binding: ListItemSongsBinding


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.list_item_songs, parent, false)
        binding = ListItemSongsBinding.bind(view)
        return SongViewHolder(view)
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        holder.itemView.apply {
            binding.tvListSongName.text = songs[position].SongTitle
            val listArtist = songs[position].ArtistName + " • " + songs[position].AlbumName
            binding.tvListArtistName.text = listArtist
            binding.tvListDuration.text = Utils.progressToString(songs[position].Duration)
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