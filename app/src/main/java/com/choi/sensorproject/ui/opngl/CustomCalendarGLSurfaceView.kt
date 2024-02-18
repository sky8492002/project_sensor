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
import kotlin.math.abs

class CustomCalendarGLSurfaceView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : GLSurfaceView(context, attrs) {

    val renderer: CustomCalendarGLRenderer

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

    private var firstTouchY: Float? = null
    private var lastTouchY: Float? = null
    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val curX = event.getX()
        val curY = event.getY()
        when (event.getAction()) {
            MotionEvent.ACTION_DOWN ->{
                firstTouchY = curY
            }
            MotionEvent.ACTION_MOVE ->{
                lastTouchY?.let{
                    renderer.dragY(curY - it)
                }
                lastTouchY = curY
            }
            MotionEvent.ACTION_UP -> {
                // 드레그 시에는 날짜 선택 반응 없도록 설정 (드래그는 첫 터치와 현재 터치로 판정)
                firstTouchY?.let{
                    if(abs(curY - it) < 10){
                        renderer.selectTouchedDate(curX, curY)
                    }
                }
                firstTouchY = null
                lastTouchY = null
            }
        }
        return true
    }


}