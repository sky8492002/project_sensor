package com.choi.sensorproject.di

import com.choi.sensorproject.domain.paging.CustomPagingSource
import com.choi.sensorproject.domain.usecase.GetSensorRecordsUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class PagingSourceModule {
    @Provides
    @Singleton
    fun provideCustomPagingSource(
        getSensorRecordsUseCase: GetSensorRecordsUseCase
    ): CustomPagingSource {
        return CustomPagingSource(getSensorRecordsUseCase)
    }
}