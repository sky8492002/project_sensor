package com.choi.sensorproject.data.mapper

import android.annotation.SuppressLint
import com.choi.sensorproject.domain.model.AppInfoModel
import com.choi.sensorproject.domain.model.RecordsForHourModel
import com.choi.sensorproject.domain.model.SensorRecordModel
import com.choi.sensorproject.data.room.entity.AppInfoEntity
import com.choi.sensorproject.data.room.entity.SensorRecordEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat

fun SensorRecordEntity.toModel(): SensorRecordModel {
    return SensorRecordModel(
        xAngle = xAngle,
        zAngle = zAngle,
        recordTime = recordTime,
        orientation = orientation,
        runningAppName = runningAppName,
        isScreenOn = isScreenOn
    )
}

// 시간 별로 통합하여 분류
fun List<SensorRecordModel>.toRecordsForHourModels(pageDate: String): List<RecordsForHourModel> {
    @SuppressLint("SimpleDateFormat")
    val timeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    @SuppressLint("SimpleDateFormat")
    val hourFormat = SimpleDateFormat("HH")

    val recordsForHourModels: MutableList<RecordsForHourModel> = mutableListOf()
    for (hour in 0 until 24) {
        val emptyRecords: MutableList<SensorRecordModel> = mutableListOf()
        recordsForHourModels.add(RecordsForHourModel(pageDate, hour.toString(), emptyRecords))
    }

    // SensorRecordModel list의 요소를 시간 별로 통합하여 RecordsForHourModel list로 변환
    for (sensorRecordModel in this) {
        val hour = sensorRecordModel.recordTime.split(" ")[1].split(":")[0].toInt()
        recordsForHourModels[hour].records.add(sensorRecordModel)
//        timeFormat.parse(sensorRecordModel.recordTime)?.let {
//            val hour = hourFormat.format(it).toInt()
//            recordsForHourModels[hour].records.add(sensorRecordModel)
//        }
    }

    return recordsForHourModels.toList()
}

fun AppInfoEntity.toModel(): AppInfoModel{
    return AppInfoModel(
        appName = appName,
        appIcon = appIcon,
        appPlayingImage = appPlayingImage
    )
}

fun List<AppInfoEntity>.toModels(): List<AppInfoModel>{
    return this.map{
        AppInfoModel(
            appName = it.appName,
            appIcon = it.appIcon,
            appPlayingImage = it.appPlayingImage
        )
    }
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