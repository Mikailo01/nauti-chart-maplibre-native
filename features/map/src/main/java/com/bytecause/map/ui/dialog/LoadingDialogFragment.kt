package com.bytecause.map.ui.dialog

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.navArgs
import com.bytecause.core.resources.R
import com.bytecause.feature.map.databinding.LoadingDialogFragmentBinding
import com.bytecause.map.ui.viewmodel.LoadingDialogSharedViewModel
import com.bytecause.util.delegates.viewBinding
import kotlinx.coroutines.launch

class LoadingDialogFragment : DialogFragment() {

    private val binding by viewBinding(LoadingDialogFragmentBinding::inflate)

    private val args: LoadingDialogFragmentArgs by navArgs<LoadingDialogFragmentArgs>()

    private val sharedViewModel: LoadingDialogSharedViewModel by activityViewModels()

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

        // collect progress values from shared viewmodel and update progress text view
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                sharedViewModel.progressSharedFlow.collect { progress ->

                    binding.progressTextView.apply {
                        if (visibility == View.GONE) visibility = View.VISIBLE
                        text = getString(R.string.processed_count).format(progress)
                    }

                    binding.loadingDialogText.apply {
                        val textValue = getString(R.string.processing)
                        if (text != textValue) text = textValue
                    }
                }
            }
        }
    }
}