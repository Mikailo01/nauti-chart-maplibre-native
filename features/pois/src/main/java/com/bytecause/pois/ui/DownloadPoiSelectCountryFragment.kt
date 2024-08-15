package com.bytecause.pois.ui

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.bytecause.domain.util.OverpassQueryBuilder
import com.bytecause.features.pois.R
import com.bytecause.features.pois.databinding.DownloadPoiFragmentLayoutBinding
import com.bytecause.pois.ui.model.CountryParentItem
import com.bytecause.pois.ui.recyclerview.adapter.CountryParentAdapter
import com.bytecause.pois.ui.recyclerview.interfaces.CountryAndRegionsListener
import com.bytecause.pois.ui.viewmodel.DownloadPoiSelectCountryViewModel
import com.bytecause.presentation.components.views.recyclerview.StatefulRecyclerView
import com.bytecause.util.context.storageAvailable
import com.bytecause.util.delegates.viewBinding
import com.bytecause.util.string.StringUtil
import com.bytecause.util.string.StringUtil.excludeObjectFiltersList
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import okio.IOException
import java.net.ConnectException

fun Map<String, CountryParentItem>.getKeyByIndex(index: Int): String {
    return keys.toList()[index]
}

@AndroidEntryPoint
class DownloadPoiSelectCountryFragment : Fragment(R.layout.download_poi_fragment_layout),
    CountryAndRegionsListener {

    private val binding by viewBinding(DownloadPoiFragmentLayoutBinding::bind)

    private val viewModel: DownloadPoiSelectCountryViewModel by viewModels()

    // [0] = Continent name, [1] = continentId
    private val args: DownloadPoiSelectCountryFragmentArgs by navArgs()

    private lateinit var recyclerView: StatefulRecyclerView
    private lateinit var recyclerViewParentAdapter: CountryParentAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        if (viewModel.mapContent.value.isEmpty()) viewModel.getAssociatedCountries(
            args.args[1].toInt(),
            args.args[0]
        )

        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.downloadButton.apply {
            visibility =
                if (viewModel.downloadQueueMap.values.all { it.isEmpty() }) View.GONE else View.VISIBLE

            setOnClickListener {
                when (viewModel.isDownloading()) {
                    true -> setDownloadState(isDownloading = false)
                    false -> setDownloadState(isDownloading = true)
                }
            }
        }

        binding.toolbar.apply {
            destNameTextView.text = args.args[0]
            navBack.setOnClickListener {
                findNavController().popBackStack()
            }
        }

        binding.headerTextView.text = getString(com.bytecause.core.resources.R.string.countries)

        binding.availableStorage.availableStorageRefreshButton.setOnClickListener {
            storageAvailable()
        }

        storageAvailable()

        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.CREATED) {
                viewModel.mapContent.collect { content ->
                    if (content.isEmpty()) {
                        setContentLoading(true)
                        return@collect
                    } else {
                        setContentLoading(false)
                    }

                    if (!::recyclerViewParentAdapter.isInitialized) {
                        recyclerViewParentAdapter = CountryParentAdapter(
                            content,
                            this@DownloadPoiSelectCountryFragment
                        )
                        recyclerView = binding.recyclerView.apply {
                            layoutManager = LinearLayoutManager(requireContext())
                            adapter = recyclerViewParentAdapter
                            setHasFixedSize(true)
                        }

                        viewModel.recyclerViewExpandedStateList.takeIf { stateList -> stateList.isNotEmpty() }
                            ?.let { states ->
                                recyclerViewParentAdapter.restoreExpandedStates(states)
                            }
                        setDownloadUiState(viewModel.isDownloading())
                    } else recyclerViewParentAdapter.submitMap(content)
                }
            }
        }

        viewModel.regionEntityUiStateLiveData.observe(viewLifecycleOwner) { uiState ->
            when (val exception = uiState.error) {
                is IOException -> {
                    viewModel.cancelRegionsLoadingState()
                    Toast.makeText(
                        requireContext(),
                        getString(
                            if (exception is ConnectException) com.bytecause.core.resources.R.string.service_unavailable
                            else com.bytecause.core.resources.R.string.no_network_available
                        ),
                        Toast.LENGTH_SHORT
                    ).show()
                }

                is NoSuchElementException -> {
                    viewModel.cancelRegionsLoadingState()
                    Toast.makeText(
                        requireContext(),
                        "No element found",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                null -> {
                    if (uiState.items.isEmpty()) return@observe
                    viewModel.showCountryRegions(uiState.items, args.args[0])
                }

                else -> {
                    viewModel.cancelRegionsLoadingState()
                    Toast.makeText(
                        requireContext(),
                        getString(com.bytecause.core.resources.R.string.something_went_wrong),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        viewModel.poiDownloadUiStateLiveData.observe(viewLifecycleOwner) { uiState ->
            if (viewModel.downloadJob == null) return@observe

            when (val exception = uiState.error) {
                is IOException -> {
                    setDownloadState(false)
                    Toast.makeText(
                        requireContext(),
                        getString(
                            if (exception is ConnectException) com.bytecause.core.resources.R.string.service_unavailable
                            else com.bytecause.core.resources.R.string.no_network_available
                        ),
                        Toast.LENGTH_SHORT
                    ).show()
                }

                null -> {
                    if (!uiState.isLoading) setDownloadState(false)
                    else viewModel.showDownloadProgressBar(true)

                    if (uiState.items.isEmpty()) return@observe

                    Toast.makeText(
                        requireContext(),
                        getString(com.bytecause.core.resources.R.string.download_success),
                        Toast.LENGTH_SHORT
                    ).show()
                }

                else -> {
                    setDownloadState(false)
                    Toast.makeText(
                        requireContext(),
                        getString(com.bytecause.core.resources.R.string.something_went_wrong),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.CREATED) {
                viewModel.downloadButtonVisibility.collectLatest { isVisible ->
                    if (isVisible) binding.downloadButton.visibility = View.VISIBLE
                    else binding.downloadButton.visibility = View.GONE
                }
            }
        }
    }

    private fun setContentLoading(isLoading: Boolean) {
        if (isLoading) {
            binding.progressBarLayout.visibility = View.VISIBLE
            binding.contentRelativeLayout.visibility = View.GONE
        } else {
            binding.progressBarLayout.visibility = View.GONE
            binding.contentRelativeLayout.visibility = View.VISIBLE
        }
    }

    private fun setDownloadState(isDownloading: Boolean) {
        when (isDownloading) {
            true -> {
                viewModel.getPois(
                    regionName = "",
                    query = OverpassQueryBuilder
                        .format(OverpassQueryBuilder.FormatTypes.JSON)
                        .timeout(120)
                        .region(viewModel.getRegionNameFromQueue())
                        .type(OverpassQueryBuilder.Type.Node)
                        .search(
                            com.bytecause.domain.util.SearchTypes.UnionSet(StringUtil.searchTypesStringList)
                                .filterNot(
                                    emptyList(),
                                    excludeObjectFiltersList,
                                    emptyList(),
                                    emptyList(),
                                    emptyList(),
                                    emptyList()
                                )
                        )
                        .build()
                )
                setDownloadUiState(true)
            }

            false -> {
                viewModel.cancelDownloadJob()
                setDownloadUiState(false)
            }
        }
    }

    private fun setDownloadUiState(isDownloading: Boolean) {
        if (!::recyclerViewParentAdapter.isInitialized) return

        if (isDownloading) {
            binding.downloadButton.apply {
                binding.downloadButton.apply {
                    text = getString(com.bytecause.core.resources.R.string.cancel)
                    setTextColor(Color.RED)
                }
            }
            recyclerViewParentAdapter.setIsDownloading(true)
        } else {
            binding.downloadButton.apply {
                text = getString(com.bytecause.core.resources.R.string.download)
                setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        com.bytecause.core.resources.R.color.adaptive_color
                    )
                )
            }
            recyclerViewParentAdapter.setIsDownloading(false)
        }
    }

    private fun storageAvailable() {
        requireContext().storageAvailable().let {
            binding.availableStorage.freeSpaceTextview.text =
                it.entries.firstOrNull()?.key ?: "-"
            binding.availableStorage.storageProgressBar.progress =
                it.entries.firstOrNull()?.value ?: 0
        }
    }

    override fun onCheckBoxClickListener(
        buttonView: CompoundButton,
        position: Int,
        parentPosition: Int,
        isChecked: Boolean
    ) {

        when (isChecked) {
            true -> {
                viewModel.addToDownloadQueue(parentPosition, position)
                viewModel.updateRegionClickedState(parentPosition, position)
            }

            false -> {
                viewModel.removeFromDownloadQueue(parentPosition, position)
                viewModel.updateRegionClickedState(parentPosition, position)
            }
        }
    }

    override fun onExpandClickListener(position: Int) {
        viewModel.regionIsLoadingState(position)

        viewModel.countryEntityList.find {
            it.iso2 == viewModel.mapContent.value.getKeyByIndex(position)
        }?.let { country ->
            viewModel.getRegions(
                countryId = country.id,
                isoCode = country.iso2,
                query = OverpassQueryBuilder
                    .format(OverpassQueryBuilder.FormatTypes.JSON)
                    .timeout(120)
                    .geocodeAreaISO(country.iso2)
                    .type(OverpassQueryBuilder.Type.Relation)
                    .adminLevel(4)
                    .build()
            )
        }
    }

    override fun onCountryDownloadClickListener(position: Int) {
        Toast.makeText(requireContext(), "Not yet implemented.", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        viewModel.saveRecyclerViewExpandedStates(recyclerViewParentAdapter.getExpandedStateList())
        super.onDestroyView()
    }
}