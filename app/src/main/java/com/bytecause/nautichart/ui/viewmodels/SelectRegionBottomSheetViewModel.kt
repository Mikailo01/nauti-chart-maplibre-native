package com.bytecause.nautichart.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bytecause.nautichart.data.repository.UserPreferencesRepositoryImpl
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SelectRegionBottomSheetViewModel @Inject constructor(
    private val userPreferencesRepositoryImpl: UserPreferencesRepositoryImpl
): ViewModel() {

    fun saveFirstRunFlag(flag: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            userPreferencesRepositoryImpl.saveFirstRunFlag(flag)
        }
    }
}