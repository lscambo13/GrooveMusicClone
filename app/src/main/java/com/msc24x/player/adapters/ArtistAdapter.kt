package com.msc24x.player.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.msc24x.player.R
import com.msc24x.player.databinding.ListItemSongsBinding


class ArtistAdapter(
    var artists: List<String>,
    val listener: OnItemClickListener
) : RecyclerView.Adapter<ArtistAdapter.ArtistViewHolder>() {

    private lateinit var binding: ListItemSongsBinding

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArtistViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.list_item_artists, parent, false)
        binding = ListItemSongsBinding.bind(view)
        return ArtistViewHolder(view)
    }

    override fun onBindViewHolder(holder: ArtistViewHolder, position: Int) {
        holder.itemView.apply {
            binding.tvListArtistName.text = artists[position]
        }
    }

    override fun getItemCount(): Int {
        return artists.size
    }

    inner class ArtistViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
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