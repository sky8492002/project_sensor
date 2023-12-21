package com.choi.sensorproject.domain.repository

import com.choi.sensorproject.domain.model.SensorRecordModel
import kotlinx.coroutines.flow.Flow

interface SensorRecordRepository {

    suspend fun insertSensorRecord(sensorRecordModel: SensorRecordModel)
    suspend fun getSensorRecords(pageDate: String) : List<SensorRecordModel>
}