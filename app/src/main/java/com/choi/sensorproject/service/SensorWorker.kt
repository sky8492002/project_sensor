package com.choi.sensorproject.service

import android.Manifest
import android.annotation.SuppressLint
import android.app.ForegroundServiceStartNotAllowedException
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.choi.sensorproject.domain.usecase.InsertSensorRecordUseCase
import com.example.sensorproject.R
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
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

    private val NOTIFICATION_ID = 1

    var curXrAngle : Float = 0f
    var curZrAngle : Float = 0f

    @SuppressLint("SimpleDateFormat")
    val timeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

    init {
        val sensorManager = ContextCompat.getSystemService(context, SensorManager:: class.java) as SensorManager
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY),
            SensorManager.SENSOR_DELAY_FASTEST)
    }

    @RequiresApi(Build.VERSION_CODES.S)
    override suspend fun doWork(): Result{
        return try {
            coroutineScope {
                withContext(Dispatchers.IO){
                    for (i in 1..900){
                        // 포그라운드 서비스에서 작업을 실행
                        // 프로세스를 활성 상태로 유지해야 한다는 신호를 OS에 제공하여 작업이 OS에 의해 중단되는 것을 방지
                        setForeground(getForegroundInfo())
                        val currentTime : Long = System.currentTimeMillis()
                        insertSensorRecordUseCase(curXrAngle, curZrAngle, timeFormat.format(currentTime), "")
                        delay(1000)
                    }
                }
            }
            Result.success()
        } catch(e: ForegroundServiceStartNotAllowedException) {
            Result.failure()
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    override suspend fun getForegroundInfo(): ForegroundInfo {

        val notification = NotificationCompat.Builder(applicationContext, "channel_id")
            .setContentTitle("title")
            .setTicker("title")
            .setContentText("$curXrAngle $curZrAngle")
            .setSmallIcon(R.drawable.phone)
            .setOngoing(true)
            .setChannelId(createNotificationChannel("channel_id", "title").id)
            .build()

        // 안드로이드 13 이상은 알림 권한 부여 필요
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            applicationContext.getSystemService(NotificationManager::class.java)
                .notify(1, notification)
        }

        return ForegroundInfo(
            NOTIFICATION_ID, notification
        )
    }
    private fun createNotificationChannel(
        channelId: String,
        name: String
    ): NotificationChannel {
        return NotificationChannel(
            channelId, name, NotificationManager.IMPORTANCE_LOW
        ).also { channel ->
            applicationContext.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    @SuppressLint("SwitchIntDef")
    @RequiresApi(Build.VERSION_CODES.R)
    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            val x = event.values[0].toDouble()
            val y = event.values[1].toDouble()
            val z = event.values[2].toDouble()
            val r = Math.sqrt(x.pow(2) + y.pow(2) + z.pow(2))

            // 화면 방향이 기본(충전포트가 아래)일 때의 xtAngle, zrAngle
            val baseXrAngle = (90 - Math.acos(x / r) * 180 / Math.PI).toFloat()
            val baseZrAngle = (90 - Math.acos(z / r) * 180 / Math.PI).toFloat()

            curXrAngle = baseXrAngle
            curZrAngle = baseZrAngle

            // 화면 방향이 상하좌우로 바뀔 때 중력좌표축이 달라지는 것을 고려 (세로모드 고정일 시 필요 x?)
//            when(applicationContext.getSystemService<DisplayManager>()?.getDisplay(Display.DEFAULT_DISPLAY)?.rotation){
//                Surface.ROTATION_0 ->{
//                    curXrAngle = baseXrAngle
//                    curZrAngle = baseZrAngle
//                }
//                Surface.ROTATION_90 ->{
//                    curXrAngle = -baseZrAngle
//                    curZrAngle = baseXrAngle
//                }
//                Surface.ROTATION_180 ->{
//                    curXrAngle = -baseXrAngle
//                    curZrAngle = -baseZrAngle
//                }
//                Surface.ROTATION_270 ->{
//                    curXrAngle = baseZrAngle
//                    curZrAngle = -baseXrAngle
//                }
//            }
        }
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {

    }
}