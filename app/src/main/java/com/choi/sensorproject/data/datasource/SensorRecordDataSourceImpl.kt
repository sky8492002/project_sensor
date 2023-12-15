package com.choi.sensorproject.data.datasource

import com.choi.sensorproject.data.mapper.toEntity
import com.choi.sensorproject.domain.model.SensorRecordModel
import com.choi.sensorproject.room.AppDatabase
import javax.inject.Inject

class SensorRecordDataSourceImpl @Inject constructor(
    private val appDatabase: AppDatabase
): SensorRecordDataSource {
    override suspend fun insertSensorRecord(sensorRecordModel: SensorRecordModel) {
        appDatabase.sensorRecordDao().insertSensorAngle(sensorRecordModel.toEntity())
    }

}