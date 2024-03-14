package com.choi.sensorproject.ui.showrecord.composeui

import androidx.compose.runtime.MutableState
import androidx.paging.compose.LazyPagingItems
import com.choi.sensorproject.ui.model.RecordsForHourUIModel
import com.choi.sensorproject.ui.recyclerview.FocusedLayoutManager
import com.choi.sensorproject.ui.recyclerview.RecordsForHourAdapter
import java.text.SimpleDateFormat

object SensorRecordLogic {

    private val dayFormat = SimpleDateFormat("yyyy-MM-dd")
    private val hourFormat = SimpleDateFormat("HH")
    private val timeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    private val minuteFormat = SimpleDateFormat("mm")
    private val secondFormat = SimpleDateFormat("ss")
    fun getScrollPosition(initPageDate: String, items: LazyPagingItems<RecordsForHourUIModel>): Int{
        val curTimeMillis = System.currentTimeMillis()

        // pagingSource의 INIT_PAGE_DATE에 따라 스크롤 위치가 달라짐
        when(initPageDate){
            // 오늘 날짜인 경우 현재 시간으로 스크롤함
            dayFormat.format(curTimeMillis) -> {
                for(index in 0 until items.itemCount){
                    val curModel = items[index]
                    if(curModel != null && curModel.hour.toInt() == hourFormat.format(curTimeMillis).toInt()
                        && curModel.date == initPageDate) {
                        return index
                    }
                }
            }
            // 다른 날짜인 경우 해당 날짜로 스크롤함
            else -> {
                for(index in 0 until items.itemCount){
                    val curModel = items[index]
                    if(curModel != null && curModel.date == initPageDate){
                        return index
                    }
                }
            }
        }
        return 0
    }
}