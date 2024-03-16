package com.bytecause.nautichart.ui.view.fragment.dialog

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.DialogFragment
import com.bytecause.nautichart.databinding.LocationAlertDialogBinding
import com.bytecause.nautichart.ui.view.delegate.viewBinding

class LocationDialogFragment : DialogFragment() {

    private val binding by viewBinding(LocationAlertDialogBinding::inflate)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        if (dialog != null && dialog?.window != null) {
            dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.dialogCloseButton.setOnClickListener { _ ->
            dismiss()
        }

        binding.dialogGrantButton.setOnClickListener { _ ->
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            // opens Settings Screen(Activity) as new activity. Otherwise, it will be opened in currently running activity.
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            val uri = Uri.fromParts("package", context?.packageName, null)
            intent.data = uri
            startActivity(intent)
            dismiss()
        }
    }
}