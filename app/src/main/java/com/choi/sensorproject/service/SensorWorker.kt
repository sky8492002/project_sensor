package com.choi.sensorproject.service

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import androidx.work.CoroutineWorker
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.choi.sensorproject.room.RoomManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlin.math.pow

class SensorWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params), SensorEventListener {

    var curXAngle : Float = 0f
    var curYAngle: Float = 0f
    override suspend fun doWork(): Result = coroutineScope {
        withContext(Dispatchers.IO){
            for (i in 1..900){
                RoomManager.recordAngles(curXAngle, curYAngle)
                delay(1000)
            }
        }
        Result.success()
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            val x = event.values[0].toDouble()
            val y = event.values[1].toDouble()
            val z = event.values[2].toDouble()
            val r = Math.sqrt(x.pow(2) + y.pow(2) + z.pow(2))
            curXAngle = (90 - Math.acos(x / r) * 180 / Math.PI).toFloat()
            curYAngle = (90 - Math.acos(y / r) * 180 / Math.PI).toFloat()
        }
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
    }
}