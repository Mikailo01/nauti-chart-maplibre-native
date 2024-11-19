package data.repository

import com.bytecause.domain.abstractions.TrackRouteRepository
import com.bytecause.domain.model.RouteRecordModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakeTrackRouteRepositoryImpl : TrackRouteRepository {

    private val records = mutableListOf<RouteRecordModel>()

    override suspend fun saveRecord(record: RouteRecordModel) {
        records.add(record)
    }

    override suspend fun removeRecord(id: Long) {
        records.removeIf { it.id == id }
    }

    override fun getRecordById(id: Long): Flow<RouteRecordModel?> =
        flowOf(records.find { it.id == id })

    override fun getRecordByTimestamp(timestamp: Long): Flow<RouteRecordModel?> =
        flowOf(records.find { it.dateCreated == timestamp })

    override fun getRecords(): Flow<List<RouteRecordModel>> = flowOf(records)
}