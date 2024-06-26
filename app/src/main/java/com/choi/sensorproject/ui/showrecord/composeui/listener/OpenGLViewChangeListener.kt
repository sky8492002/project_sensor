package com.choi.sensorproject.ui.showrecord.composeui.listener

import com.choi.sensorproject.ui.model.SensorRecordUIModel

interface OpenGLViewChangeListener {

    fun onReset()
    fun onCurSensorRecordChange(model: SensorRecordUIModel)

    fun onPhoneAngleChange(phoneAngle: FloatArray)
}