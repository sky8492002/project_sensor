package com.choi.sensorproject.room

import androidx.room.TypeConverter
import com.choi.sensorproject.service.Orientation

class OrientationTypeConverters {
    @TypeConverter
    fun toString(orientation: Orientation): String {
        return orientation.name
    }

    @TypeConverter
    fun toOrientation(name: String): Orientation {
        return Orientation.valueOf(name)
    }
}