package com.bytecause.pois.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bytecause.domain.model.ContinentModel
import com.bytecause.features.pois.R
import com.bytecause.features.pois.databinding.DownloadPoiFragmentLayoutBinding
import com.bytecause.pois.ui.viewmodel.DownloadPoiViewModel
import com.bytecause.presentation.components.views.recyclerview.StatefulRecyclerView
import com.bytecause.presentation.components.views.recyclerview.adapter.GenericRecyclerViewAdapter
import com.bytecause.util.bindings.RecyclerViewBindingInterface
import com.bytecause.util.context.storageAvailable
import com.bytecause.util.delegates.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


@AndroidEntryPoint
class DownloadPoiFragment : Fragment(R.layout.download_poi_fragment_layout) {

    private val binding by viewBinding(DownloadPoiFragmentLayoutBinding::bind)

    private val viewModel: DownloadPoiViewModel by viewModels()

    private lateinit var recyclerView: StatefulRecyclerView
    private lateinit var genericRecyclerViewAdapter: GenericRecyclerViewAdapter<ContinentModel>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val bindingInterface = object : RecyclerViewBindingInterface<ContinentModel> {
            override fun bindData(item: ContinentModel, itemView: View, itemPosition: Int) {
                val innerItemView: LinearLayout =
                    itemView.findViewById(R.id.recycler_view_inner_item_view)
                val imageView: ImageView = itemView.findViewById(R.id.place_image_view)
                val continentName: TextView =
                    itemView.findViewById(com.bytecause.core.presentation.R.id.place_name_text_view)
                val distanceTextView: TextView =
                    itemView.findViewById(com.bytecause.core.presentation.R.id.distance_textview)

                innerItemView.setOnClickListener {
                    val action =
                        DownloadPoiFragmentDirections.actionDownloadPoiFragmentToDownloadPoiSelectCountryFragment(
                            arrayOf(
                                item.name,
                                item.id.toString()
                            )
                        )
                    findNavController().navigate(action)
                }

                imageView.setImageDrawable(
                    ContextCompat.getDrawable(
                        requireContext(),
                        com.bytecause.core.resources.R.drawable.earth_24
                    )
                )
                continentName.text = item.name
                distanceTextView.visibility = View.GONE
            }
        }

        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.continentListStateFlow.collect { continentList ->
                    continentList ?: return@collect

                    withContext(Dispatchers.Main) {
                        genericRecyclerViewAdapter =
                            GenericRecyclerViewAdapter(
                                continentList,
                                com.bytecause.core.presentation.R.layout.searched_places_recycler_view_item_view,
                                bindingInterface
                            )

                        recyclerView = binding.recyclerView.apply {
                            layoutManager = LinearLayoutManager(requireContext())
                            adapter = genericRecyclerViewAdapter
                            setHasFixedSize(true)
                        }
                    }
                }
            }
        }

        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.headerTextView.text = getString(com.bytecause.core.resources.R.string.continents)

        binding.toolbar.apply {
            destNameTextView.text = findNavController().currentDestination?.label
            navBack.setOnClickListener {
                findNavController().popBackStack()
            }
        }
        binding.availableStorage.availableStorageRefreshButton.setOnClickListener {
            storageAvailable()
        }

        storageAvailable()
    }

    private fun storageAvailable() {
        requireContext().storageAvailable().let {
            binding.availableStorage.freeSpaceTextview.text =
                it.entries.firstOrNull()?.key ?: "-"
            binding.availableStorage.storageProgressBar.progress =
                it.entries.firstOrNull()?.value ?: 0
        }
    }
}