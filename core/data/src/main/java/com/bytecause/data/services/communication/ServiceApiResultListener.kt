package com.bytecause.data.services.communication

import com.bytecause.domain.model.ApiResult
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

sealed class ServiceEvent {
    data class RegionPoiDownload(val regionId: Int, val result: ApiResult<*>) : ServiceEvent()
    data class RegionPoiDownloadStarted(val regionId: Int) : ServiceEvent()
    data class RegionPoiDownloadCancelled(val regionId: Int) : ServiceEvent()
    data class HarboursUpdate(val result: ApiResult<*>) : ServiceEvent()
    data object HarboursUpdateStarted : ServiceEvent()
    data object HarboursUpdateCancelled : ServiceEvent()
}

object ServiceApiResultListener {
    private val _eventFlow = MutableSharedFlow<ServiceEvent>(replay = 1)
    val eventFlow: SharedFlow<ServiceEvent> = _eventFlow.asSharedFlow()

    fun postEvent(event: ServiceEvent) {
        _eventFlow.tryEmit(event)
    }
}