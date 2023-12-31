package com.choi.sensorproject.domain.model

// 한 시간의 데이터를 모아서 관리
data class RecordsForHourModel(
    val date: String,
    val hour: String,
    val records: MutableList<SensorRecordModel>
)