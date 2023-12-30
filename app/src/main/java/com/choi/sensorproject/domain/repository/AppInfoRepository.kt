package com.choi.sensorproject.domain.repository

import com.choi.sensorproject.domain.model.AppInfoModel
import kotlinx.coroutines.flow.Flow

interface AppInfoRepository {

    suspend fun insertAppInfo(appInfoModel: AppInfoModel)

    suspend fun getAppInfo(appName: String): AppInfoModel

    suspend fun getAllAppInfos(): Flow<List<AppInfoModel>>
}