package com.choi.sensorproject.data.room.entity

import android.graphics.Bitmap
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.choi.sensorproject.data.room.ImageTypeConverters
import com.choi.sensorproject.data.room.ReductionImageTypeConverters

@Entity(tableName = "app_info")
data class AppInfoEntity (
    @PrimaryKey
    @ColumnInfo(name = "app_name")
    val appName: String,
    @ColumnInfo(name = "app_icon")
    @field:TypeConverters(ImageTypeConverters::class)
    val appIcon: Bitmap?,
    @ColumnInfo(name = "app_playing_image")
    @field:TypeConverters(ReductionImageTypeConverters::class)
    val appPlayingImage: Bitmap?
)