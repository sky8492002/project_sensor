package com.choi.sensorproject.ui.opngl

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.GLU
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent

class CustomCalendarGLSurfaceView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : GLSurfaceView(context, attrs) {

    private val renderer: CustomCalendarGLRenderer

    init {
        // renderer 설정, 배경 투명하게 설정
        setEGLContextClientVersion(2)
        renderer = CustomCalendarGLRenderer(context, resources)
        setEGLConfigChooser( 8, 8, 8, 8, 16, 0)
        holder.setFormat(PixelFormat.RGBA_8888)
        holder.setFormat(PixelFormat.TRANSLUCENT)
        setZOrderOnTop(true)
        setRenderer(renderer)
    }

    fun reset(){
        renderer.resetAllMatrix()
    }


    private var lastTouchY: Float? = null
    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {

        val curX = event.getX()
        val curY = event.getY()
        when (event.getAction()) {
            MotionEvent.ACTION_MOVE ->{
                lastTouchY?.let{
                    renderer.dragY(curY - it)
                }
                lastTouchY = curY
            }
            MotionEvent.ACTION_UP -> {
                renderer.selectTouchedDate(curX, curY)
                lastTouchY = null
            }
        }
        return true
    }


}