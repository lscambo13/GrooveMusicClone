package com.msc24x.player.tabs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.msc24x.player.R
import com.msc24x.player.databinding.FragmentAlbumsBinding

class AlbumsFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = layoutInflater.inflate(R.layout.fragment_albums, container, false)
        val binding = FragmentAlbumsBinding.bind(view)

        val viewPagerMain = activity?.findViewById<ViewPager2>(R.id.viewPagerMain)
        binding.placeholder.setOnClickListener {
            viewPagerMain?.currentItem = 0
        }
        return view
    }
}