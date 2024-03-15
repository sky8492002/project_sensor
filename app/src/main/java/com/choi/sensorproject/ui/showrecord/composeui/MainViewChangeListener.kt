package com.choi.sensorproject.ui.showrecord.composeui

import androidx.paging.PagingData
import com.choi.sensorproject.ui.model.RecordsForHourUIModel
import com.choi.sensorproject.ui.model.SensorRecordUIModel

interface MainViewChangeListener {

    fun onRecordPagingDataChange(pagingData: PagingData<RecordsForHourUIModel>)
    fun onCurSensorRecordChange(model: SensorRecordUIModel)
    fun onCurRecordsForHourChange(model: RecordsForHourUIModel)
}