package com.choi.sensorproject.domain.usecase

import com.choi.sensorproject.domain.repository.SensorRecordRepository
import javax.inject.Inject

class InsertSensorRecordUseCase @Inject constructor(
    private val repository: SensorRecordRepository
){

    operator fun invoke(xAngle: Float, yAngle: Float){

    }
}