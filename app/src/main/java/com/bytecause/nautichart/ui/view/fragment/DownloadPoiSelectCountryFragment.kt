package com.bytecause.nautichart.ui.view.fragment

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
import com.bytecause.nautichart.R
import com.bytecause.nautichart.databinding.DownloadPoiFragmentLayoutBinding
import com.bytecause.nautichart.domain.model.CountryParentItem
import com.bytecause.nautichart.domain.model.UiState
import com.bytecause.nautichart.interfaces.CountryAndRegionsListenerInterface
import com.bytecause.nautichart.ui.adapter.CountryParentAdapter
import com.bytecause.nautichart.ui.util.storageAvailable
import com.bytecause.nautichart.ui.view.custom.StatefulRecyclerView
import com.bytecause.nautichart.ui.view.delegate.viewBinding
import com.bytecause.nautichart.ui.viewmodels.DownloadPoiSelectCountryViewModel
import com.bytecause.nautichart.util.SearchTypes
import com.bytecause.nautichart.util.SimpleOverpassQueryBuilder
import com.bytecause.nautichart.util.StringUtil
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

fun Map<String, CountryParentItem>.getKeyByIndex(index: Int): String {
    return keys.toList()[index]
}

@AndroidEntryPoint
class DownloadPoiSelectCountryFragment : Fragment(R.layout.download_poi_fragment_layout),
    CountryAndRegionsListenerInterface {

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

        binding.headerTextView.text = getString(R.string.countries)

        binding.availableStorage.availableStorageRefreshButton.setOnClickListener {
            storageAvailable()
        }

        storageAvailable()

        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.CREATED) {
                viewModel.mapContent.collectLatest { content ->
                    if (content.isEmpty()) return@collectLatest

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

        viewModel.regionUiStateLiveData.observe(viewLifecycleOwner) { uiState ->
            when (uiState.error) {
                is UiState.Error.NetworkError -> {
                    viewModel.cancelRegionsLoading()
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.no_network_available),
                        Toast.LENGTH_SHORT
                    ).show()
                }

                is UiState.Error.ServiceUnavailable -> {
                    viewModel.cancelRegionsLoading()
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.service_unavailable),
                        Toast.LENGTH_SHORT
                    ).show()
                }

                is UiState.Error.Other -> {
                    viewModel.cancelRegionsLoading()
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.something_went_wrong),
                        Toast.LENGTH_SHORT
                    ).show()
                }

                null -> {
                    if (uiState.items.isEmpty()) return@observe
                    if (!uiState.isLoading) viewModel.cancelRegionsLoading()

                    viewModel.showCountryRegions(uiState.items, args.args[0])
                }
            }
        }

        viewModel.poiDownloadUiStateLiveData.observe(viewLifecycleOwner) { uiState ->
            when (uiState.error) {
                UiState.Error.NetworkError -> {
                    setDownloadState(false)
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.no_network_available),
                        Toast.LENGTH_SHORT
                    ).show()
                }

                UiState.Error.ServiceUnavailable -> {
                    setDownloadState(false)
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.service_unavailable),
                        Toast.LENGTH_SHORT
                    ).show()
                }

                UiState.Error.Other -> {
                    setDownloadState(false)
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.something_went_wrong),
                        Toast.LENGTH_SHORT
                    ).show()
                }

                null -> {
                    if (!uiState.isLoading) setDownloadState(false)
                    else viewModel.showDownloadProgressBar(true)

                    if (uiState.items.isEmpty()) return@observe

                    viewModel.resetCheckedState()
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.download_success),
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

    private fun setDownloadState(isDownloading: Boolean) {
        when (isDownloading) {
            true -> {
                viewModel.getPois(
                    regionName = "",
                    query = SimpleOverpassQueryBuilder(
                        format = SimpleOverpassQueryBuilder.FormatTypes.JSON,
                        timeoutInSeconds = 120,
                        regionNameList = viewModel.getRegionNameFromQueue(),
                        type = "node",
                        search = SearchTypes.UnionSet(StringUtil.searchTypesStringList.toTypedArray())
                    ).getQuery()
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
                    text = getString(R.string.cancel)
                    setTextColor(Color.RED)
                }
            }
            recyclerViewParentAdapter.setIsDownloading(true)
        } else {
            binding.downloadButton.apply {
                text = getString(R.string.download)
                setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.adaptive_color
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
        viewModel.changeRegionLoadingState(position)

        viewModel.countryList[position].let { country ->
            viewModel.getRegions(
                countryId = country.id,
                isoCode = country.iso2,
                query = SimpleOverpassQueryBuilder(
                    format = SimpleOverpassQueryBuilder.FormatTypes.JSON,
                    timeoutInSeconds = 120,
                    geocodeAreaISO = country.iso2,
                    type = "relation",
                    adminLevel = 4
                ).getQuery()
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