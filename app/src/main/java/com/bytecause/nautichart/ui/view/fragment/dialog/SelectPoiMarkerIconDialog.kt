package com.bytecause.nautichart.ui.view.fragment.dialog

import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bytecause.nautichart.R
import com.bytecause.nautichart.databinding.SelectPoiMarkerIconDialogBinding
import com.bytecause.nautichart.domain.model.IconsChildItem
import com.bytecause.nautichart.interfaces.SelectPoiMarkerIconInterface
import com.bytecause.nautichart.ui.adapter.CustomPoiMarkerIconParentAdapter
import com.bytecause.nautichart.ui.view.delegate.viewBinding
import com.bytecause.nautichart.ui.viewmodels.SelectPoiMarkerIconViewModel
import com.bytecause.nautichart.util.TAG
import com.bytecause.nautichart.util.Util
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class SelectPoiMarkerIconDialog : DialogFragment(), SelectPoiMarkerIconInterface {

    private val binding by viewBinding(SelectPoiMarkerIconDialogBinding::inflate)

    private val viewModel: SelectPoiMarkerIconViewModel by viewModels()

    private lateinit var parentRecyclerView: RecyclerView
    private lateinit var parentAdapter: CustomPoiMarkerIconParentAdapter

    private val util = Util()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.CREATED) {
                viewModel.getRecentUsedPoiMarkerIcons().collect {
                    it.iconDrawableResourceNameList.reversed().let { drawableResourceNameList ->

                        // create recycler view with icons content.
                        viewModel.contentList.value?.let { newContent ->
                            withContext(Dispatchers.Main) {
                                if (!::parentRecyclerView.isInitialized) {
                                    parentRecyclerView =
                                        binding.poiMarkerIconsParentRecyclerView.apply {
                                            layoutManager = LinearLayoutManager(requireContext())
                                            setHasFixedSize(true)
                                            parentAdapter = CustomPoiMarkerIconParentAdapter(
                                                newContent,
                                                this@SelectPoiMarkerIconDialog
                                            )
                                            adapter = parentAdapter
                                        }
                                }
                            }
                        }

                        if (drawableResourceNameList.isEmpty()) return@collect

                        // if proto datastore holds recently used icons and recentlyUsedIcons list is empty
                        // populate recentlyUsedIcons list with content from proto datastore.
                        if (viewModel.recentlyUsedIcons.isEmpty()) {
                            for (element in drawableResourceNameList) {
                                viewModel.addUsedIcon(
                                    IconsChildItem(
                                        "Recently used",
                                        resources.getIdentifier(
                                            element.drawableResourceName,
                                            "drawable",
                                            requireActivity().packageName
                                        )
                                    )
                                )
                            }
                        }
                        // if icons are not present in list, update state.
                        if (!viewModel.iconList.containsAll(viewModel.recentlyUsedIcons)) viewModel.updateRecentlyUsedIcons()

                        withContext(Dispatchers.Main) {
                            parentAdapter.notifyDataSetChanged()
                        }
                    }
                }
            }
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.apply {
            destNameTextView.text = getString(R.string.select_icon)
            navBack.setOnClickListener {
                findNavController().popBackStack()
            }
        }
    }

    override fun onIconClickListener(view: View, position: Int) {
        if (!util.lastClick(500)) return
        (view.findViewById<ImageButton>(R.id.icon_view_holder).tag as Int).let { drawableId ->
            // Saves clicked icon drawable resource name into proto datastore.
            viewModel.saveRecentlyUsedPoiMarkerIcon(resources.getResourceEntryName(drawableId))

            findNavController().previousBackStackEntry?.savedStateHandle?.set("selectedDrawableId", drawableId)
            findNavController().popBackStack()
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
