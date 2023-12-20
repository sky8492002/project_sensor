package com.choi.sensorproject.ui.model

import com.choi.sensorproject.domain.model.SensorRecordModel

// 한 시간의 데이터를 모아서 관리
data class RecordsForHourModel(
    val date: String,
    val hour: Int,
    val records: MutableList<SensorRecordModel>
)