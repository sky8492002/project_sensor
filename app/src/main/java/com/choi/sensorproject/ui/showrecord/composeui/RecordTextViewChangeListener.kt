package com.choi.sensorproject.ui.showrecord.composeui

import com.choi.sensorproject.ui.model.SensorRecordUIModel

interface RecordTextViewChangeListener {

    fun onReset()
    fun onCurSensorRecordChange(model: SensorRecordUIModel)
}