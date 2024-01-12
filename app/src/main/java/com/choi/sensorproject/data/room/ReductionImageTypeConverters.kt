package com.choi.sensorproject.data.room

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.room.TypeConverter
import java.io.ByteArrayOutputStream

// 특정 type을 room database에 저장하는 법을 알려주기 위해 필요
class ReductionImageTypeConverters {


    // Bitmap을 저장 / 불러오기 위해 필요한 두가지 TypeConverter
    @TypeConverter
    fun toByteArray(bitmap: Bitmap?): ByteArray {
        val outputStream = ByteArrayOutputStream()
        bitmap?.let{
            // 1mb보다 큰 이미지를 저장할 때 앱이 crash 되기 때문에 크기를 1/4로, 품질을 30% 수준으로 압축하여 저장
            val resizedBitmap = Bitmap.createScaledBitmap(bitmap, it.width / 2, it.height / 2, true)
            resizedBitmap.compress(Bitmap.CompressFormat.PNG, 30, outputStream) // quality: 기존의 N%로 압축
        }
        return outputStream.toByteArray()
    }

    @TypeConverter
    fun toBitmap(byteArray: ByteArray): Bitmap?{
        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size) // offset: decoder가 구문 분석을 시작하는 위치(배열의 시작점)
    }

}