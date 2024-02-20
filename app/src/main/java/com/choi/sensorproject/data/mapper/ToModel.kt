package com.choi.sensorproject.data.mapper

import android.annotation.SuppressLint
import android.util.Log
import com.choi.sensorproject.domain.model.AppInfoModel
import com.choi.sensorproject.domain.model.RecordsForHourModel
import com.choi.sensorproject.domain.model.SensorRecordModel
import com.choi.sensorproject.data.room.entity.AppInfoEntity
import com.choi.sensorproject.data.room.entity.SensorRecordEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
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

    val sensorRecordModels = this
    val recordsForHourModels: MutableList<RecordsForHourModel> = mutableListOf()

    for (hour in 0 until 24) {
        val emptyRecords: MutableList<SensorRecordModel> = mutableListOf()
        recordsForHourModels.add(RecordsForHourModel(pageDate, hour.toString(), emptyRecords))
    }

    // 여기서 timeFormat.parse(sensorRecordUIModel.recordTime)을 하는 것이 customClockView에서 시점을 터치할 때의 버벅임을 줄여줌
    // 동일한 함수를 호출할 때 캐시에 저장된 데이터를 사용하기 때문
    // 기록 로딩 속도에 영향을 주지 않기 위해 비동기로 실행
    CoroutineScope(Dispatchers.IO).launch {
        for (sensorRecordModel in sensorRecordModels) {
            timeFormat.parse(sensorRecordModel.recordTime)
        }
    }

    // SensorRecordModel list의 요소를 시간 별로 통합하여 RecordsForHourModel list로 변환
    for (sensorRecordModel in sensorRecordModels) {
        val hour = sensorRecordModel.recordTime.split(" ")[1].split(":")[0].toInt()
        recordsForHourModels[hour].records.add(sensorRecordModel)
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