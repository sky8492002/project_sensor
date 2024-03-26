package com.choi.sensorproject.ui.showrecord.deprecated

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Point
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Region
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import com.choi.sensorproject.service.Orientation
import com.choi.sensorproject.ui.model.RecordsForHourUIModel
import com.choi.sensorproject.ui.showrecord.listener.TouchListener
import java.text.SimpleDateFormat

@SuppressLint("SimpleDateFormat")
class CustomClockView(context: Context, attrs: AttributeSet) : View(context, attrs) {
    var touchListener: TouchListener? = null

    private val totalRecF = RectF()
    private val insideRecF = RectF()
    private val outsideRecF = RectF()
    private val paint = Paint()
    private var radius = 0
    private var insideRadius = 0
    private var outsideRadius = 0
    private var arcStrokeWidth = 100f

    private var curModel: RecordsForHourUIModel? = null

    private var regions: MutableList<Region> = mutableListOf()

    @SuppressLint("SimpleDateFormat")
    private val timeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    @SuppressLint("SimpleDateFormat")
    private val minuteFormat = SimpleDateFormat("mm")
    @SuppressLint("SimpleDateFormat")
    private val secondFormat = SimpleDateFormat("ss")

    fun setCurModel(recordsForHourUIModel: RecordsForHourUIModel){
        curModel = recordsForHourUIModel
        regions.clear()
        postInvalidate() // 화면을 갱신 (invalidate 앞에 post를 붙이면 ui thread가 아니더라도 ui thread에 요청할 수 있음)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = getDefaultSize(suggestedMinimumWidth, widthMeasureSpec)
        val height = getDefaultSize(suggestedMinimumHeight, heightMeasureSpec)

        val min = Math.min(width, height)
        radius = (min - paddingLeft - 90) / 2
        insideRadius = radius - arcStrokeWidth.toInt() / 2
        outsideRadius = radius + arcStrokeWidth.toInt() / 2

        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {

        val centerX = (width.div(2)).toFloat()
        val centerY = (height.div(2)).toFloat()

        // totalRectF: 도형을 그리는 최대 범위 설정
        totalRecF.apply {
            set(centerX - radius, centerY - radius, centerX + radius, centerY + radius)
        }
        insideRecF.apply {
            set(centerX - insideRadius, centerY - insideRadius, centerX + insideRadius, centerY + insideRadius)
        }
        outsideRecF.apply {
            set(centerX - outsideRadius, centerY - outsideRadius, centerX + outsideRadius, centerY + outsideRadius)
        }

        curModel?.let{ curModel ->
            var curIndex = 0
            for (sec in 0 until 3600) {

                val startAngle = -90f + 0.1f * sec
                val sweepAngle = 0.1f

                if(curIndex >= curModel.records.size){
                    break
                }
                val curRecord = curModel.records[curIndex]
                val curDate = timeFormat.parse(curRecord.recordTime)
                if(curDate != null){
                    val curMinute = minuteFormat.format(curDate).toInt()
                    val curSecond = secondFormat.format(curDate).toInt()
                    val curTotalSeconds = curMinute * 60 + curSecond

                    if(curTotalSeconds == sec){

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
                            canvas.drawArc(totalRecF, startAngle, sweepAngle, false, paint.apply {
                                color = Color.parseColor("#FFECB3")
                                strokeWidth = arcStrokeWidth
                                style = Paint.Style.STROKE
                            })
                        }
                        else if(frontLeaning && leftLeaning.not()){
                            canvas.drawArc(totalRecF, startAngle, sweepAngle, false, paint.apply {
                                color = Color.parseColor("#E1BEE7")
                                strokeWidth = arcStrokeWidth
                                style = Paint.Style.STROKE
                            })
                        }
                        else if(frontLeaning.not() && leftLeaning) {
                            canvas.drawArc(totalRecF, startAngle, sweepAngle, false, paint.apply {
                                color = Color.parseColor("#FF8F00")
                                strokeWidth = arcStrokeWidth
                                style = Paint.Style.STROKE
                            })
                        }
                        else if(frontLeaning.not() && leftLeaning.not()){
                            canvas.drawArc(totalRecF, startAngle, sweepAngle, false, paint.apply {
                                color = Color.parseColor("#6A1B9A")
                                strokeWidth = arcStrokeWidth
                                style = Paint.Style.STROKE
                            })
                        }

                        // 터치 범위 설정 (touchPath는 두 호를 잇는 경로를 설정함)
                        val touchPath = Path()
                        touchPath.arcTo(insideRecF, startAngle, sweepAngle)
                        touchPath.arcTo(outsideRecF, startAngle, sweepAngle)
                        touchPath.close()
                        //touchPath.computeBounds(centerRecF, true)
                        val curRegion = Region(Rect().apply {
                            set(outsideRecF.left.toInt(), outsideRecF.top.toInt(),
                                outsideRecF.right.toInt(), outsideRecF.bottom.toInt())})
                        curRegion.setPath(touchPath, curRegion)
                        regions.add(curRegion)

//                        if(curTotalSeconds > 1800){
//                            canvas.drawPath(touchPath,  paint.apply {
//                                color = Color.parseColor("#FFFFFF")
//                                style = Paint.Style.STROKE
//                            })
//                        }
//                        else if(curTotalSeconds <= 1800){
//                            canvas.drawPath(touchPath,  paint.apply {
//                                color = Color.parseColor("#000000")
//                                style = Paint.Style.STROKE
//                            })
//                        }

                        curIndex +=1
                    }
                    else{
                        canvas.drawArc(totalRecF, startAngle, sweepAngle, false, paint.apply {
                            color = Color.GRAY
                            strokeWidth = 100f
                            style = Paint.Style.STROKE
                        })
                    }

                }
                else{
                    canvas.drawArc(totalRecF, startAngle, sweepAngle, false, paint.apply {
                        color = Color.GRAY
                        strokeWidth = 100f
                        style = Paint.Style.STROKE
                    })
                }
            }
        }

        super.onDraw(canvas)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {

        event?.let{
            val point = Point()
            point.x = it.x.toInt()
            point.y = it.y.toInt()

            Log.d("touched", point.x.toString() + " " + point.y.toString())

            for (index in 0 until regions.size) {
                if (regions[index].contains(point.x, point.y) && event.action == MotionEvent.ACTION_DOWN) {
                    touchListener?.onSensorRecordTouch(curModel!!.records[index])
                    Log.d("touched", curModel!!.records[index].recordTime)
                    break
                }
            }

        }
        return true
    }

}