package com.choi.sensorproject.data.datasource

import com.choi.sensorproject.data.mapper.toEntity
import com.choi.sensorproject.data.mapper.toModel
import com.choi.sensorproject.domain.model.SensorRecordModel
import com.choi.sensorproject.data.room.AppDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class SensorRecordDataSourceImpl @Inject constructor(
    private val appDatabase: AppDatabase
): SensorRecordDataSource {
    override suspend fun insertSensorRecord(sensorRecordModel: SensorRecordModel) {
        appDatabase.sensorRecordDao().insertSensorAngle(sensorRecordModel.toEntity())
    }

    override suspend fun getSensorRecords(pageDate: String): List<SensorRecordModel> {

        val pageDateQuery = "$pageDate%"

        val sensorRecordModelList : MutableList<SensorRecordModel> = mutableListOf()
        val sensorRecordEntityList = appDatabase.sensorRecordDao().getSensorRecords(pageDateQuery)

        for(entity in sensorRecordEntityList){
            sensorRecordModelList.add(entity.toModel())
        }

        return sensorRecordModelList

    }

}