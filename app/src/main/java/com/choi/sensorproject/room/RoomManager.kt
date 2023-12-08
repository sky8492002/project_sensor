package com.choi.sensorproject.room

import android.util.Log

object RoomManager {

    fun recordAngles(xAngle: Float, yAngle:Float){
        Log.d("angle", "$xAngle, $yAngle")
    }
}