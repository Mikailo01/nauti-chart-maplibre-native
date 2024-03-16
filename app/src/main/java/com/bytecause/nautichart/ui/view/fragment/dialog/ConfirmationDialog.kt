package com.bytecause.nautichart.ui.view.fragment.dialog

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import com.bytecause.nautichart.databinding.ConfirmationDialogBinding
import com.bytecause.nautichart.domain.model.ArgsObjectTypeArray
import com.bytecause.nautichart.ui.view.delegate.viewBinding

class ConfirmationDialog : DialogFragment() {

    interface ConfirmationDialogListener {
        fun onDialogPositiveClick(dialogId: String, additionalData: Any?)
    }

    private val binding by viewBinding(ConfirmationDialogBinding::inflate)

    companion object {
        private const val ARG_TITLE = "title"
        private const val ARG_DESCRIPTION = "description"
        private const val ARG_ADDITIONAL_DATA = "additionalData"
        private const val ARG_DIALOG_ID = "dialogId"

        private var additionalData: ArgsObjectTypeArray? = null

        fun newInstance(
            title: String,
            description: String?,
            additionalData: ArgsObjectTypeArray?,
            dialogId: String
        ): ConfirmationDialog {
            val fragment = ConfirmationDialog()
            val args = Bundle()
            args.putString(ARG_TITLE, title)
            args.putString(ARG_DESCRIPTION, description)

            Companion.additionalData = additionalData.apply {
                if (this == null) return@apply
                when (additionalData) {
                    is ArgsObjectTypeArray.StringTypeArray -> args.putStringArray(
                        ARG_ADDITIONAL_DATA,
                        additionalData.value
                    )

                    is ArgsObjectTypeArray.IntTypeArray -> args.putIntArray(
                        ARG_ADDITIONAL_DATA,
                        additionalData.value
                    )

                    is ArgsObjectTypeArray.StringType -> args.putString(
                        ARG_ADDITIONAL_DATA,
                        additionalData.value
                    )

                    is ArgsObjectTypeArray.IntType -> args.putInt(
                        ARG_ADDITIONAL_DATA,
                        additionalData.value
                    )

                    else -> return@apply
                }
            }

            args.putString(ARG_DIALOG_ID, dialogId)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        if (dialog != null && dialog?.window != null) {
            dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)
        }

        this.isCancelable = false

        // Set up a FragmentResultListener to receive result.
        val dialogId = requireNotNull(arguments?.getString(ARG_DIALOG_ID))

        val retrievedData = arguments?.let {
            if (it.containsKey(ARG_ADDITIONAL_DATA)) {
                when (additionalData) {
                    is ArgsObjectTypeArray.StringTypeArray -> arguments?.getStringArray(
                        ARG_ADDITIONAL_DATA
                    )
                    is ArgsObjectTypeArray.IntTypeArray -> arguments?.getIntArray(
                        ARG_ADDITIONAL_DATA
                    )
                    is ArgsObjectTypeArray.StringType -> arguments?.getString(
                        ARG_ADDITIONAL_DATA
                    )
                    is ArgsObjectTypeArray.IntType -> arguments?.getInt(
                        ARG_ADDITIONAL_DATA
                    )

                    else -> null
                }
            } else {
                null
            }
        }

        parentFragmentManager.setFragmentResultListener(
            "confirmationResult", this
        ) { _, _ ->
            (parentFragment as? ConfirmationDialogListener)?.onDialogPositiveClick(
                dialogId,
                retrievedData
            )
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val title = arguments?.getString(ARG_TITLE)
        binding.titleText.text = title

        val description = arguments?.getString(ARG_DESCRIPTION)
        binding.textDescription.text = description ?: run {
            binding.textDescription.visibility = View.GONE
            ""
        }

        binding.cancelButton.setOnClickListener {
            this.dismiss()
        }

        binding.confirmButton.setOnClickListener {
            // Confirmation result.
            parentFragmentManager.setFragmentResult(
                "confirmationResult",
                bundleOf()
            )
            this.dismiss()
        }
    }
}