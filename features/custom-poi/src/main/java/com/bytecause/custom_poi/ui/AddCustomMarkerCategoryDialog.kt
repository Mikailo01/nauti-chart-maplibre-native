package com.bytecause.custom_poi.ui

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.bytecause.core.resources.R
import com.bytecause.custom_poi.ui.viewmodel.AddCustomMarkerCategoryViewModel
import com.bytecause.custom_poi.ui.viewmodel.CustomMarkerCategorySharedViewModel
import com.bytecause.data.local.room.tables.CustomPoiCategoryEntity
import com.bytecause.features.custom_poi.databinding.AddCustomMarkerCategoryDialogBinding
import com.bytecause.util.context.showKeyboard
import com.bytecause.util.delegates.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class AddCustomMarkerCategoryDialog :
    DialogFragment() {

    private val binding by viewBinding(
        AddCustomMarkerCategoryDialogBinding::inflate
    )

    private val viewModel: AddCustomMarkerCategoryViewModel by viewModels()
    private val sharedViewModel: CustomMarkerCategorySharedViewModel by activityViewModels()

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

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.apply {
            val contentColor =
                ContextCompat.getColor(requireContext(), R.color.md_theme_onPrimaryContainer)

            toolbarAppBarLayout.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.md_theme_primaryContainer
                )
            )
            navBack.apply {
                setColorFilter(contentColor)
                setOnClickListener {
                    sharedViewModel.resetDrawableId()
                    findNavController().popBackStack()
                }
            }
            destNameTextView.apply {
                text = getString(R.string.new_category)
                setTextColor(contentColor)
            }
            toolbarDivider.visibility = View.GONE
        }

        binding.addIconImageButton.setOnClickListener {
            findNavController().navigate(com.bytecause.features.custom_poi.R.id.action_addCustomMarkerCategoryDialog_to_selectPoiMarkerIconDialog)
        }

        binding.categoryNameEdittextInput.let {
            requireContext().showKeyboard(it)
        }

        binding.categoryNameEdittextInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {
            }

            override fun onTextChanged(
                s: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {
            }

            override fun afterTextChanged(s: Editable?) {
                checkInput(s)
            }
        })

        binding.saveButton.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                when (isUserInputValid()) {
                    true -> {
                        val resourceName =
                            resources.getResourceEntryName(binding.addIconImageButton.tag as Int)

                        val newCategory = CustomPoiCategoryEntity(
                            categoryName = binding.categoryNameEdittextInput.text.toString(),
                            drawableResourceName = resourceName
                        )

                        launch {
                            viewModel.insertCategory(newCategory)
                        }.invokeOnCompletion {
                            sharedViewModel.resetDrawableId()
                            findNavController().popBackStack()
                        }
                    }

                    false -> binding.categoryNameEdittextLayout.error =
                        getString(R.string.category_present)

                    else -> {
                        if (binding.categoryNameEdittextLayout.error == null) {
                            binding.addIconImageButton.performClick()
                            Toast.makeText(
                                requireContext(),
                                getString(R.string.choose_icon),
                                Toast.LENGTH_SHORT
                            )
                                .show()
                        }
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                sharedViewModel.drawableIdStateFlow.collect {
                    if (it == -1) return@collect

                    binding.addIconImageButton.apply {
                        setImageDrawable(
                            ContextCompat.getDrawable(
                                requireContext(),
                                it
                            )
                        )
                        tag = it
                        binding.addIconTextView.text = getString(R.string.change_icon)
                    }
                }
            }
        }
    }

    private fun checkInput(s: CharSequence?) {
        binding.categoryNameEdittextLayout.error = if (s.isNullOrEmpty()) {
            getString(R.string.missing_field)
        } else null
    }

    private suspend fun isUserInputValid(): Boolean? = withContext(Dispatchers.Main) {
        val categoryName = binding.categoryNameEdittextInput.text.toString()
        checkInput(categoryName)

        if (binding.categoryNameEdittextLayout.error == null && binding.addIconImageButton.tag != null) {
            return@withContext viewModel.isCategoryNamePresent(categoryName).firstOrNull() != true
        } else return@withContext null
    }
}