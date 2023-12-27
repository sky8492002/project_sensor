package com.choi.sensorproject

import android.app.Application
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorManager
import android.util.Log
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.choi.sensorproject.service.SensorWorker
import dagger.hilt.android.HiltAndroidApp
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidApp
class SensorApplication: Application(), Configuration.Provider {

    override fun onCreate() {
        super.onCreate()

        initWorkManager()
    }

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    // 기본 초기자 대신 hilt가 적용된 workmanager 사용
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder().setWorkerFactory(workerFactory).build()

    // 앱 실행 시 WorkManager 시작
    private fun initWorkManager() {
        val workRequest = PeriodicWorkRequestBuilder<SensorWorker>(1, TimeUnit.HOURS).build()
        val workManager = WorkManager.getInstance(applicationContext)
        workManager.enqueueUniquePeriodicWork(
            "SensorWorker",
            ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,
            workRequest
        )
    }
}