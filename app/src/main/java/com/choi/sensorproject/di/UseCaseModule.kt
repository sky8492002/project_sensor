package com.choi.sensorproject.di

import com.choi.sensorproject.domain.repository.SensorRecordRepository
import com.choi.sensorproject.domain.usecase.GetSensorRecordsUseCase
import com.choi.sensorproject.domain.usecase.InsertSensorRecordUseCase
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
    ): InsertSensorRecordUseCase{
        return InsertSensorRecordUseCase(sensorRecordRepository)
    }
    @Provides
    @Singleton
    fun provideGetSensorRecordUseCase(
        sensorRecordRepository: SensorRecordRepository
    ): GetSensorRecordsUseCase {
        return GetSensorRecordsUseCase(sensorRecordRepository)
    }
}