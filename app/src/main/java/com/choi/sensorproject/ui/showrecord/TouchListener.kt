package com.choi.sensorproject.ui.showrecord

import com.choi.sensorproject.ui.model.SensorRecordUIModel

interface TouchListener {
    fun onSensorRecordTouch(sensorRecordUIModel: SensorRecordUIModel)
}