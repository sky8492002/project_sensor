package com.choi.sensorproject.di

import com.choi.sensorproject.data.datasource.AppInfoDataSource
import com.choi.sensorproject.data.datasource.AppInfoDataSourceImpl
import com.choi.sensorproject.data.datasource.SensorRecordDataSource
import com.choi.sensorproject.data.repository.AppInfoRepositoryImpl
import com.choi.sensorproject.data.repository.SensorRecordRepositoryImpl
import com.choi.sensorproject.domain.repository.AppInfoRepository
import com.choi.sensorproject.domain.repository.SensorRecordRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class RepositoryModule {

    @Provides
    @Singleton
    fun provideSensorRecordRepository(
        sensorRecordDataSource: SensorRecordDataSource
    ): SensorRecordRepository {
        return SensorRecordRepositoryImpl(sensorRecordDataSource)
    }

    @Provides
    @Singleton
    fun provideAppInfoRepository(
        appInfoDataSource: AppInfoDataSource
    ): AppInfoRepository{
        return AppInfoRepositoryImpl(appInfoDataSource)
    }
}