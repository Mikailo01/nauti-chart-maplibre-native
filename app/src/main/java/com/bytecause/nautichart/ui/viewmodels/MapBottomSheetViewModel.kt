package com.bytecause.nautichart.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bytecause.nautichart.R
import com.bytecause.nautichart.data.repository.UserPreferencesRepository
import com.bytecause.nautichart.domain.model.LayersChildItem
import com.bytecause.nautichart.ui.view.fragment.bottomsheet.LayerTypes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MapBottomSheetViewModel @Inject constructor(
    private val repository: dagger.Lazy<UserPreferencesRepository>
) : ViewModel() {

    private val contentMap: MutableMap<LayerTypes, List<LayersChildItem>> =
        mutableMapOf(
            LayerTypes.TILESOURCE to listOf(
                LayersChildItem(
                    LayerTypes.TILESOURCE,
                    R.drawable.terrain,
                    R.string.default_tile_source
                ),
                LayersChildItem(
                    LayerTypes.TILESOURCE,
                    R.drawable.satellite,
                    R.string.satellite
                ),
                LayersChildItem(
                    LayerTypes.TILESOURCE,
                    R.drawable.topo_map,
                    R.string.topography
                )
            ),
            LayerTypes.ADDITIONALOVERLAY to listOf(
                LayersChildItem(
                    LayerTypes.ADDITIONALOVERLAY,
                    R.drawable.grid,
                    R.string.grid
                )
            )
        )

    private val _contentMapStateFlow: MutableStateFlow<Map<LayerTypes, List<LayersChildItem>>> =
        MutableStateFlow(contentMap)
    val contentMapStateFlow: StateFlow<Map<LayerTypes, List<LayersChildItem>>> =
        _contentMapStateFlow.asStateFlow()

    fun cacheSelectedTileSource(tileSourceName: String) {
        viewModelScope.launch {
            repository.get().cacheSelectedTileSource(tileSourceName)
        }
    }
}