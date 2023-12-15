package com.choi.sensorproject.domain.model

import java.time.LocalDateTime

data class SensorRecordModel(
    val xAngle: Float,
    val yAngle: Float,
    val recordTime: String,
    val runningAppName: String
)