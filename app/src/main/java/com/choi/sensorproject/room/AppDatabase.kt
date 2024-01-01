package com.choi.sensorproject.room

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.choi.sensorproject.room.dao.AppInfoDao
import com.choi.sensorproject.room.dao.SensorRecordDao
import com.choi.sensorproject.room.entity.AppInfoEntity
import com.choi.sensorproject.room.entity.SensorRecordEntity

@Database(
    entities = [
        SensorRecordEntity:: class,
        AppInfoEntity:: class
    ],
    version = 1
)

abstract class AppDatabase : RoomDatabase() {
    abstract fun sensorRecordDao(): SensorRecordDao
    abstract fun appInfoDao(): AppInfoDao
}
