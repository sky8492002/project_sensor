package com.choi.sensorproject.data.mapper

import com.choi.sensorproject.domain.model.AppInfoModel
import com.choi.sensorproject.domain.model.SensorRecordModel
import com.choi.sensorproject.room.entity.AppInfoEntity
import com.choi.sensorproject.room.entity.SensorRecordEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

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

fun Flow<List<AppInfoEntity>>.toModelsFlow(): Flow<List<AppInfoModel>>{
    return this.map{
        val appInfoModels: MutableList<AppInfoModel> = mutableListOf()
        for(entity in it){
            appInfoModels.add(entity.toModel())
        }
        appInfoModels.toList()
    }
}