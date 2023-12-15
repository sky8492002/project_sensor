package com.choi.sensorproject.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.choi.sensorproject.room.entity.SensorRecordEntity

@Dao
interface SensorRecordDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSensorAngle(
        sensorRecordEntity: SensorRecordEntity
    )

    @Query("SELECT * FROM sensor_record")
    fun getAllSensorRecords() : List<SensorRecordEntity>
}