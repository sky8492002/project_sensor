package com.choi.sensorproject.room.entity

import android.graphics.Bitmap
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_info")
data class AppInfoEntity (
    @PrimaryKey
    @ColumnInfo(name = "app_name")
    val appName: String,
    @ColumnInfo(name = "app_playing_image")
    val appPlayingImage: Bitmap
)