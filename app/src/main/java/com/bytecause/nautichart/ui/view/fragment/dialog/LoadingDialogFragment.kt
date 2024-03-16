package com.bytecause.nautichart.ui.view.fragment.dialog

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.navArgs
import com.bytecause.nautichart.databinding.LoadingDialogFragmentBinding
import com.bytecause.nautichart.ui.view.delegate.viewBinding

class LoadingDialogFragment : DialogFragment() {

    private val binding by viewBinding(LoadingDialogFragmentBinding::inflate)

    private val args: LoadingDialogFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        this.isCancelable = false

        if (dialog != null && dialog?.window != null) {
            dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        args.textInfo?.let {
            binding.loadingDialogText.text = it
        } ?: run { binding.loadingDialogText.visibility = View.GONE }
    }
}