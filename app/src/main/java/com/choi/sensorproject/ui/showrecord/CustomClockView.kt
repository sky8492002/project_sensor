package com.choi.sensorproject.ui.showrecord

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

class CustomClockView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    private val rectF = RectF()
    private val paint = Paint()
    private var radius = 0

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = getDefaultSize(suggestedMinimumWidth, widthMeasureSpec)
        val height = getDefaultSize(suggestedMinimumHeight, heightMeasureSpec)

        val min = Math.min(width, height)
        radius = (min - paddingLeft - 90) / 2

        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {

        val centerX = (width.div(2)).toFloat()
        val centerY = (height.div(2)).toFloat()

        // rectF: 도형을 그리는 최대 범위 설정
        rectF.apply {
            set(centerX - radius, centerY - radius, centerX + radius, centerY + radius)
        }

        canvas.drawArc(rectF, -90f, 180f, false, paint.apply {
            color = Color.parseColor("#95a5a6")
            strokeWidth = 20f
            style = Paint.Style.STROKE
        })
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {

        return true
    }

}