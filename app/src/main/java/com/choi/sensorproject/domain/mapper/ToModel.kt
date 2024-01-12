package com.choi.sensorproject.domain.mapper

import com.choi.sensorproject.domain.model.AppInfoModel
import com.choi.sensorproject.domain.model.SensorRecordModel
import com.choi.sensorproject.ui.model.AppInfoUIModel
import com.choi.sensorproject.ui.model.SensorRecordUIModel

fun SensorRecordUIModel.toModel(): SensorRecordModel {
    return SensorRecordModel(
        xAngle = xAngle,
        zAngle = zAngle,
        orientation = orientation,
        recordTime = recordTime,
        runningAppName = runningAppName,
        isScreenOn = isScreenOn
    )
}
fun AppInfoUIModel.toModel():AppInfoModel{
    return AppInfoModel(
        appName = appName,
        appIcon = appIcon,
        appPlayingImage = appPlayingImage
    )
}