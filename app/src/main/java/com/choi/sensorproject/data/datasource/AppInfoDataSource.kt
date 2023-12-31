package com.choi.sensorproject.data.datasource

import com.choi.sensorproject.domain.model.AppInfoModel
import kotlinx.coroutines.flow.Flow

interface AppInfoDataSource {
    suspend fun insertAppInfo(appInfoModel: AppInfoModel)
    suspend fun getAppInfo(appName: String): List<AppInfoModel>

    suspend fun getAllAppInfos(): Flow<List<AppInfoModel>>
}