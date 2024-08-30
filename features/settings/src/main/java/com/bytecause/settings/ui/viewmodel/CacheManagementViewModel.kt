package com.bytecause.settings.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bytecause.data.repository.abstractions.DownloadedRegionsRepository
import com.bytecause.domain.abstractions.HarboursDatabaseRepository
import com.bytecause.domain.abstractions.PoiCacheRepository
import com.bytecause.domain.abstractions.VesselsDatabaseRepository
import com.bytecause.settings.ui.event.CacheManagementEffect
import com.bytecause.settings.ui.event.CacheManagementEvent
import com.bytecause.settings.ui.state.CacheManagementState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CacheManagementViewModel @Inject constructor(
    private val harboursRepository: HarboursDatabaseRepository,
    private val vesselsRepository: VesselsDatabaseRepository,
    private val poiCacheRepository: PoiCacheRepository,
    private val downloadedRegionsRepository: DownloadedRegionsRepository
) : ViewModel() {

    private val _uiState: MutableStateFlow<CacheManagementState> =
        MutableStateFlow(CacheManagementState())
    val uiState: StateFlow<CacheManagementState> = _uiState.asStateFlow()

    private val _effect = Channel<CacheManagementEffect>(capacity = Channel.CONFLATED)
    val effect = _effect.receiveAsFlow()

    fun uiEventHandler(event: CacheManagementEvent) {
        when (event) {
            CacheManagementEvent.OnNavigateBack -> sendEffect(CacheManagementEffect.NavigateBack)
        }
    }

    private fun sendEffect(effect: CacheManagementEffect) {
        viewModelScope.launch {
            _effect.send(effect)
        }
    }
}