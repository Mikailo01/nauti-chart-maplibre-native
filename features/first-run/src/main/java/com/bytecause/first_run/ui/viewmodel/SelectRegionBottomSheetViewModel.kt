package com.bytecause.first_run.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bytecause.data.repository.UserPreferencesRepositoryImpl
import com.bytecause.data.repository.abstractions.UserPreferencesRepository
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