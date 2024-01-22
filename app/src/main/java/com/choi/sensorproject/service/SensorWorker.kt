package com.choi.sensorproject.service

import android.annotation.SuppressLint
import android.app.ForegroundServiceStartNotAllowedException
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.res.Configuration.ORIENTATION_LANDSCAPE
import android.content.res.Configuration.ORIENTATION_PORTRAIT
import android.content.res.Resources
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.hardware.display.DisplayManager
import android.os.Build
import android.os.PowerManager
import android.util.Log
import android.view.Display
import android.view.Surface
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.choi.sensorproject.domain.usecase.sensor.InsertSensorRecordUseCase
import com.choi.sensorproject.ui.model.SensorRecordUIModel
import com.example.sensorproject.R
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.lang.Math.abs
import java.lang.Math.pow
import java.text.SimpleDateFormat
import java.util.Calendar
import kotlin.math.pow


// 의존성 주입한 usecase를 인자로 넣을 시 worker가 실행되지 않는 문제 발생
// context, params 외의 인자를 받기 위해 WorkerFactory를 상속받아 수정하는 작업이 없었기 때문
// HiltWorkerFactory가 대신 해주어 해결 (build.gradle에 androidx.hilt:hilt-compiler가 추가되었을 때만 작동)

@HiltWorker
class SensorWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted params: WorkerParameters,
    private var insertSensorRecordUseCase: InsertSensorRecordUseCase
)  : CoroutineWorker(context, params), SensorEventListener {

    private val NOTIFICATION_ID = 1

    var sensorManager: SensorManager
    var usageStatsManager: UsageStatsManager
    var powerManager: PowerManager

    private var curXAngle : Float = 0f
    private var curZAngle : Float = 0f
    private var curAppPackageName: String = "none"
    private var curOrientation: Orientation = Orientation.Portrait

    @SuppressLint("SimpleDateFormat")
    val timeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

    init {
        // 중력 정보를 불러오기 위해 필요
        sensorManager = ContextCompat.getSystemService(context, SensorManager:: class.java) as SensorManager
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY),
            SensorManager.SENSOR_DELAY_FASTEST)

        // 실행중인 앱 정보를 불러오기 위해 필요
        usageStatsManager =
            context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

        // 화면 켜짐/꺼짐 정보를 불러오기 위해 필요
        powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager

    }

    @RequiresApi(Build.VERSION_CODES.S)
    override suspend fun doWork(): Result{
        return try {
            coroutineScope {
                withContext(Dispatchers.IO){
                    updateCurAppPackageName()
                    //for (i in 1..3600){
                    while(true){
                        // 포그라운드 서비스에서 작업을 실행
                        // 프로세스를 활성 상태로 유지해야 한다는 신호를 OS에 제공하여 작업이 OS에 의해 중단되는 것을 방지
                        val currentTimeMillis : Long = System.currentTimeMillis()
                        updateCurAppPackageName()
                        val curSensorRecordUIModel =
                            SensorRecordUIModel(curXAngle, curZAngle, curOrientation, timeFormat.format(currentTimeMillis), curAppPackageName, powerManager.isInteractive)
                        insertSensorRecordUseCase(curSensorRecordUIModel)
                        setForeground(getForegroundInfo())
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
            .setContentTitle("sensor_project")
            .setTicker("sensor_project")
            .setContentText("$curXAngle $curZAngle $curAppPackageName")
            .setSmallIcon(R.drawable.phone)
            .setOngoing(true)
            .setChannelId(createNotificationChannel("channel_id", "sensor_project").id)
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
            var x = event.values[0].toDouble()
            var y = event.values[1].toDouble()
            val z = event.values[2].toDouble()

            // xAngle 범위는 -180 ~ 180, zAngle 범위는 -90 ~ 90
            // xAngle 범위가 -180 ~ 180인 이유: 가로 상태에서 왼쪽, 오른쪽으로 기울였을 경우 xAngle 값이 서로 달라야 하기 때문
            // zAngle 범위가 -90 ~ 90인 이유: 가로 상태에서 왼쪽, 오른쪽으로 기울였을 경우 zAngle 값이 서로 동일해야 하기 때문
            // 세로 상태에서는 기기를 거꾸로 잡지 않는 한 xAngle이 -90 ~ 90 사이에 있음, zAngle 범위가 -180 ~ 180이었어도 zAngle이 -90 ~ 90 사이에 있음
            val sqrtXY = Math.sqrt(x.pow(2) + y.pow(2) )
            var baseXAngle = (90 - Math.acos(x / sqrtXY) * 180 / Math.PI).toFloat()
            if(y < 0) baseXAngle = 180 - baseXAngle
            else if(y > 0 && x < 0) baseXAngle = 360 + baseXAngle

            curXAngle = if(baseXAngle <= 180 ) baseXAngle else baseXAngle - 360


            var baseZAngle = ((z / 9.8) * 90).toFloat()
            if(y < 0 && abs(curXAngle) < 90) baseZAngle = 180 - baseZAngle

            curZAngle = baseZAngle

            Log.d("angle", curXAngle.toString() + " " + curZAngle.toString() + " "+ x.toString() + " " + y.toString() + " " + z.toString())

            // 가로 모드인지 세로 모드인지 확인 후 기록
            when(Resources.getSystem().configuration.orientation){
                ORIENTATION_LANDSCAPE ->{
                    curOrientation = Orientation.LandScape
                }
                ORIENTATION_PORTRAIT -> {
                    curOrientation = Orientation.Portrait
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun updateCurAppPackageName(){
        // 문제점: queryEvents(start, end) 일 때 end-start 시간 후 사용 중인 앱이 바뀌지 없으면 usageEvents가 event를 갖지 않게 됨
        // 현재로선 예상되는 앱 최대 연속 사용시간 보다 길게 잡아야 함 (1일)
        val calendar = Calendar.getInstance()
        val endTime = calendar.timeInMillis
        calendar.add(Calendar.DATE, -1)
        val startTime = calendar.timeInMillis

        var cacheAppPackageName = "none"

        val usageEvents = usageStatsManager.queryEvents(startTime, endTime)
        val curEvent = UsageEvents.Event()
        var eventCount = 0
        while (usageEvents.hasNextEvent()) {
            usageEvents.getNextEvent(curEvent)
            eventCount +=1

            if(curEvent.getEventType() == UsageEvents.Event.ACTIVITY_RESUMED){
                cacheAppPackageName = curEvent.packageName
            }
        }
        Log.d("시간 event 체크", timeFormat.format(startTime) + " " + timeFormat.format(endTime) + " " + eventCount)
        curAppPackageName = cacheAppPackageName

    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {

    }
}

enum class Orientation{
    Portrait, LandScape
}