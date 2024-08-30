package com.bytecause.custom_poi.ui

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bytecause.custom_poi.ui.viewmodel.CustomMarkerViewModel
import com.bytecause.data.local.room.tables.CustomPoiCategoryEntity
import com.bytecause.data.local.room.tables.CustomPoiEntity
import com.bytecause.domain.model.ArgsObjectTypeArray
import com.bytecause.features.custom_poi.R
import com.bytecause.features.custom_poi.databinding.AddCustomMarkerBinding
import com.bytecause.presentation.components.views.dialog.ConfirmationDialog
import com.bytecause.presentation.components.views.recyclerview.FullyExpandedRecyclerView
import com.bytecause.presentation.components.views.recyclerview.adapter.GenericRecyclerViewAdapter
import com.bytecause.presentation.viewmodels.MapSharedViewModel
import com.bytecause.util.delegates.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

sealed interface ItemType {
    data class Category(val category: CustomPoiCategoryEntity) : ItemType
    data object AddButton : ItemType
}

private const val ANIMATION_MIN_SCALE = 0.6f
private const val ANIMATION_MAX_SCALE = 1f
private const val ADD_BUTTON_TAG = "add_button"

@AndroidEntryPoint
class CustomMarkerDialog :
    DialogFragment(),
    ConfirmationDialog.ConfirmationDialogListener {
    private val binding by viewBinding(AddCustomMarkerBinding::inflate)

    private val viewModel: CustomMarkerViewModel by viewModels()
    private val mapSharedViewModel: MapSharedViewModel by activityViewModels()

    private lateinit var categoryRecyclerView: FullyExpandedRecyclerView
    private lateinit var categoryGenericRecyclerViewAdapter: GenericRecyclerViewAdapter<ItemType>

    private lateinit var poiRecyclerView: FullyExpandedRecyclerView
    private lateinit var poiGenericRecyclerViewAdapter: GenericRecyclerViewAdapter<CustomPoiEntity>

    private var scaleAnimator: AnimatorSet? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        if (dialog != null && dialog?.window != null) {
            dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)
        }

        val gridLayoutManager =
            object : GridLayoutManager(requireContext(), 5) {
                override fun canScrollVertically(): Boolean {
                    return false
                }
            }

        @SuppressLint("ResourceType")
        val categoryBindingInterface =
            object :
                com.bytecause.util.bindings.RecyclerViewBindingInterface<ItemType> {
                override fun bindData(
                    item: ItemType,
                    itemView: View,
                    itemPosition: Int,
                ) {
                    itemView.findViewById<TextView?>(R.id.categoryName).apply {
                        (item as? ItemType.Category)?.run {
                            text = category.categoryName
                            itemView.tag = category.drawableResourceName
                        } ?: (item as ItemType.AddButton).run {
                            text = getString(com.bytecause.core.resources.R.string.add_custom)
                            itemView.tag = ADD_BUTTON_TAG
                        }

                        setCompoundDrawablesWithIntrinsicBounds(
                            null,
                            (item as? ItemType.Category)?.run {
                                ResourcesCompat.getDrawable(
                                    resources,
                                    resources.getIdentifier(
                                        category.drawableResourceName,
                                        "drawable",
                                        requireActivity().packageName,
                                    ),
                                    null
                                )
                            } ?: ContextCompat.getDrawable(
                                requireContext(),
                                com.bytecause.core.resources.R.drawable.baseline_dashboard_customize_24
                            ),
                            null,
                            null,
                        )
                    }
                    itemView.apply {
                        setOnClickListener {
                            if (!com.bytecause.util.common.LastClick()
                                    .lastClick(300)
                            ) return@setOnClickListener

                            // Checks if the user clicked on add new category button
                            if (categoryRecyclerView.getChildAt(itemPosition).tag == ADD_BUTTON_TAG
                            ) {
                                // Reset click position
                                viewModel.setSelectedItemViewPosition(RecyclerView.NO_POSITION)
                                resetClickedButtonState()

                                navigateToAddCategoryDialog()

                                viewModel.clearMarkerList()
                                return@setOnClickListener
                            }

                            resetClickedButtonState()

                            // Store the selected position for future reference
                            viewModel.setSelectedItemViewPosition(itemPosition)

                            // Starts animation after item view click
                            animateClick(itemView)

                            // Gets markers for selected category
                            getPoiMarkers(
                                itemView.findViewById<TextView>(
                                    R.id.categoryName,
                                ).text.toString()
                            )
                        }

                        setOnLongClickListener {
                            if (itemPosition > 2 && categoryRecyclerView.getChildAt(
                                    itemPosition,
                                ).tag != ADD_BUTTON_TAG
                            ) {
                                cancelCategoryViewsAnimation()

                                val confirmationDialog =
                                    ConfirmationDialog.newInstance(
                                        resources.getString(com.bytecause.core.resources.R.string.delete_category_title),
                                        resources.getString(com.bytecause.core.resources.R.string.delete_category_description),
                                        ArgsObjectTypeArray.IntType(itemPosition),
                                        "delete_poi_category",
                                    )
                                confirmationDialog.show(
                                    childFragmentManager,
                                    confirmationDialog.tag,
                                )
                            }
                            return@setOnLongClickListener true
                        }
                    }
                }
            }

        @SuppressLint("ResourceType")
        val poiBindingInterface =
            object : com.bytecause.util.bindings.RecyclerViewBindingInterface<CustomPoiEntity> {
                override fun bindData(
                    item: CustomPoiEntity,
                    itemView: View,
                    itemPosition: Int,
                ) {
                    val removeCustomPoi: ImageButton = itemView.findViewById(R.id.remove_custom_poi)
                    val poiIconImageView: ImageView = itemView.findViewById(R.id.poi_icon)
                    itemView.findViewById<TextView?>(R.id.poi_name).apply {
                        text = item.poiName
                        tag = item.poiId
                    }
                    poiIconImageView.setImageResource(
                        resources.getIdentifier(
                            item.drawableResourceName,
                            "drawable",
                            requireActivity().packageName,
                        ),
                    )
                    removeCustomPoi.setOnClickListener {
                        val confirmationDialog =
                            ConfirmationDialog.newInstance(
                                resources.getString(com.bytecause.core.resources.R.string.delete_poi_marker_title),
                                null,
                                ArgsObjectTypeArray.IntType(itemPosition),
                                "delete_poi_marker",
                            )
                        confirmationDialog.show(childFragmentManager, confirmationDialog.tag)
                    }
                }
            }

        categoryRecyclerView = binding.categoryRecyclerView
        categoryRecyclerView.layoutManager = gridLayoutManager

        poiRecyclerView = binding.poiListRecyclerView
        poiRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        viewModel.setSelectedItemViewPosition(0)

        // Initialize and set the adapter
        categoryGenericRecyclerViewAdapter =
            GenericRecyclerViewAdapter(
                emptyList(),
                R.layout.recyclerview_marker_category,
                categoryBindingInterface,
            )
        categoryRecyclerView.adapter = categoryGenericRecyclerViewAdapter

        poiGenericRecyclerViewAdapter =
            GenericRecyclerViewAdapter(
                emptyList(),
                R.layout.recyclerview_poi_list_layout,
                poiBindingInterface,
            )
        poiRecyclerView.adapter = poiGenericRecyclerViewAdapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.poiCategoriesFlow.collect { categoryList ->
                    cancelCategoryViewsAnimation()

                    // Reset clicked state if category has been removed
                    if (categoryList.size < categoryGenericRecyclerViewAdapter.itemCount) resetClickedButtonState()

                    categoryGenericRecyclerViewAdapter.updateContent(categoryList)
                    postAndNotifyAdapter(Handler(Looper.getMainLooper()), categoryRecyclerView)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.poiMarkersFlow.collect { markerList ->

                    binding.emptyListTextView.visibility =
                        if (markerList.isEmpty()) View.VISIBLE else View.GONE

                    poiGenericRecyclerViewAdapter.updateContent(markerList)
                }
            }
        }

        // Visibility controller for custom markers card view
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.selectedItemViewPosition.collect { position ->
                    when (position) {
                        RecyclerView.NO_POSITION -> {
                            binding.customPoiCardView.visibility =
                                View.GONE
                        }

                        else -> binding.customPoiCardView.apply {
                            visibility =
                                if (visibility == View.VISIBLE) return@apply else View.VISIBLE
                        }
                    }
                }
            }
        }

        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        binding.markerNameInput.addTextChangedListener(
            object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int,
                ) {
                }

                override fun onTextChanged(
                    s: CharSequence?,
                    start: Int,
                    before: Int,
                    count: Int,
                ) {
                }

                override fun afterTextChanged(s: Editable?) {
                    showErrorWhenEmpty()
                }
            },
        )

        binding.markerDescriptionLayout.visibility =
            when {
                viewModel.isMarkerDescriptionVisible -> View.VISIBLE
                else -> View.GONE
            }

        binding.toolbar.apply {
            this.toolbarAppBarLayout
            navBack.setOnClickListener { findNavController().popBackStack() }
            destNameTextView.text = getString(com.bytecause.core.resources.R.string.add_marker)
        }

        binding.saveButton.setOnClickListener {
            binding.markerNameInput.text?.let {
                if (it.isNotBlank() && viewModel.selectedItemViewPosition.value != RecyclerView.NO_POSITION) {
                    val categoryNameTextView =
                        categoryRecyclerView.layoutManager?.findViewByPosition(viewModel.selectedItemViewPosition.value)
                            ?.findViewById<TextView>(R.id.categoryName)

                    viewModel.insertCustomPoi(
                        CustomPoiEntity(
                            poiName = binding.markerNameInput.text.toString(),
                            latitude = mapSharedViewModel.latLngFlow.value?.latitude ?: 0.0,
                            longitude = mapSharedViewModel.latLngFlow.value?.longitude ?: 0.0,
                            description =
                            when (!binding.markerDescriptionInput.text.isNullOrEmpty()) {
                                true -> binding.markerDescriptionInput.text.toString()
                                else -> ""
                            },
                            categoryName = categoryNameTextView?.text?.toString() ?: "Favorite",
                            drawableResourceName = categoryRecyclerView.getChildAt(viewModel.selectedItemViewPosition.value).tag as? String
                                ?: resources.getResourceEntryName(com.bytecause.core.resources.R.drawable.baseline_favorite_24),
                        )
                    )

                    dismiss()
                } else {
                    showErrorWhenEmpty()
                    if (viewModel.selectedItemViewPosition.value == RecyclerView.NO_POSITION) animateCategoryViews()
                }
            }
        }

        mapSharedViewModel.latLngFlow.value?.let { latLng ->
            binding.coordinatesTextview.text =
                resources.getString(com.bytecause.core.resources.R.string.split_two_strings_formatter)
                    .format(
                        com.bytecause.util.map.MapUtil.latitudeToDMS(latLng.latitude),
                        com.bytecause.util.map.MapUtil.longitudeToDMS(latLng.longitude),
                    )
        }

        binding.expandDescriptionEdittext.setOnClickListener {
            when {
                binding.markerDescriptionLayout.visibility != View.VISIBLE -> {
                    binding.markerDescriptionLayout.visibility = View.VISIBLE
                    viewModel.isMarkerDescriptionVisible(true)
                    binding.addDescriptionTextview.text =
                        resources.getString(com.bytecause.core.resources.R.string.remove_description)
                    binding.addDescriptionImageview.setImageDrawable(
                        ContextCompat.getDrawable(
                            requireContext(),
                            com.bytecause.core.resources.R.drawable.baseline_remove_circle_outline_24,
                        ),
                    )
                }

                else -> {
                    binding.markerDescriptionLayout.visibility = View.GONE
                    viewModel.isMarkerDescriptionVisible(false)
                    binding.markerDescriptionInput.setText("")
                    binding.addDescriptionTextview.text =
                        resources.getString(com.bytecause.core.resources.R.string.add_description)
                    binding.addDescriptionImageview.setImageDrawable(
                        ContextCompat.getDrawable(
                            requireContext(),
                            com.bytecause.core.resources.R.drawable.baseline_description_24,
                        ),
                    )
                }
            }
        }
    }

    private fun getPoiMarkers(categoryName: String) {
        viewModel.getCategoryWithPois(categoryName)
    }

    private fun navigateToAddCategoryDialog() {
        findNavController().navigate(R.id.action_customMarkerDialog_to_addCustomMarkerCategoryDialog)
    }

    // Recursive function which will invoke performClick() on first itemView in recyclerView after layout completion.
    private fun postAndNotifyAdapter(
        handler: Handler,
        recyclerView: RecyclerView,
    ) {
        if (findNavController().currentDestination?.id == R.id.addCustomMarkerCategoryDialog) return

        handler.post {
            if (!recyclerView.isComputingLayout) {
                (
                        recyclerView.findViewHolderForLayoutPosition(
                            when (viewModel.selectedItemViewPosition.value) {
                                categoryRecyclerView.childCount - 1 -> 0
                                else -> viewModel.selectedItemViewPosition.value
                            },
                        ) as GenericRecyclerViewAdapter.ViewHolder?
                        )?.itemView?.performClick()
            } else {
                postAndNotifyAdapter(handler, recyclerView)
            }
        }
    }

    private fun animateClick(view: View) {
        if (scaleAnimator != null) cancelCategoryViewsAnimation()

        val animDuration: Long = 200

        val scaleX =
            ObjectAnimator.ofFloat(view, View.SCALE_X, ANIMATION_MIN_SCALE, ANIMATION_MAX_SCALE)
        val scaleY =
            ObjectAnimator.ofFloat(view, View.SCALE_Y, ANIMATION_MIN_SCALE, ANIMATION_MAX_SCALE)
        scaleX.duration = animDuration
        scaleY.duration = animDuration

        val animatorSet = AnimatorSet()
        animatorSet.playTogether(scaleX, scaleY)

        ContextCompat.getDrawable(
            requireContext(),
            com.bytecause.core.resources.R.drawable.rounded_background
        )?.apply {
            setTint(
                ContextCompat.getColor(
                    requireContext(),
                    com.bytecause.core.resources.R.color.button_state_color
                )
            )
            view.background = this
        }
        animatorSet.start()
    }

    private fun showErrorWhenEmpty() {
        if (findNavController().currentDestination?.id == R.id.addCustomMarkerCategoryDialog) return

        binding.markerNameLayout.let {
            if (binding.markerNameInput.text.isNullOrEmpty()) {
                it.error = getString(com.bytecause.core.resources.R.string.type_name)
            } else {
                it.error = null
            }
        }
    }

    // Resets background drawable for all item views.
    private fun resetClickedButtonState() {
        for (x in 0..<categoryRecyclerView.childCount) {
            categoryRecyclerView.getChildViewHolder(categoryRecyclerView.getChildAt(x)).itemView.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    com.bytecause.core.resources.R.color.transparent
                ),
            )
        }
        viewModel.setSelectedItemViewPosition(RecyclerView.NO_POSITION)
    }

    private fun cancelCategoryViewsAnimation() {
        scaleAnimator?.removeAllListeners()
        scaleAnimator?.cancel()
        scaleAnimator = null


        for (x in 0 until categoryRecyclerView.childCount) {
            categoryRecyclerView.getChildAt(x).apply {
                scaleX = ANIMATION_MAX_SCALE
                scaleY = ANIMATION_MAX_SCALE
            }
        }
    }

    private fun animateCategoryViews() {
        if (scaleAnimator != null) return

        scaleAnimator = AnimatorSet()

        for (x in 0 until categoryRecyclerView.childCount) {
            val view = categoryRecyclerView.getChildAt(x)

            if (view.tag == ADD_BUTTON_TAG) return

            val scaleUpX =
                ObjectAnimator.ofFloat(view, View.SCALE_X, ANIMATION_MIN_SCALE, ANIMATION_MAX_SCALE)
            val scaleUpY =
                ObjectAnimator.ofFloat(view, View.SCALE_Y, ANIMATION_MIN_SCALE, ANIMATION_MAX_SCALE)
            val scaleDownX =
                ObjectAnimator.ofFloat(view, View.SCALE_X, ANIMATION_MAX_SCALE, ANIMATION_MIN_SCALE)
            val scaleDownY =
                ObjectAnimator.ofFloat(view, View.SCALE_Y, ANIMATION_MAX_SCALE, ANIMATION_MIN_SCALE)

            scaleUpX.duration = 500
            scaleUpY.duration = 500
            scaleDownX.duration = 500
            scaleDownY.duration = 500

            val scaleUp = AnimatorSet().apply {
                playTogether(scaleUpX, scaleUpY)
            }

            val scaleDown = AnimatorSet().apply {
                playTogether(scaleDownX, scaleDownY)
            }

            scaleAnimator?.apply {
                playSequentially(scaleUp, scaleDown)
                addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        // Restart the animation when it ends
                        animation.start()
                    }
                })
            }

            scaleAnimator?.start()
        }
    }

    override fun onDialogPositiveClick(
        dialogId: String,
        additionalData: Any?,
    ) {
        (additionalData as? ArgsObjectTypeArray.IntType)?.value?.let { index ->
            when (dialogId) {
                "delete_poi_category" -> viewModel.removePoiCategory(index)
                "delete_poi_marker" -> {
                    val markerId = poiRecyclerView.layoutManager?.findViewByPosition(index)
                        ?.findViewById<TextView>(R.id.poi_name)?.tag as Long

                    viewModel.removePoiMarker(markerId, index)
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()

        // Apply the fullscreen dialog style
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT,
        )
        dialog?.window?.setBackgroundDrawable(
            ColorDrawable(
                ContextCompat.getColor(
                    requireContext(),
                    com.bytecause.core.resources.R.color.md_theme_primaryContainer,
                ),
            ),
        )
    }
}