package com.choi.sensorproject.domain.repository

import com.choi.sensorproject.domain.model.AppInfoModel

interface AppInfoRepository {

    suspend fun insertAppInfo(appInfoModel: AppInfoModel)

    suspend fun getAppInfo(appName: String): AppInfoModel
}