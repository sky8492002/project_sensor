package com.choi.sensorproject.domain.mapper

import com.choi.sensorproject.domain.model.AppInfoModel
import com.choi.sensorproject.ui.model.AppInfoUIModel

fun AppInfoUIModel.toModel():AppInfoModel{
    return AppInfoModel(
        appName = appName,
        appIcon = appIcon,
        appPlayingImage = appPlayingImage
    )
}