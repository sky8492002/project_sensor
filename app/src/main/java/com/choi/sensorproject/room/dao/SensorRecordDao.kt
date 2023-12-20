package com.choi.sensorproject.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.choi.sensorproject.room.entity.SensorRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SensorRecordDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSensorAngle(
        sensorRecordEntity: SensorRecordEntity
    )

    @Query("SELECT * FROM sensor_record WHERE record_time LIKE :pageDate")
    fun getSensorRecords(pageDate: String) : Flow<List<SensorRecordEntity>>
}