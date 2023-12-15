package com.choi.sensorproject.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.choi.sensorproject.room.entity.SensorAngle

@Dao
interface SensorRecordDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSensorAngle(
        sensorAngle: SensorAngle
    )

    @Query("SELECT * FROM sensor_angle")
    fun getAllSensorAngles() : List<SensorAngle>
}