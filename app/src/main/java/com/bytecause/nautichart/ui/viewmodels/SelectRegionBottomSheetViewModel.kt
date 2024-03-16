package com.bytecause.nautichart.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bytecause.nautichart.data.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SelectRegionBottomSheetViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository
): ViewModel() {

    fun saveFirstRunFlag(flag: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            userPreferencesRepository.saveFirstRunFlag(flag)
        }
    }
}