package com.msc24x.player

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat

class SettingsFragment : PreferenceFragmentCompat() {


    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)
        requireActivity().window.navigationBarColor = resources.getColor(R.color.colorPrimary)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
            requireActivity().window.navigationBarDividerColor =
                resources.getColor(R.color.colorPrimary)
        }

    }
}