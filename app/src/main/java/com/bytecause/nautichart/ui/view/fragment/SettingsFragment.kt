package com.bytecause.nautichart.ui.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bytecause.nautichart.R
import com.bytecause.nautichart.databinding.SettingsFragmentBinding
import com.bytecause.nautichart.ui.view.delegate.viewBinding

class SettingsFragment: Fragment(R.layout.settings_fragment) {

    private val binding by viewBinding(SettingsFragmentBinding::bind)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }
}