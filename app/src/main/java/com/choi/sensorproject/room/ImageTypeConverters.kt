package com.choi.sensorproject.room

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.room.TypeConverter
import java.io.ByteArrayOutputStream

// 특정 type을 room database에 저장하는 법을 알려주기 위해 필요
class ImageTypeConverters {


    // Bitmap을 저장 / 불러오기 위해 필요한 두가지 TypeConverter
    @TypeConverter
    fun toByteArray(bitmap: Bitmap?): ByteArray {
        val outputStream = ByteArrayOutputStream()
        bitmap?.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        return outputStream.toByteArray()
    }

    @TypeConverter
    fun toBitmap(byteArray: ByteArray): Bitmap?{
        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size) // offset: decoder가 구문 분석을 시작하는 위치(배열의 시작점)
    }

}