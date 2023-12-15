package com.choi.sensorproject.room

import android.util.Log
import javax.inject.Inject

class RoomManager @Inject constructor(
    private val db: AppDatabase
){
    fun recordAngles(xAngle: Float, yAngle:Float){
        Log.d("angle", "$xAngle, $yAngle")
    }
}