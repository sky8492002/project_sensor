package com.choi.sensorproject.ui.showrecord.composeui.listener

import androidx.paging.PagingData
import com.choi.sensorproject.ui.model.RecordsForHourUIModel
import com.choi.sensorproject.ui.showrecord.composeui.SensorRecordLogic

interface PagingViewChangeListener {
    fun onRecordPagingDataChange(pagingData: PagingData<RecordsForHourUIModel>)
}