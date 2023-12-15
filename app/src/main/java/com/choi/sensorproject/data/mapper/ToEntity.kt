package com.choi.sensorproject.data.mapper

import com.choi.sensorproject.domain.model.SensorRecordModel
import com.choi.sensorproject.room.entity.SensorRecordEntity

fun SensorRecordModel.toEntity(): SensorRecordEntity{
    return SensorRecordEntity(
        xAngle = xAngle,
        yAngle = yAngle,
        recordTime = recordTime,
        runningAppName = runningAppName
    )
}