package com.choi.sensorproject.domain.usecase

import com.choi.sensorproject.domain.model.SensorRecordModel
import com.choi.sensorproject.domain.repository.SensorRecordRepository
import javax.inject.Inject

class InsertSensorRecordUseCase @Inject constructor(
    private val repository: SensorRecordRepository
){

    suspend operator fun invoke(xAngle: Float, zAngle: Float, recordTime: String, runningAppName: String){
        val sensorRecordModel = SensorRecordModel(xAngle, zAngle, recordTime, runningAppName)
        repository.insertSensorRecord(sensorRecordModel)
    }
}