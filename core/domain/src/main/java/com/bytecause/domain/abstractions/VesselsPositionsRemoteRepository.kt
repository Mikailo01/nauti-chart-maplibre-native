package com.bytecause.domain.abstractions

import com.bytecause.domain.model.ApiResult
import com.bytecause.domain.model.VesselInfoModel
import com.bytecause.domain.model.VesselModel

interface VesselsPositionsRemoteRepository {
    suspend fun parseXml(): ApiResult<List<VesselInfoModel>>
}