package com.choi.sensorproject.domain.usecase.appinfo

import com.choi.sensorproject.domain.mapper.toUIModel
import com.choi.sensorproject.domain.mapper.toUIModelsFlow
import com.choi.sensorproject.domain.repository.AppInfoRepository
import com.choi.sensorproject.ui.model.AppInfoUIModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAppInfoUseCase @Inject constructor(
    private val repository: AppInfoRepository
){
    suspend fun getAppInfo(appName: String): AppInfoUIModel{
        return repository.getAppInfo(appName).toUIModel()
    }

    suspend fun getAllAppInfos(): Flow<List<AppInfoUIModel>> {
        return repository.getAllAppInfos().toUIModelsFlow()
    }
}