package com.choi.sensorproject.room

import androidx.room.TypeConverter
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

// 특정 type을 room database에 저장하는 법을 알려주기 위해 필요
class RoomConverters {


    // LocalDateTime을 저장 / 불러오기 위해 필요한 두가지 TypeConverter
    @TypeConverter
    fun toLocalDateTime(timestamp: Long): LocalDateTime {
        return Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault()).toLocalDateTime()
    }

    @TypeConverter
    fun toTimestamp(date: LocalDateTime): Long {
        return date.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }

}