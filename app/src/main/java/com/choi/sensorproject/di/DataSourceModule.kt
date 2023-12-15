package com.choi.sensorproject.di

import com.choi.sensorproject.data.datasource.SensorRecordDataSource
import com.choi.sensorproject.data.datasource.SensorRecordDataSourceImpl
import com.choi.sensorproject.room.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
@Module
@InstallIn(SingletonComponent::class)
class DataSourceModule{

    @Provides
    @Singleton
    fun provideSensorRecordDataSource(
        appDatabase: AppDatabase
    ): SensorRecordDataSource{
        return SensorRecordDataSourceImpl(appDatabase)
    }
}