package com.choi.sensorproject.domain.model

import java.time.LocalDateTime

data class SensorRecordModel(
    val xrAngle: Float,
    val zrAngle: Float,
    val recordTime: String,
    val runningAppName: String
)