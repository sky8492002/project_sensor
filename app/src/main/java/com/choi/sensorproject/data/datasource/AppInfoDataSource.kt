package com.choi.sensorproject.data.datasource

import com.choi.sensorproject.domain.model.AppInfoModel

interface AppInfoDataSource {
    suspend fun insertAppInfo(appInfoModel: AppInfoModel)
    suspend fun getAppInfo(appName: String): AppInfoModel
}