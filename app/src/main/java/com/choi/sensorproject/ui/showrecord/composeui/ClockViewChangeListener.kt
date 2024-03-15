package com.choi.sensorproject.ui.showrecord.composeui

import com.choi.sensorproject.ui.model.RecordsForHourUIModel

interface ClockViewChangeListener {
    fun onCurRecordsForHourChange(model: RecordsForHourUIModel)
}