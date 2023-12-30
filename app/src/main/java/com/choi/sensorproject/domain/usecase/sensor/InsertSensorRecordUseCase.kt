package com.choi.sensorproject.domain.usecase.sensor

import com.choi.sensorproject.domain.model.SensorRecordModel
import com.choi.sensorproject.domain.repository.SensorRecordRepository
import javax.inject.Inject

class InsertSensorRecordUseCase @Inject constructor(
    private val repository: SensorRecordRepository
){
    suspend operator fun invoke(xrAngle: Float, zrAngle: Float, recordTime: String, runningAppName: String){
        val sensorRecordModel = SensorRecordModel(xrAngle, zrAngle, recordTime, runningAppName)
        repository.insertSensorRecord(sensorRecordModel)
    }
}