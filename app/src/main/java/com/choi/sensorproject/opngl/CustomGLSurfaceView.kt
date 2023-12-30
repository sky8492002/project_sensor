package com.choi.sensorproject.opngl

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import android.view.MotionEvent

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

    private var mPreviousX = 0f
    private var mPreviousY = 0f
    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {

        val x: Float = event.getX()
        val y: Float = event.getY()
        when (event.getAction()) {
            MotionEvent.ACTION_MOVE -> {
                val dx: Float = mPreviousX - x
                val dy: Float = mPreviousY - y
                renderer.rotate(dx, dy)
                requestRender()
            }
        }
        mPreviousX = x
        mPreviousY = y
        return true
    }

    fun changeAngle(dx: Float, dy: Float, dz: Float){
        renderer.resetAngle()
        renderer.rotate(dx, dy, dz)
    }

    // 출력되는 이미지 변경을 위해 사용
    fun changeImage(bitmapImage: Bitmap?){
        renderer.changeImage(bitmapImage)
    }

}