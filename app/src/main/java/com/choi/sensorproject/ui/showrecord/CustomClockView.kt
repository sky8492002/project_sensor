package com.choi.sensorproject.ui.showrecord

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.OrientationEventListener
import android.view.View
import com.choi.sensorproject.service.Orientation
import com.choi.sensorproject.ui.model.RecordsForHourUIModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import kotlin.math.abs

@SuppressLint("SimpleDateFormat")
class CustomClockView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    private val rectF = RectF()
    private val paint = Paint()
    private var radius = 0

    private var curModel: RecordsForHourUIModel? = null

    @SuppressLint("SimpleDateFormat")
    private val timeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    @SuppressLint("SimpleDateFormat")
    private val minuteFormat = SimpleDateFormat("mm")
    @SuppressLint("SimpleDateFormat")
    private val secondFormat = SimpleDateFormat("ss")

    fun setCurModel(recordsForHourUIModel: RecordsForHourUIModel){
        curModel = recordsForHourUIModel
        postInvalidate() // 화면을 갱신 (invalidate 앞에 post를 붙이면 ui thread가 아니더라도 ui thread에 요청할 수 있음)
    }

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

        curModel?.let{ curModel ->
            var curIndex = 0
            for (n in 0 until 3600) {
                if(curIndex >= curModel.records.size){
                    break
                }
                val curRecord = curModel.records[curIndex]
                val curDate = timeFormat.parse(curRecord.recordTime)
                if(curDate != null){
                    val minute = minuteFormat.format(curDate).toInt()
                    val second = secondFormat.format(curDate).toInt()
                    val totalSeconds = minute * 60 + second

                    if(totalSeconds == n){

                        // 기기가 앞쪽으로 쏠림: 연한색, 뒤쪽: 진한색
                        // 기기가 왼쪽으로 쏠림: 주황색, 오른쪽: 보라색
                        var frontLeaning = false
                        var leftLeaning = false

                        when(curRecord.orientation){
                            Orientation.Portrait ->{
                                frontLeaning = curRecord.zAngle < 0
                                leftLeaning = curRecord.xAngle >= 0
                            }
                            Orientation.LandScape ->{
                                frontLeaning = curRecord.zAngle < 0
                                leftLeaning =curRecord.xAngle.let{ xAngle ->
                                    if(xAngle >= 0){
                                        if(xAngle >= 90){
                                            true
                                        }
                                        else false
                                    }
                                    else{
                                        if(xAngle >= -90){
                                            true
                                        }
                                        else false
                                    }
                                }
                            }
                        }

                        if (frontLeaning && leftLeaning) {
                            canvas.drawArc(rectF, -90f + 0.1f * n, 0.1f, false, paint.apply {
                                color = Color.parseColor("#FFECB3")
                                strokeWidth = 100f
                                style = Paint.Style.STROKE
                            })
                        }
                        else if(frontLeaning && leftLeaning.not()){
                            canvas.drawArc(rectF, -90f + 0.1f * n, 0.1f, false, paint.apply {
                                color = Color.parseColor("#E1BEE7")
                                strokeWidth = 100f
                                style = Paint.Style.STROKE
                            })
                        }
                        else if(frontLeaning.not() && leftLeaning) {
                            canvas.drawArc(rectF, -90f + 0.1f * n, 0.1f, false, paint.apply {
                                color = Color.parseColor("#FF8F00")
                                strokeWidth = 100f
                                style = Paint.Style.STROKE
                            })
                        }
                        else if(frontLeaning.not() && leftLeaning.not()){
                            canvas.drawArc(rectF, -90f + 0.1f * n, 0.1f, false, paint.apply {
                                color = Color.parseColor("#6A1B9A")
                                strokeWidth = 100f
                                style = Paint.Style.STROKE
                            })
                        }
                        curIndex +=1
                    }
                    else{
                        canvas.drawArc(rectF, -90f + 0.1f * n, 0.1f, false, paint.apply {
                            color = Color.GRAY
                            strokeWidth = 100f
                            style = Paint.Style.STROKE
                        })
                    }

                }
                else{
                    canvas.drawArc(rectF, -90f + 0.1f * n, 0.1f, false, paint.apply {
                        color = Color.GRAY
                        strokeWidth = 100f
                        style = Paint.Style.STROKE
                    })
                }
            }
        }

//        canvas.drawArc(rectF, -90f, 6f, false, paint.apply {
//            color = Color.parseColor("#95a5a6")
//            strokeWidth = 100f
//            style = Paint.Style.STROKE
//        })
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {

        return true
    }

}