package com.choi.sensorproject.service

import android.app.Service
import android.content.Intent
import android.os.IBinder


class SensorService: Service() {
    override fun onBind(p0: Intent?): IBinder? {
        return null
    }
}