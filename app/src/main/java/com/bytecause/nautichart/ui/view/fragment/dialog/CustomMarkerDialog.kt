package com.bytecause.nautichart.ui.view.fragment.dialog

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
import android.util.Log
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
import com.bytecause.nautichart.R
import com.bytecause.nautichart.data.local.room.tables.CustomPoiCategoryEntity
import com.bytecause.nautichart.data.local.room.tables.CustomPoiEntity
import com.bytecause.nautichart.databinding.AddCustomMarkerBinding
import com.bytecause.nautichart.ui.adapter.GenericRecyclerViewAdapter
import com.bytecause.nautichart.ui.adapter.RecyclerViewBindingInterface
import com.bytecause.nautichart.ui.view.custom.FullyExpandedRecyclerView
import com.bytecause.nautichart.ui.view.delegate.viewBinding
import com.bytecause.nautichart.ui.viewmodels.CustomMarkerViewModel
import com.bytecause.nautichart.ui.viewmodels.MapSharedViewModel
import com.bytecause.nautichart.util.MapUtil
import com.bytecause.nautichart.util.TAG
import com.bytecause.nautichart.util.Util
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlin.properties.Delegates


// TODO("Refactor")
@AndroidEntryPoint
class CustomMarkerDialog : DialogFragment(),
    ConfirmationDialog.ConfirmationDialogListener {

    private val binding by viewBinding(AddCustomMarkerBinding::inflate)

    private val mapSharedViewModel: MapSharedViewModel by activityViewModels()
    private val viewModel: CustomMarkerViewModel by viewModels()

    private lateinit var categoryRecyclerView: FullyExpandedRecyclerView
    private lateinit var categoryGenericRecyclerViewAdapter: GenericRecyclerViewAdapter<CustomPoiCategoryEntity>

    private lateinit var poiRecyclerView: FullyExpandedRecyclerView
    private lateinit var poiGenericRecyclerViewAdapter: GenericRecyclerViewAdapter<CustomPoiEntity>

    private val poiCategoriesList = mutableListOf<CustomPoiCategoryEntity>()
    private val poiList = mutableListOf<CustomPoiEntity>()

    private var categoryAddButton by Delegates.notNull<CustomPoiCategoryEntity>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        if (dialog != null && dialog?.window != null) {
            dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)
        }

        categoryAddButton = CustomPoiCategoryEntity(
            categoryName = "Add custom",
            drawableResourceName = resources.getResourceEntryName(R.drawable.baseline_dashboard_customize_24)
        )

        val gridLayoutManager = object : GridLayoutManager(requireContext(), 5) {
            override fun canScrollVertically(): Boolean {
                return false
            }
        }

        @SuppressLint("ResourceType")
        val categoryBindingInterface =
            object : RecyclerViewBindingInterface<CustomPoiCategoryEntity> {
                override fun bindData(
                    item: CustomPoiCategoryEntity,
                    itemView: View,
                    itemPosition: Int
                ) {
                    itemView.findViewById<TextView?>(R.id.categoryName).apply {
                        text = item.categoryName
                        tag = item.drawableResourceName

                        setCompoundDrawablesWithIntrinsicBounds(
                            null,
                            ResourcesCompat.getDrawable(
                                resources,
                                resources.getIdentifier(
                                    item.drawableResourceName,
                                    "drawable",
                                    requireActivity().packageName
                                ),
                                null
                            ),
                            null,
                            null
                        )
                    }
                    itemView.apply {
                        setOnClickListener {
                            if (!Util().lastClick(300)) return@setOnClickListener

                            if (itemPosition == categoryGenericRecyclerViewAdapter.itemCount - 1 && categoryRecyclerView.getChildAt(
                                    itemPosition
                                )
                                    .findViewById<TextView>(R.id.categoryName).text == categoryAddButton.categoryName
                            ) {
                                viewModel.setItemViewPosition(RecyclerView.NO_POSITION)
                                resetClickedButtonState()

                                //findNavController().navigate(R.id.action_customMarkerDialog_to_addCustomMarkerCategoryDialog)
                                navigate()
                                //poiList.clear()
                                updatePoiList(poiList)
                                return@setOnClickListener
                            }

                            resetClickedButtonState()

                            // Store the selected position for future reference
                            viewModel.setItemViewPosition(itemPosition)

                            animateClick(itemView)

                            lifecycleScope.launch {
                                val categoryName = itemView.findViewById<TextView>(
                                    R.id.categoryName
                                ).text.toString()
                                viewModel.getCategoryWithPois(
                                    categoryName
                                )
                                    .firstOrNull()?.let {
                                        updatePoiList(it.pois)
                                    }

                                /*viewModel.getPoisByCategoryName(
                                    itemView.findViewById<TextView>(
                                        R.id.categoryName
                                    ).text.toString()
                                )
                                    .firstOrNull().let {
                                        updatePoiList(it)
                                    }*/
                            }
                        }
                        setOnLongClickListener {
                            if (itemPosition > 2 && categoryRecyclerView.getChildAt(
                                    itemPosition
                                )
                                    .findViewById<TextView>(R.id.categoryName).text != categoryAddButton.categoryName
                            ) {
                                viewModel.setRemoveIndex(itemPosition)
                                val confirmationDialog = ConfirmationDialog.newInstance(
                                    resources.getString(R.string.delete_category_title),
                                    resources.getString(R.string.delete_category_description),
                                    null,
                                    "delete_poi_category"
                                )
                                confirmationDialog.show(
                                    childFragmentManager,
                                    confirmationDialog.tag
                                )
                            }
                            return@setOnLongClickListener true
                        }
                    }
                }
            }

        @SuppressLint("ResourceType")
        val poiBindingInterface = object : RecyclerViewBindingInterface<CustomPoiEntity> {
            override fun bindData(item: CustomPoiEntity, itemView: View, itemPosition: Int) {
                val removeCustomPoi: ImageButton = itemView.findViewById(R.id.remove_custom_poi)
                val poiIconImageView: ImageView = itemView.findViewById(R.id.poi_icon)
                itemView.findViewById<TextView?>(R.id.poi_name).apply {
                    text = item.poiName
                    tag = item.poiId //item.categoryName
                }
                poiIconImageView.setImageResource(
                    resources.getIdentifier(
                        item.drawableResourceName,
                        "drawable",
                        requireActivity().packageName
                    )
                )
                removeCustomPoi.setOnClickListener {
                    viewModel.setRemoveIndex(itemPosition)
                    val confirmationDialog = ConfirmationDialog.newInstance(
                        resources.getString(R.string.delete_poi_marker_title),
                        null,
                        null,
                        "delete_poi_marker"
                    )
                    confirmationDialog.show(childFragmentManager, confirmationDialog.tag)
                }
            }

        }

        categoryRecyclerView = binding.categoryRecyclerView
        categoryRecyclerView.layoutManager = gridLayoutManager

        poiRecyclerView = binding.poiListRecyclerView
        poiRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        viewModel.setItemViewPosition(0)

        // Initialize and set the adapter
        categoryGenericRecyclerViewAdapter = GenericRecyclerViewAdapter(
            poiCategoriesList,
            R.layout.recyclerview_marker_category,
            categoryBindingInterface
        )
        categoryRecyclerView.adapter = categoryGenericRecyclerViewAdapter

        poiGenericRecyclerViewAdapter =
            GenericRecyclerViewAdapter(
                poiList,
                R.layout.recyclerview_poi_list_layout,
                poiBindingInterface
            )
        poiRecyclerView.adapter = poiGenericRecyclerViewAdapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.getAllCategories.collect {
                    if (it.isEmpty()) {
                        Log.d(TAG(), "first run")

                        // ID's aren't static, so drawable name has to be used instead.
                        val initCategories = listOf(
                            CustomPoiCategoryEntity(
                                categoryName = "Favorite",
                                drawableResourceName = resources.getResourceEntryName(R.drawable.baseline_favorite_24)
                            ),
                            CustomPoiCategoryEntity(
                                categoryName = "Travel plans",
                                drawableResourceName = resources.getResourceEntryName(R.drawable.baseline_mode_of_travel_24)
                            ),
                            CustomPoiCategoryEntity(
                                categoryName = "Nautical POI",
                                drawableResourceName = resources.getResourceEntryName(R.drawable.nautical_poi_icon)
                            )
                        )
                        for (x in initCategories) {
                            viewModel.insertCategory(x)
                        }
                        categoryGenericRecyclerViewAdapter.notifyItemRangeChanged(
                            0,
                            poiCategoriesList.size
                        )
                        postAndNotifyAdapter(Handler(Looper.getMainLooper()), categoryRecyclerView)
                    } else if (poiCategoriesList.isNotEmpty()) {
                        Log.d(TAG(), "poi list not empty")
                        if (poiCategoriesList.size <= 20) {
                            poiCategoriesList.let { list ->
                                list.removeLast()
                                list.addAll(
                                    it.filter {
                                        poiCategoriesList.none { presentCategory ->
                                            presentCategory == it
                                        }
                                    }
                                )
                                list.add(categoryAddButton)
                            }
                            categoryGenericRecyclerViewAdapter.notifyItemRangeChanged(
                                3,
                                poiCategoriesList.size - 3
                            )
                        }
                        if (poiCategoriesList.size > 20) {
                            poiCategoriesList.removeLast()
                            categoryGenericRecyclerViewAdapter.notifyItemRangeRemoved(
                                poiCategoriesList.size,
                                1
                            )
                        }
                    } else {
                        Log.d(TAG(), "second run")
                        for (x in it) {
                            poiCategoriesList.add(x)
                        }
                        if (poiCategoriesList.size < 20) poiCategoriesList.add(categoryAddButton)
                        categoryGenericRecyclerViewAdapter.notifyItemRangeChanged(
                            0,
                            poiCategoriesList.size
                        )
                        postAndNotifyAdapter(Handler(Looper.getMainLooper()), categoryRecyclerView)
                    }
                }
            }
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.markerNameInput.addTextChangedListener(object : TextWatcher {
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
                showErrorWhenEmpty()
            }
        })

        binding.markerDescriptionLayout.visibility = when {
            viewModel.isMarkerDescriptionVisible -> View.VISIBLE
            else -> View.GONE
        }

        binding.toolbar.apply {
            navBack.setOnClickListener {
                findNavController().popBackStack()
            }
            destNameTextView.text = getString(R.string.add_marker)
        }

        binding.saveButton.setOnClickListener {
            requireNotNull(binding.markerNameInput.text).let {
                if (it.isNotEmpty() && viewModel.itemViewPosition != RecyclerView.NO_POSITION) {
                    val categoryNameTextView =
                        categoryRecyclerView.layoutManager?.findViewByPosition(viewModel.itemViewPosition)
                            ?.findViewById<TextView>(R.id.categoryName)
                    viewModel.insertCustomPoi(
                        CustomPoiEntity(
                            poiName = binding.markerNameInput.text.toString(),
                            latitude = mapSharedViewModel.geoPoint?.latitude ?: 0.0,
                            longitude = mapSharedViewModel.geoPoint?.longitude ?: 0.0,
                            description = when (!binding.markerDescriptionInput.text.isNullOrEmpty()) {
                                true -> binding.markerDescriptionInput.text.toString()
                                else -> ""
                            },
                            categoryName = categoryNameTextView?.text?.toString() ?: "Favorite",
                            drawableResourceName = categoryNameTextView?.tag as? String
                                ?: resources.getResourceEntryName(R.drawable.baseline_favorite_24)
                        )
                    )
                    this.dismiss()
                } else showErrorWhenEmpty()
            }
        }

        mapSharedViewModel.geoPoint?.let { geoPoint ->
            binding.coordinatesTextview.text =
                resources.getString(R.string.split_two_strings_formatter).format(
                    MapUtil.latitudeToDMS(geoPoint.latitude),
                    MapUtil.longitudeToDMS(geoPoint.longitude)
                )
        }

        binding.expandDescriptionEdittext.setOnClickListener {
            when {
                binding.markerDescriptionLayout.visibility != View.VISIBLE -> {
                    binding.markerDescriptionLayout.visibility = View.VISIBLE
                    viewModel.isMarkerDescriptionVisible(true)
                    binding.addDescriptionTextview.text =
                        resources.getString(R.string.remove_description)
                    binding.addDescriptionImageview.setImageDrawable(
                        ContextCompat.getDrawable(
                            requireContext(),
                            R.drawable.baseline_remove_circle_outline_24
                        )
                    )
                }

                else -> {
                    binding.markerDescriptionLayout.visibility = View.GONE
                    viewModel.isMarkerDescriptionVisible(false)
                    binding.markerDescriptionInput.setText("")
                    binding.addDescriptionTextview.text =
                        resources.getString(R.string.add_description)
                    binding.addDescriptionImageview.setImageDrawable(
                        ContextCompat.getDrawable(
                            requireContext(),
                            R.drawable.baseline_description_24
                        )
                    )
                }
            }
        }
    }

    private fun navigate() {
        findNavController().navigate(R.id.action_customMarkerDialog_to_addCustomMarkerCategoryDialog)
    }

    // Recursive function which will invoke performClick() on first itemView in recyclerView after layout completion.
    private fun postAndNotifyAdapter(handler: Handler, recyclerView: RecyclerView) {
        if (findNavController().currentDestination?.id == R.id.addCustomMarkerCategoryDialog) return
        // if (sharedViewModel.isAddCategoryDialogShowing) return
        handler.post {
            if (!recyclerView.isComputingLayout) {
                (recyclerView.findViewHolderForLayoutPosition(
                    when (viewModel.itemViewPosition) {
                        categoryRecyclerView.childCount - 1 -> 0
                        else -> viewModel.itemViewPosition
                    }
                ) as GenericRecyclerViewAdapter.ViewHolder?)?.itemView?.performClick()
            } else {
                postAndNotifyAdapter(handler, recyclerView)
            }
        }
    }

    private fun animateClick(view: View) {
        val animDuration: Long = 200

        val scaleX = ObjectAnimator.ofFloat(view, View.SCALE_X, 0.6f, 1f)
        val scaleY = ObjectAnimator.ofFloat(view, View.SCALE_Y, 0.6f, 1f)
        scaleX.duration = animDuration
        scaleY.duration = animDuration

        val animatorSet = AnimatorSet()
        animatorSet.playTogether(scaleX, scaleY)

        ContextCompat.getDrawable(requireContext(), R.drawable.rounded_background)?.apply {
            setTint(ContextCompat.getColor(requireContext(), R.color.button_state_color))
            view.background = this
        }
        animatorSet.start()
    }

    private fun showErrorWhenEmpty() {
        if (findNavController().currentDestination?.id == R.id.addCustomMarkerCategoryDialog) return
        // if (sharedViewModel.isAddCategoryDialogShowing) return
        binding.markerNameLayout.let {
            if (binding.markerNameInput.text.isNullOrEmpty()) it.error = "Type name!"
            else it.error = null
        }
    }

    // Resets background drawable for all item views.
    private fun resetClickedButtonState() {
        for (x in 0..<categoryRecyclerView.childCount) {
            categoryRecyclerView.getChildViewHolder(categoryRecyclerView.getChildAt(x)).itemView.setBackgroundColor(
                ContextCompat.getColor(requireContext(), R.color.transparent)
            )
        }
    }

    private fun updatePoiList(list: List<CustomPoiEntity>?, position: Int? = null) {
        list ?: return
        val itemCount = poiList.size

        if (position != null) {
            poiList.removeAt(position)
            poiGenericRecyclerViewAdapter.notifyItemRangeRemoved(position, itemCount)
            if (poiList.isEmpty()) binding.emptyListTextView.visibility = View.VISIBLE
        } else if (list.isNotEmpty()) {
            poiList.clear()
            poiGenericRecyclerViewAdapter.notifyItemRangeRemoved(0, itemCount)
            for (x in list) {
                poiList.add(x)
            }
            poiGenericRecyclerViewAdapter.notifyItemRangeChanged(0, poiList.size)
            binding.emptyListTextView.visibility = View.GONE
        } else {
            if ((categoryRecyclerView.getChildAt(viewModel.itemViewPosition)
                    ?: categoryRecyclerView.getChildAt(categoryGenericRecyclerViewAdapter.itemCount - 1))
                    .findViewById<TextView>(R.id.categoryName).text == categoryAddButton.categoryName
            ) {
                binding.emptyListTextView.visibility = View.GONE
                poiGenericRecyclerViewAdapter.itemCount
            } else {
                poiList.clear()
                poiGenericRecyclerViewAdapter.notifyItemRangeRemoved(0, itemCount)
                binding.emptyListTextView.visibility = View.VISIBLE
            }
        }
    }

    private fun removePoiCategory() {
        viewLifecycleOwner.lifecycleScope.launch {
            requireNotNull(viewModel.removeIndex).let {
                viewModel.setItemViewPosition(RecyclerView.NO_POSITION)
                viewModel.removeCategory(poiCategoriesList[it])
                poiCategoriesList.removeAt(it)
                updatePoiList(poiList)
                categoryGenericRecyclerViewAdapter.notifyItemRemoved(it)
                resetClickedButtonState()
            }
        }
    }

    private fun removePoiMarker() {
        viewLifecycleOwner.lifecycleScope.launch {
            requireNotNull(viewModel.removeIndex).let {
                poiRecyclerView.layoutManager?.findViewByPosition(it)
                    ?.findViewById<TextView>(R.id.poi_name).let { textView ->
                        if (textView != null) {
                            viewModel.removeCustomPoi(textView.tag as Long)
                        }
                    }
                updatePoiList(poiList, it)
            }
        }
    }

    override fun onDialogPositiveClick(dialogId: String, additionalData: Any?) {
        when (dialogId) {
            "delete_poi_category" -> removePoiCategory()
            "delete_poi_marker" -> removePoiMarker()
        }
    }

    override fun onStart() {
        super.onStart()

        // Apply the fullscreen dialog style
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        dialog?.window?.setBackgroundDrawable(
            ColorDrawable(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.dialog_background
                )
            )
        )
    }
}