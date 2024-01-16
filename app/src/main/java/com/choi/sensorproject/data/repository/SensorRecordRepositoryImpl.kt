package com.choi.sensorproject.data.repository

import com.choi.sensorproject.data.datasource.SensorRecordDataSource
import com.choi.sensorproject.domain.model.SensorRecordModel
import com.choi.sensorproject.data.paging.SensorRecordPagingSource
import com.choi.sensorproject.domain.repository.SensorRecordRepository
import javax.inject.Inject

class SensorRecordRepositoryImpl @Inject constructor(
    private val dataSource: SensorRecordDataSource
): SensorRecordRepository {
    override suspend fun insertSensorRecord(sensorRecordModel: SensorRecordModel) {
        dataSource.insertSensorRecord(sensorRecordModel)
    }

    override suspend fun getSensorRecords(pageDate: String): List<SensorRecordModel> {
        return dataSource.getSensorRecords(pageDate)
    }

    override fun getSensorRecordPagingSource(initPageDate: String): SensorRecordPagingSource {
        return SensorRecordPagingSource(this, initPageDate)
    }
}