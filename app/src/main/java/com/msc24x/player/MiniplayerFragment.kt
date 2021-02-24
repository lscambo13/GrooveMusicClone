package com.msc24x.player

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders

class MiniplayerFragment : Fragment() {

    companion object {
        fun newInstance() = MiniplayerFragment()
    }

    private lateinit var viewModel: MiniplayerViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.miniplayer_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(MiniplayerViewModel::class.java)
        // TODO: Use the ViewModel
    }

}