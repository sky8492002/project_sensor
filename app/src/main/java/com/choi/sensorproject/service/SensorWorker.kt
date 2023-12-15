package com.choi.sensorproject.service

import android.annotation.SuppressLint
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import androidx.core.content.ContextCompat.getSystemService
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.choi.sensorproject.domain.usecase.InsertSensorRecordUseCase
import com.choi.sensorproject.room.RoomManager
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import javax.inject.Inject
import kotlin.math.pow

// 의존성 주입한 usecase를 인자로 넣을 시 worker가 실행되지 않는 문제 발생
// context, params 외의 인자를 받기 위해 WorkerFactory를 상속받아 수정하는 작업이 없었기 때문
// HiltWorkerFactory가 대신 해주어 해결 (build.gradle에 androidx.hilt:hilt-compiler가 추가되었을 때만 작동)

@HiltWorker
class SensorWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private var insertSensorRecordUseCase: InsertSensorRecordUseCase
)  : CoroutineWorker(context, params), SensorEventListener {

    var curXAngle : Float = 0f
    var curYAngle: Float = 0f

    @SuppressLint("SimpleDateFormat")
    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

    init {
        val sensorManager = getSystemService(context, SensorManager:: class.java) as SensorManager
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY),
            SensorManager.SENSOR_DELAY_FASTEST)
    }

    override suspend fun doWork(): Result = coroutineScope {
        withContext(Dispatchers.IO){
            for (i in 1..900){
                val currentTime : Long = System.currentTimeMillis()
                insertSensorRecordUseCase(curXAngle, curYAngle, dateFormat.format(currentTime), "")
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