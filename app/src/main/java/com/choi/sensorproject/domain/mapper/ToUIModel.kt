package com.choi.sensorproject.domain.mapper

import androidx.paging.PagingData
import androidx.paging.map
import com.choi.sensorproject.domain.model.AppInfoModel
import com.choi.sensorproject.domain.model.RecordsForHourModel
import com.choi.sensorproject.ui.model.AppInfoUIModel
import com.choi.sensorproject.ui.model.RecordsForHourUIModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

fun PagingData<RecordsForHourModel>.toUIModelsPagingData(): PagingData<RecordsForHourUIModel>{
    return this.map{
        it.toUIModel()
    }
}

fun RecordsForHourModel.toUIModel(): RecordsForHourUIModel{
    return RecordsForHourUIModel(
        date = date,
        hour = hour,
        records = records
    )
}


fun AppInfoModel.toUIModel(): AppInfoUIModel{
    return AppInfoUIModel(
        appName = appName,
        appPlayingImage = appPlayingImage
    )
}

fun Flow<List<AppInfoModel>>.toUIModelsFlow(): Flow<List<AppInfoUIModel>> {
    return this.map{
        val appInfoUIModels: MutableList<AppInfoUIModel> = mutableListOf()
        for(model in it){
            appInfoUIModels.add(model.toUIModel())
        }
        appInfoUIModels
    }
}