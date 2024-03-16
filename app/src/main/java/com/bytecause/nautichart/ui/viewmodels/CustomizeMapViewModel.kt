package com.bytecause.nautichart.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.bytecause.nautichart.data.repository.PoiCacheRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import javax.inject.Inject

@HiltViewModel
class CustomizeMapViewModel @Inject constructor(
    poiCacheRepository: PoiCacheRepository
) : ViewModel() {

    val getAllDistinctCategories: Flow<List<String>> = poiCacheRepository.getAllDistinctCategories
        .catch {
            emit(emptyList())
        }
}