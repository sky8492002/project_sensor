package com.choi.sensorproject.domain.usecase

import com.choi.sensorproject.domain.model.SensorRecordModel
import com.choi.sensorproject.domain.repository.SensorRecordRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetSensorRecordsUseCase @Inject constructor(
    private val repository: SensorRecordRepository
) {
    suspend operator fun invoke(pageDate: String): Flow<List<SensorRecordModel>> {
        return repository.getSensorRecords(pageDate)
    }
}