package com.choi.sensorproject.ui.showrecord.composeui

import com.choi.sensorproject.ui.model.SensorRecordUIModel

interface OpenGLViewChangeListener {
    fun onCurSensorRecordChange(model: SensorRecordUIModel)

    fun onPhoneAngleChange(phoneAngle: FloatArray)
}