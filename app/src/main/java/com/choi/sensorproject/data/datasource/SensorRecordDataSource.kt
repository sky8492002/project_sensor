package com.choi.sensorproject.data.datasource

import com.choi.sensorproject.domain.model.SensorRecordModel

interface SensorRecordDataSource {

    suspend fun insertSensorRecord(sensorRecordModel: SensorRecordModel)
}