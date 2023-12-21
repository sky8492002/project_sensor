package com.choi.sensorproject.data.datasource

import com.choi.sensorproject.domain.model.SensorRecordModel
import kotlinx.coroutines.flow.Flow

interface SensorRecordDataSource {

    suspend fun insertSensorRecord(sensorRecordModel: SensorRecordModel)
    suspend fun getSensorRecords(pageDate: String): List<SensorRecordModel>
}