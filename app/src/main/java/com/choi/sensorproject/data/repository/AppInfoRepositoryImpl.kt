package com.choi.sensorproject.data.repository

import com.choi.sensorproject.data.datasource.AppInfoDataSource
import com.choi.sensorproject.domain.model.AppInfoModel
import com.choi.sensorproject.domain.repository.AppInfoRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class AppInfoRepositoryImpl @Inject constructor(
    private val appInfoDataSource: AppInfoDataSource
): AppInfoRepository {
    override suspend fun insertAppInfo(appInfoModel: AppInfoModel) {
        appInfoDataSource.insertAppInfo(appInfoModel)
    }

    override suspend fun getAppInfo(appName: String): List<AppInfoModel> {
        return appInfoDataSource.getAppInfo(appName)
    }

    override suspend fun getAllAppInfos(): Flow<List<AppInfoModel>> {
        return appInfoDataSource.getAllAppInfos()
    }
}