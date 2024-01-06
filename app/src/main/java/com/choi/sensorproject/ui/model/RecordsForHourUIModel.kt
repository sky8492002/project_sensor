package com.choi.sensorproject.ui.model

// 한 시간의 데이터를 모아서 관리
data class RecordsForHourUIModel(
    val date: String,
    val hour: String,
    val records: List<SensorRecordUIModel>
)