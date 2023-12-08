package com.choi.sensorproject

import android.app.Application
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.choi.sensorproject.service.SensorWorker
import java.util.concurrent.TimeUnit

class SensorApplication: Application() {

    override fun onCreate() {
        super.onCreate()

        initWorkManager()
    }

    // 앱 실행 시 WorkManager 시작
    private fun initWorkManager() {
        val workRequest = PeriodicWorkRequestBuilder<SensorWorker>(15, TimeUnit.MINUTES).build()
        val workManager = WorkManager.getInstance(applicationContext)
        workManager.enqueue(workRequest)
    }
}