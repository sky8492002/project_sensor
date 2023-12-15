package com.choi.sensorproject.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "sensor_angle")
data class SensorAngle (
    @PrimaryKey
    @ColumnInfo(name = "sensor_angle_id")
    val sensorAngleId: String,
    @ColumnInfo(name = "x_angle")
    val xAngle: Float,
    @ColumnInfo(name = "y_angle")
    val yAngle: Float,
    @ColumnInfo(name = "record_time")
    val recordTime: LocalDateTime,
)