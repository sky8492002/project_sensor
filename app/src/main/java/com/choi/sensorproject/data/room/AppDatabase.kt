package com.choi.sensorproject.data.room

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.choi.sensorproject.data.room.dao.AppInfoDao
import com.choi.sensorproject.data.room.dao.SensorRecordDao
import com.choi.sensorproject.data.room.entity.AppInfoEntity
import com.choi.sensorproject.data.room.entity.SensorRecordEntity

@Database(
    entities = [
        SensorRecordEntity:: class,
        AppInfoEntity:: class
    ],
//    autoMigrations = [
//        AutoMigration (from = 4, to = 5)
//    ],
    version = 5,
    exportSchema = false // 다음 버전 migration을 위해 스키마를 저장할 지 결정 (app/schemas 폴더에서 확인 가능)
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun sensorRecordDao(): SensorRecordDao
    abstract fun appInfoDao(): AppInfoDao
}