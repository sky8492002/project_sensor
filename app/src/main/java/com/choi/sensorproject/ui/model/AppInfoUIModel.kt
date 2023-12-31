package com.choi.sensorproject.ui.model

import android.graphics.Bitmap

data class AppInfoUIModel(
    val appName: String,
    val appIcon: Bitmap?,
    var appPlayingImage: Bitmap?
)