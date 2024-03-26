package com.choi.sensorproject.ui.showrecord.opngl

import android.content.Context
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import android.util.Log

class CustomGLSurfaceView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : GLSurfaceView(context, attrs) {

    private val renderer: CustomGLRenderer

    init {
        // renderer 설정, 배경 투명하게 설정
        setEGLContextClientVersion(2)
        renderer = CustomGLRenderer(context)
        setEGLConfigChooser( 8, 8, 8, 8, 16, 0)
        holder.setFormat(PixelFormat.RGBA_8888)
        holder.setFormat(PixelFormat.TRANSLUCENT)
        setZOrderOnTop(true)
        setRenderer(renderer)
    }

    fun changePhoneAngle(dx: Float, dy: Float, dz: Float){
        renderer.resetPhoneMatrix()
        renderer.rotatePhone(dx, dy, dz)
    }

    fun changePinLocation(dx: Float, dy:Float, dz: Float){
        renderer.resetPinMatrix()
        renderer.translatePin(dx, dy, dz)
    }

    // 출력되는 이미지 변경을 위해 사용
    fun changeAppPlayingImage(appPlayingImage: Bitmap?){
        renderer.changeAppPlayingImage(appPlayingImage)
    }

    fun changeAppIcon(appIcon: Bitmap?){
        renderer.changeAppIcon(appIcon)
    }

}