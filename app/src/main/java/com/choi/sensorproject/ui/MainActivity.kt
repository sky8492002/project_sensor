package com.choi.sensorproject.ui

import android.Manifest
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.sensorproject.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 세로 모드 고정
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        // 안드로이드 13 이상은 알림 권한 부여 필요
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            checkNotificationPermission()
        }
        setContentView(R.layout.activity_main)
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun checkNotificationPermission() {
        val notificationPermission = Manifest.permission.POST_NOTIFICATIONS

        val requestNotificationPermission =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted->
                if (isGranted){
                    Toast.makeText(this, "알림 권한이 허용되었습니다.", Toast.LENGTH_SHORT).show()
                }
                else{
                    Toast.makeText(this, "알림 권한이 거부되었습니다.", Toast.LENGTH_SHORT).show()
                }

            }

        when {
            // 권한을 허용한 경우
            ContextCompat.checkSelfPermission(applicationContext, notificationPermission) == PackageManager.PERMISSION_GRANTED -> {
                Toast.makeText(this, "알림 권한이 이미 허용되었습니다.", Toast.LENGTH_SHORT).show()
            }
            // 사용자가 권한 요청을 명시적으로 거부한 경우
            ActivityCompat.shouldShowRequestPermissionRationale(this, notificationPermission) -> {
                // 권한 설정 화면으로 이동
                Toast.makeText(this, "알림 권한이 필요합니다. 설정 화면으로 이동합니다.", Toast.LENGTH_SHORT).show()
                val intent: Intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).setData(
                    Uri.parse("package:" + this.packageName))
                startActivity(intent)
                this.finish()
            }
            // 사용자가 권한 요청을 처음 보거나, 다시 묻지 않음 선택한 경우
            else -> {
                requestNotificationPermission.launch(notificationPermission)
            }
        }
    }

}