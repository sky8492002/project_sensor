package com.choi.sensorproject.data.datasource

import com.choi.sensorproject.data.mapper.toEntity
import com.choi.sensorproject.data.mapper.toModel
import com.choi.sensorproject.domain.model.AppInfoModel
import com.choi.sensorproject.room.AppDatabase
import javax.inject.Inject

class AppInfoDataSourceImpl @Inject constructor(
    private val appDatabase: AppDatabase
): AppInfoDataSource {
    override suspend fun insertAppInfo(appInfoModel: AppInfoModel) {
        appDatabase.appInfoDao().insertAppInfo(appInfoModel.toEntity())
    }

    override suspend fun getAppInfo(appName: String): AppInfoModel {
        return appDatabase.appInfoDao().getAppInfo(appName).toModel()
    }
}