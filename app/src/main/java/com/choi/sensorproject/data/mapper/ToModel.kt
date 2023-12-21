package com.choi.sensorproject.data.mapper

import com.choi.sensorproject.domain.model.SensorRecordModel
import com.choi.sensorproject.room.entity.SensorRecordEntity

fun SensorRecordEntity.toModel(): SensorRecordModel {
    return SensorRecordModel(
        xAngle = xAngle,
        zAngle = zAngle,
        recordTime = recordTime,
        runningAppName = runningAppName
    )
}