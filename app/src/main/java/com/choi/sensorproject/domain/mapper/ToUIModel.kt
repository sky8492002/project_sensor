package com.choi.sensorproject.domain.mapper

import androidx.paging.PagingData
import androidx.paging.map
import com.choi.sensorproject.domain.model.AppInfoModel
import com.choi.sensorproject.domain.model.RecordsForHourModel
import com.choi.sensorproject.domain.model.SensorRecordModel
import com.choi.sensorproject.ui.model.AppInfoUIModel
import com.choi.sensorproject.ui.model.RecordsForHourUIModel
import com.choi.sensorproject.ui.model.SensorRecordUIModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

fun SensorRecordModel.toUIModel(): SensorRecordUIModel {
    return SensorRecordUIModel(
        xAngle = xAngle,
        zAngle = zAngle,
        orientation = orientation,
        recordTime = recordTime,
        runningAppName = runningAppName,
        isScreenOn = isScreenOn
    )
}
@JvmName("callFromSensorRecordModel")
fun List<SensorRecordModel>.toUIModels(): List<SensorRecordUIModel> {
    return this.map{
        SensorRecordUIModel(
            xAngle = it.xAngle,
            zAngle = it.zAngle,
            orientation = it.orientation,
            recordTime = it.recordTime,
            runningAppName = it.runningAppName,
            isScreenOn = it.isScreenOn
        )
    }
}

fun PagingData<RecordsForHourModel>.toUIModelsPagingData(): PagingData<RecordsForHourUIModel>{
    return this.map{
        it.toUIModel()
    }
}

fun RecordsForHourModel.toUIModel(): RecordsForHourUIModel{
    return RecordsForHourUIModel(
        date = date,
        hour = hour,
        records = records.toUIModels()
    )
}
fun AppInfoModel.toUIModel(): AppInfoUIModel{
    return AppInfoUIModel(
        appName = appName,
        appIcon = appIcon,
        appPlayingImage = appPlayingImage
    )
}
@JvmName("callFromAppInfoModel") // List형에 대하여 toUIModels가 중복이므로 고유한 이름을 지정해야 함
fun List<AppInfoModel>.toUIModels(): List<AppInfoUIModel> {
    return this.map{
        AppInfoUIModel(
            appName = it.appName,
            appIcon = it.appIcon,
            appPlayingImage = it.appPlayingImage
        )
    }
}

fun Flow<List<AppInfoModel>>.toUIModelsFlow(): Flow<List<AppInfoUIModel>> {
    return this.map{
        val appInfoUIModels: MutableList<AppInfoUIModel> = mutableListOf()
        for(model in it){
            appInfoUIModels.add(model.toUIModel())
        }
        appInfoUIModels
    }
}