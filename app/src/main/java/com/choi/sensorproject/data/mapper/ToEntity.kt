package com.choi.sensorproject.data.mapper

import com.choi.sensorproject.domain.model.AppInfoModel
import com.choi.sensorproject.domain.model.SensorRecordModel
import com.choi.sensorproject.room.entity.AppInfoEntity
import com.choi.sensorproject.room.entity.SensorRecordEntity

fun SensorRecordModel.toEntity(): SensorRecordEntity{
    return SensorRecordEntity(
        xrAngle = xrAngle,
        zrAngle = zrAngle,
        recordTime = recordTime,
        runningAppName = runningAppName
    )
}

fun AppInfoModel.toEntity(): AppInfoEntity{
    return AppInfoEntity(
        appName = appName,
        appIcon = appIcon,
        appPlayingImage = appPlayingImage
    )
}