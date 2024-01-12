package com.choi.sensorproject.domain.model

import com.choi.sensorproject.service.Orientation
import java.time.LocalDateTime

data class SensorRecordModel(
    val xAngle: Float,
    val zAngle: Float,
    val orientation: Orientation,
    val recordTime: String,
    val runningAppName: String,
    val isScreenOn: Boolean
)