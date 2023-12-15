package com.choi.sensorproject.data.repository

import com.choi.sensorproject.data.datasource.SensorRecordDataSource
import com.choi.sensorproject.domain.model.SensorRecordModel
import com.choi.sensorproject.domain.repository.SensorRecordRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SensorRecordRepositoryImpl @Inject constructor(
    private val dataSource: SensorRecordDataSource
): SensorRecordRepository {
    override fun insertSensorRecord() {
        TODO("Not yet implemented")
    }

    override fun getSensorRecords(): Flow<SensorRecordModel> {
        TODO("Not yet implemented")
    }
}