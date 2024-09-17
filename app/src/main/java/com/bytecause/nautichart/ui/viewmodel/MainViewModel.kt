package com.bytecause.nautichart.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bytecause.domain.abstractions.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    val shouldRequestNotificationPermission: Flow<Boolean> =
        userPreferencesRepository.getShouldRequestNotificationPermission()

    fun saveShouldRequestNotificationPermission(boolean: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.saveShouldRequestNotificationPermission(boolean)
        }
    }
}