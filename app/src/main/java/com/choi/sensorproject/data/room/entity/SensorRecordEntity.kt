package com.choi.sensorproject.data.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.choi.sensorproject.data.room.OrientationTypeConverters
import com.choi.sensorproject.service.Orientation
import java.time.LocalDateTime

@Entity(tableName = "sensor_record")
data class SensorRecordEntity (
    @PrimaryKey(autoGenerate = true)
    val sensorAngleId: Long = 0, // 자동 id 생성
    @ColumnInfo(name = "x_angle")
    val xAngle: Float,
    @ColumnInfo(name = "z_angle")
    val zAngle: Float,
    @ColumnInfo(name = "orientation")
    @field:TypeConverters(OrientationTypeConverters::class)
    val orientation: Orientation,
    @ColumnInfo(name = "record_time")
    val recordTime: String,
    @ColumnInfo(name = "running_app_name")
    val runningAppName: String,
    @ColumnInfo(name = "is_screen_on", defaultValue = "1")
    val isScreenOn: Boolean,
)