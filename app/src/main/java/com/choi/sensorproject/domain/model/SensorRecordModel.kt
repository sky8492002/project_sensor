package com.choi.sensorproject.domain.model

import java.time.LocalDateTime

data class SensorRecordModel(
    val xAngle: Float,
    val zAngle: Float,
    val recordTime: String,
    val runningAppName: String
)