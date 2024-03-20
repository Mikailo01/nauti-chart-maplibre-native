package com.bytecause.nautichart.ui.view.fragment.dialog

import android.content.DialogInterface
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bytecause.nautichart.R
import com.bytecause.nautichart.databinding.CustomizeMapDialogFragmentLayoutBinding
import com.bytecause.nautichart.ui.adapter.GenericRecyclerViewAdapter
import com.bytecause.nautichart.ui.adapter.RecyclerViewBindingInterface
import com.bytecause.nautichart.ui.view.custom.AdaptiveSpacingItemDecoration
import com.bytecause.nautichart.ui.view.delegate.viewBinding
import com.bytecause.nautichart.ui.viewmodels.CustomizeMapViewModel
import com.bytecause.nautichart.ui.viewmodels.MapViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

private const val SCROLL_VIEW_ALPHA = 200
private const val TOOLBAR_ALPHA = 168

@AndroidEntryPoint
class CustomizeMapDialog : DialogFragment() {

    private val binding by viewBinding(CustomizeMapDialogFragmentLayoutBinding::inflate)

    private val mapViewModel: MapViewModel by activityViewModels()
    private val viewModel: CustomizeMapViewModel by viewModels()

    private lateinit var recyclerView: RecyclerView
    private lateinit var genericRecyclerViewAdapter: GenericRecyclerViewAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_FRAME, R.style.MyCustomTheme)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val bindingInterface = object : RecyclerViewBindingInterface<String> {
            override fun bindData(item: String, itemView: View, itemPosition: Int) {
                val textView: TextView = itemView.findViewById(R.id.poi_category_name_text_view)
                val imageView: ImageView = itemView.findViewById(R.id.poi_category_image_view)

                textView.text = item
                imageView.setOnClickListener {
                    Toast.makeText(requireContext(), "Not yet implemented.", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.CREATED) {
                viewModel.getAllDistinctCategories.collect { distinctCategoryList ->
                    if (distinctCategoryList.isEmpty()) {
                        binding.poiCategoriesRecyclerView.visibility = View.GONE
                        binding.noPoisDownloadedTextView.visibility = View.VISIBLE
                        return@collect
                    }

                    genericRecyclerViewAdapter = GenericRecyclerViewAdapter(
                        distinctCategoryList,
                        R.layout.customize_map_recycler_view_item_view,
                        bindingInterface
                    )

                    recyclerView = binding.poiCategoriesRecyclerView.apply {
                        layoutManager =
                            GridLayoutManager(
                                requireContext(),
                                3,
                                GridLayoutManager.HORIZONTAL,
                                false
                            )
                        adapter = genericRecyclerViewAdapter
                        addItemDecoration(AdaptiveSpacingItemDecoration(80, false))
                        setHasFixedSize(true)
                    }
                }
            }
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.chipShowAll.setOnClickListener {
            Toast.makeText(requireContext(), "Not yet implemented.", Toast.LENGTH_SHORT).show()
        }

        mapViewModel.vesselLocationsVisible.observe(viewLifecycleOwner) {
            binding.chipAis.isChecked = it
        }

        binding.chipAis.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                if (mapViewModel.vesselLocationsVisible.value == false) mapViewModel.toggleVesselLocations()
            } else {
                if (mapViewModel.vesselLocationsVisible.value == true) mapViewModel.toggleVesselLocations()
            }
        }

        binding.chipHarbours.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked && mapViewModel.harboursVisible.value != true) {
                mapViewModel.toggleHarboursLocations()
            } else if (!isChecked && mapViewModel.harboursVisible.value == true) {
                mapViewModel.toggleHarboursLocations()
            }
        }

        val layoutParams = binding.invisibleWindow.layoutParams
        layoutParams.height = resources.displayMetrics.heightPixels / 6
        binding.invisibleWindow.layoutParams = layoutParams

        binding.customizeMapScrollView.setBackgroundColor(
            ColorUtils.setAlphaComponent(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.dialog_background
                ), SCROLL_VIEW_ALPHA
            )
        )

        binding.toolbar.apply {
            toolbarAppBarLayout.setBackgroundColor(
                ColorUtils.setAlphaComponent(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.dialog_background
                    ), TOOLBAR_ALPHA
                )
            )

            navBack.apply {
                setOnClickListener {
                    findNavController().popBackStack()
                }
            }
            destNameTextView.apply {
                text = findNavController().currentDestination?.label
            }
        }

        mapViewModel.setIsCustomizeDialogVisible(true)

        mapViewModel.harboursVisible.observe(viewLifecycleOwner) {
            binding.chipHarbours.isChecked = it
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
                    R.color.transparent
                )
            )
        )
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        mapViewModel.setIsCustomizeDialogVisible(false)
    }
}