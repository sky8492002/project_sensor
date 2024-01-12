package com.choi.sensorproject.data.mapper

import com.choi.sensorproject.domain.model.AppInfoModel
import com.choi.sensorproject.domain.model.SensorRecordModel
import com.choi.sensorproject.data.room.entity.AppInfoEntity
import com.choi.sensorproject.data.room.entity.SensorRecordEntity

fun SensorRecordModel.toEntity(): SensorRecordEntity {
    return SensorRecordEntity(
        xAngle = xAngle,
        zAngle = zAngle,
        recordTime = recordTime,
        orientation = orientation,
        runningAppName = runningAppName,
        isScreenOn = isScreenOn
    )
}

fun AppInfoModel.toEntity(): AppInfoEntity {
    return AppInfoEntity(
        appName = appName,
        appIcon = appIcon,
        appPlayingImage = appPlayingImage
    )
}