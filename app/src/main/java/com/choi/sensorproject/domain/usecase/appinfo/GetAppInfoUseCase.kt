package com.choi.sensorproject.domain.usecase.appinfo

import com.choi.sensorproject.domain.mapper.toUIModel
import com.choi.sensorproject.domain.mapper.toUIModels
import com.choi.sensorproject.domain.mapper.toUIModelsFlow
import com.choi.sensorproject.domain.model.AppInfoModel
import com.choi.sensorproject.domain.repository.AppInfoRepository
import com.choi.sensorproject.ui.model.AppInfoUIModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAppInfoUseCase @Inject constructor(
    private val repository: AppInfoRepository
){
    suspend fun getAppInfo(appName: String): List<AppInfoUIModel>{
        return repository.getAppInfo(appName).toUIModels()
    }

    suspend fun getAllAppInfos(): Flow<List<AppInfoUIModel>> {
        return repository.getAllAppInfos().toUIModelsFlow()
    }
}