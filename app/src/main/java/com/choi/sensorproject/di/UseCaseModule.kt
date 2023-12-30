package com.choi.sensorproject.di

import com.choi.sensorproject.domain.repository.AppInfoRepository
import com.choi.sensorproject.domain.repository.SensorRecordRepository
import com.choi.sensorproject.domain.usecase.appinfo.GetAppInfoUseCase
import com.choi.sensorproject.domain.usecase.sensor.GetSensorRecordsUseCase
import com.choi.sensorproject.domain.usecase.appinfo.InsertAppInfoUseCase
import com.choi.sensorproject.domain.usecase.sensor.InsertSensorRecordUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class UseCaseModule {
    @Provides
    @Singleton
    fun provideInsertSensorRecordUseCase(
        sensorRecordRepository: SensorRecordRepository
    ): InsertSensorRecordUseCase {
        return InsertSensorRecordUseCase(sensorRecordRepository)
    }
    @Provides
    @Singleton
    fun provideGetSensorRecordUseCase(
        sensorRecordRepository: SensorRecordRepository
    ): GetSensorRecordsUseCase {
        return GetSensorRecordsUseCase(sensorRecordRepository)
    }
    @Provides
    @Singleton
    fun provideInsertAppInfoUseCase(
        appInfoRepository: AppInfoRepository
    ): InsertAppInfoUseCase {
        return InsertAppInfoUseCase(appInfoRepository)
    }
    @Provides
    @Singleton
    fun provideGetAppInfoInfoUseCase(
        appInfoRepository: AppInfoRepository
    ): GetAppInfoUseCase {
        return GetAppInfoUseCase(appInfoRepository)
    }

}