package com.bytecause.first_run.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.bytecause.domain.abstractions.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SelectRegionBottomSheetViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    suspend fun saveFirstRunFlag(flag: Boolean) {
        userPreferencesRepository.saveFirstRunFlag(flag)
    }
}