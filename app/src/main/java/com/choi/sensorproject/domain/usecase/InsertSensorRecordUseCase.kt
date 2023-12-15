package com.choi.sensorproject.domain.usecase

import com.choi.sensorproject.domain.model.SensorRecordModel
import com.choi.sensorproject.domain.repository.SensorRecordRepository
import javax.inject.Inject

class InsertSensorRecordUseCase @Inject constructor(
    private val repository: SensorRecordRepository
){

    operator fun invoke(xAngle: Float, yAngle: Float, recordTime: String, runningAppName: String){
        val sensorRecordModel = SensorRecordModel(xAngle, yAngle, recordTime, runningAppName)
        repository.insertSensorRecord(sensorRecordModel)
    }
}