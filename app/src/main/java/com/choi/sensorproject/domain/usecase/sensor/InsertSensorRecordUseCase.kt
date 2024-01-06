package com.choi.sensorproject.domain.usecase.sensor

import com.choi.sensorproject.domain.mapper.toModel
import com.choi.sensorproject.domain.model.SensorRecordModel
import com.choi.sensorproject.domain.repository.SensorRecordRepository
import com.choi.sensorproject.ui.model.SensorRecordUIModel
import javax.inject.Inject

class InsertSensorRecordUseCase @Inject constructor(
    private val repository: SensorRecordRepository
){
    suspend operator fun invoke(sensorRecordUIModel: SensorRecordUIModel){
        repository.insertSensorRecord(sensorRecordUIModel.toModel())
    }
}