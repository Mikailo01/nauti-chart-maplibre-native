package com.bytecause.nautichart.ui.view.fragment.dialog

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.bytecause.nautichart.databinding.CustomTileSourceDialogBinding
import com.bytecause.nautichart.ui.view.delegate.viewBinding

class CustomTileSourceDialog : DialogFragment() {

    private val binding by viewBinding(CustomTileSourceDialogBinding::inflate)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        if (dialog != null && dialog?.window != null) {
            dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.setZoomLevelRangeLayout.setOnClickListener {
            // TODO("Finish implementation.")
        }

        binding.dismissButton.setOnClickListener {
            this.dismiss()
        }

        binding.confirmButton.setOnClickListener {
            Toast.makeText(requireContext(), "Confirmed!", Toast.LENGTH_SHORT).show()
        }
    }
}