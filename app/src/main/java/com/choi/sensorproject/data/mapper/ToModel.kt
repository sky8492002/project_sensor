package com.choi.sensorproject.data.mapper

import com.choi.sensorproject.domain.model.AppInfoModel
import com.choi.sensorproject.domain.model.SensorRecordModel
import com.choi.sensorproject.room.entity.AppInfoEntity
import com.choi.sensorproject.room.entity.SensorRecordEntity

fun SensorRecordEntity.toModel(): SensorRecordModel {
    return SensorRecordModel(
        xrAngle = xrAngle,
        zrAngle = zrAngle,
        recordTime = recordTime,
        runningAppName = runningAppName
    )
}

fun AppInfoEntity.toModel(): AppInfoModel{
    return AppInfoModel(
        appName = appName,
        appPlayingImage = appPlayingImage
    )
}