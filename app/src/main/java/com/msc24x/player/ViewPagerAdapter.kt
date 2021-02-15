package com.msc24x.player

import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.adapter.FragmentStateAdapter


class ViewPagerAdapter(fa: Fragment) : FragmentStateAdapter(fa) {
    val eventList = listOf(SongsFragment(), ArtistsFragment(), AlbumsFragment())

    // Layout "layout_demo_viewpager2_cell.xml" will be defined later
    override fun getItemCount() = eventList.count()
    override fun createFragment(position: Int): Fragment {
        MainActivity().switchTab(eventList[position])
        return eventList[position]
    }

    class EventViewHolder(view: View) : RecyclerView.ViewHolder(view)
}