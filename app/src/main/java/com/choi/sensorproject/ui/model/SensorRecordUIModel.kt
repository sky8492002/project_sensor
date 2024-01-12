package com.choi.sensorproject.ui.model

import com.choi.sensorproject.service.Orientation

data class SensorRecordUIModel(
    val xAngle: Float,
    val zAngle: Float,
    val orientation: Orientation,
    val recordTime: String,
    val runningAppName: String,
    val isScreenOn: Boolean
)