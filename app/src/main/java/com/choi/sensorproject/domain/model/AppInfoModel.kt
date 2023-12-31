package com.choi.sensorproject.domain.model

import android.graphics.Bitmap

data class AppInfoModel(
    val appName: String,
    val appIcon: Bitmap?,
    var appPlayingImage: Bitmap?
)