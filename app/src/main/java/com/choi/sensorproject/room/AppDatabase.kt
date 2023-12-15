package com.choi.sensorproject.room

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.choi.sensorproject.room.dao.SensorRecordDao
import com.choi.sensorproject.room.entity.SensorAngle

@Database(
    entities = [
        SensorAngle:: class,
    ],
    version = 1
)
@TypeConverters(RoomConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun sensorRecordDao(): SensorRecordDao
}