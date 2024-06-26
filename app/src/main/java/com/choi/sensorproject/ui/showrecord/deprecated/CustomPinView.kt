package com.choi.sensorproject.ui.showrecord.deprecated

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PathMeasure
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View

class CustomPinView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    // 0~360 각도에 대응하는 0~3600 시간 범위 내에서 위치 설정
    private var curSec = 0
    private var curAppIcon: Bitmap? = null
    private val paint = Paint()
    private val innerRecF = RectF()
    private var radius = 0
    private var innerRadius = 0
    private var arcStrokeWidth = 100f
    private var curPoint = FloatArray(2)

    fun setPin(sec: Int, appIcon: Bitmap?){
        curSec = sec
        curPoint = getCurPoint()

        curAppIcon = appIcon
        postInvalidate() // 화면을 갱신 (invalidate 앞에 post를 붙이면 ui thread가 아니더라도 ui thread에 요청할 수 있음)

    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = getDefaultSize(suggestedMinimumWidth, widthMeasureSpec)
        val height = getDefaultSize(suggestedMinimumHeight, heightMeasureSpec)

        val min = Math.min(width, height)
        radius = (min - paddingLeft - 90) / 2
        innerRadius = radius - arcStrokeWidth.toInt() / 2

        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {

        curAppIcon?.let{
            canvas.drawBitmap(it, curPoint[0] ,curPoint[1], paint)
        }

        super.onDraw(canvas)
    }

    private fun getCurPoint(): FloatArray{
        val centerX = (width.div(2)).toFloat()
        val centerY = (height.div(2)).toFloat()

        innerRecF.apply {
            set(centerX - innerRadius, centerY - innerRadius, centerX + innerRadius, centerY + innerRadius)
        }

        val startAngle = -90f + 0.1f * curSec
        val sweepAngle = 0.1f

        val arcPath = Path()
        arcPath.arcTo(innerRecF, startAngle, sweepAngle)

        // 호 경로의 절반에 있는 지점의 좌표를 찾음
        val pm = PathMeasure(arcPath, false)
        val point = FloatArray(2)
        pm.getPosTan(pm.getLength() * 0.5f, point, null) // distance 만큼 시작 지점을 이동한 후의 위치를 point 변수에 담음

        return point
    }
}