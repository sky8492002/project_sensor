package com.choi.sensorproject.ui.showrecord.composeui.listener

import com.choi.sensorproject.ui.showrecord.composeui.SensorRecordLogic

interface LazyRowViewChangeListener {
    fun onRefresh(initPageDate: String)

    fun onForceScrollTypeChange(forceScrollType: SensorRecordLogic.ForceScrollType)
}