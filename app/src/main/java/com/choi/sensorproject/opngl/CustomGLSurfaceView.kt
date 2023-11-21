package com.choi.sensorproject.opngl

import android.annotation.SuppressLint
import android.content.Context
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import android.view.MotionEvent

class CustomGLSurfaceView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : GLSurfaceView(context, attrs) {

    private val renderer: CustomGLRenderer

    init {
        setEGLContextClientVersion(2)
        renderer = CustomGLRenderer(context)
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

}