package com.choi.sensorproject.domain.mapper

import android.annotation.SuppressLint
import com.choi.sensorproject.domain.model.AppInfoModel
import com.choi.sensorproject.domain.model.SensorRecordModel
import com.choi.sensorproject.ui.model.AppInfoUIModel
import com.choi.sensorproject.ui.model.RecordsForHourUIModel
import java.text.SimpleDateFormat

// 시간 별로 통합하여 분류
fun List<SensorRecordModel>.toRecordsForHourUIModels(pageDate: String): List<RecordsForHourUIModel> {
    @SuppressLint("SimpleDateFormat")
    val timeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    @SuppressLint("SimpleDateFormat")
    val hourFormat = SimpleDateFormat("HH")

    val recordsForHourUIModelList: MutableList<RecordsForHourUIModel> = mutableListOf()
    for (hour in 0 until 24) {
        val emptyRecords: MutableList<SensorRecordModel> = mutableListOf()
        recordsForHourUIModelList.add(RecordsForHourUIModel(pageDate, hour.toString(), emptyRecords))
    }

    // SensorRecordModel list의 요소를 시간 별로 통합하여 RecordsForHourUIModel list로 변환
    for (sensorRecordModel in this) {
        timeFormat.parse(sensorRecordModel.recordTime)?.let {
            val hour = hourFormat.format(it).toInt()
            recordsForHourUIModelList[hour].records.add(sensorRecordModel)
        }
    }

    return recordsForHourUIModelList.toList()
}

fun AppInfoModel.toUiModel(): AppInfoUIModel{
    return AppInfoUIModel(
        appName = appName,
        appPlayingImage = appPlayingImage
    )
}