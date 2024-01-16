package com.choi.sensorproject.domain.repository

import com.choi.sensorproject.domain.model.SensorRecordModel
import com.choi.sensorproject.data.paging.SensorRecordPagingSource

interface SensorRecordRepository {

    suspend fun insertSensorRecord(sensorRecordModel: SensorRecordModel)
    suspend fun getSensorRecords(pageDate: String) : List<SensorRecordModel>
    fun getSensorRecordPagingSource(initPageDate: String) : SensorRecordPagingSource
}