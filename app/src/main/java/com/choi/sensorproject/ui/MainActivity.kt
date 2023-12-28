package com.choi.sensorproject.ui

import android.Manifest
import android.app.AppOpsManager
import android.app.AppOpsManager.OPSTR_GET_USAGE_STATS
import android.content.Context
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
import java.security.Permission

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

        // 갤러리 접근 권한 체크 필요
        checkGalleryPermission()

        // 사용정보 접근 권한 체크 필요
        checkUsageStatsPermission()



        setContentView(R.layout.activity_main)
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun checkNotificationPermission() {
        val notificationPermission = Manifest.permission.POST_NOTIFICATIONS

        val moveToSettingIntent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).setData(
            Uri.parse("package:$packageName"))

        checkPermission(notificationPermission,"알림", moveToSettingIntent)
    }

    private fun checkGalleryPermission(){
        val galleryPermission = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            Manifest.permission.READ_MEDIA_IMAGES
        } else{
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        val moveToSettingIntent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).setData(
            Uri.parse("package:$packageName"))

        checkPermission(galleryPermission, "갤러리 접근", moveToSettingIntent)
    }

    private fun checkUsageStatsPermission(){
        val usageStatsPermission = OPSTR_GET_USAGE_STATS

        val moveToSettingIntent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)

        checkSpecialPermission(usageStatsPermission, "사용정보 접근", moveToSettingIntent)
    }

    private fun checkPermission(permission: String, permissionName: String, moveToSettingIntent: Intent){

        // 권한 허용을 요청하는 Dialog 띄우고 결과에 따라 다른 메세지를 보여줌
        val requestPermission =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted->
                if (isGranted){
                    Toast.makeText(this, "$permissionName 권한이 허용되었습니다.", Toast.LENGTH_SHORT).show()
                }
                else{
                    Toast.makeText(this, "$permissionName 권한이 거부되었습니다.", Toast.LENGTH_SHORT).show()
                    this.finish()
                }

            }

        when {
            // 권한을 이미 허용한 경우
            ContextCompat.checkSelfPermission(applicationContext, permission) == PackageManager.PERMISSION_GRANTED -> {
                Toast.makeText(this, "$permissionName 권한이 이미 허용되었습니다.", Toast.LENGTH_SHORT).show()
            }
            // 사용자가 Dialog로 띄워진 권한 요청을 명시적으로 거부한 적이 있을 경우
            ActivityCompat.shouldShowRequestPermissionRationale(this, permission) -> {
                // 권한 설정 화면으로 이동
                Toast.makeText(this, "$permissionName 권한이 필요합니다. 설정 화면으로 이동합니다.", Toast.LENGTH_SHORT).show()
                startActivity(moveToSettingIntent)
                this.finish()
            }
            else -> {
                requestPermission.launch(permission)
            }
        }
    }

    private fun checkSpecialPermission(permission: String, permissionName: String, moveToSettingIntent: Intent){
        val appOps = getSystemService(APP_OPS_SERVICE) as AppOpsManager
        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            appOps.unsafeCheckOpNoThrow(
                permission,
                android.os.Process.myUid(), packageName
            )
        } else {
            appOps.checkOpNoThrow(
                permission,
                android.os.Process.myUid(), packageName
            )
        }

        if(mode != AppOpsManager.MODE_ALLOWED){
            Toast.makeText(this, "$permissionName 권한이 필요합니다. 설정 화면으로 이동합니다.", Toast.LENGTH_SHORT).show()
            startActivity(moveToSettingIntent)
            this.finish()
        }
        else{
            Toast.makeText(this, "$permissionName 권한이 이미 허용되었습니다.", Toast.LENGTH_SHORT).show()
        }
    }

}