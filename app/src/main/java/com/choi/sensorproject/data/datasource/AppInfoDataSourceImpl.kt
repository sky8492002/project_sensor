package com.choi.sensorproject.data.datasource

import com.choi.sensorproject.data.mapper.toEntity
import com.choi.sensorproject.data.mapper.toModel
import com.choi.sensorproject.data.mapper.toModels
import com.choi.sensorproject.data.mapper.toModelsFlow
import com.choi.sensorproject.domain.model.AppInfoModel
import com.choi.sensorproject.room.AppDatabase
import com.choi.sensorproject.room.entity.AppInfoEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class AppInfoDataSourceImpl @Inject constructor(
    private val appDatabase: AppDatabase
): AppInfoDataSource {
    override suspend fun insertAppInfo(appInfoModel: AppInfoModel) {
        appDatabase.appInfoDao().insertAppInfo(appInfoModel.toEntity())
    }

    override suspend fun getAppInfo(appName: String): List<AppInfoModel> {
        return appDatabase.appInfoDao().getAppInfo(appName).toModels()
    }

    override suspend fun getAllAppInfos(): Flow<List<AppInfoModel>> {
        return appDatabase.appInfoDao().getAllAppInfos().toModelsFlow()
    }
}