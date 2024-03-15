package com.choi.sensorproject.ui.showrecord.composeui

import com.choi.sensorproject.ui.model.RecordsForHourUIModel

interface BalanceViewChangeListener {
    fun onCurRecordsForHourChange(model: RecordsForHourUIModel)
}