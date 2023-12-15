package com.choi.sensorproject.domain.repository

import com.choi.sensorproject.domain.model.SensorRecordModel
import kotlinx.coroutines.flow.Flow

interface SensorRecordRepository {

    fun insertSensorRecord()
    fun getSensorRecords() : Flow<SensorRecordModel>
}