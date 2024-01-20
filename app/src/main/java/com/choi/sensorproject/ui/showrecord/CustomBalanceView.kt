package com.choi.sensorproject.ui.showrecord

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import com.choi.sensorproject.service.Orientation
import com.choi.sensorproject.ui.model.RecordsForHourUIModel
import com.example.sensorproject.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.pow

class CustomBalanceView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    private val paint = Paint()
    private val totalRectF = RectF()
    private var radius = 0

    private var centerX: Float = 0f
    private var centerY: Float = 0f

    private var frontLeaningCount = 0
    private var backLeaningCount = 0
    private var leftLeaningCount = 0
    private var rightLeaningCount = 0

    private var balanceLineStartY = 0f
    private var balanceLineEndY = 0f

    private var curStartY = 0f
    private var curEndY = 0f

    private var curDrawingJob: Job? = null

    fun setCurModel(recordsForHourUIModel: RecordsForHourUIModel){
        frontLeaningCount = 0
        backLeaningCount = 0
        leftLeaningCount = 0
        rightLeaningCount = 0

        for(record in recordsForHourUIModel.records){
            if(!record.isScreenOn) continue

            var frontLeaning = false
            var leftLeaning = false

            when (record.orientation) {
                Orientation.Portrait -> {
                    frontLeaning = record.zAngle < 0
                    leftLeaning = record.xAngle >= 0
                }

                Orientation.LandScape -> {
                    frontLeaning = record.zAngle < 0
                    leftLeaning = record.xAngle.let { xAngle ->
                        if (xAngle >= 0) {
                            if (xAngle >= 90) {
                                true
                            } else false
                        } else {
                            if (xAngle >= -90) {
                                true
                            } else false
                        }
                    }
                }
            }

            if(frontLeaning) frontLeaningCount +=1 else backLeaningCount +=1
            if(leftLeaning) leftLeaningCount +=1 else rightLeaningCount +=1
        }

        if(leftLeaningCount + rightLeaningCount != 0){
            balanceLineStartY = centerY + (leftLeaningCount - rightLeaningCount).toFloat() / (leftLeaningCount + rightLeaningCount).toFloat() * 50
            balanceLineEndY = centerY + (rightLeaningCount - leftLeaningCount).toFloat() / (leftLeaningCount + rightLeaningCount).toFloat() * 50
        }
        else{
            balanceLineStartY = centerY
            balanceLineEndY = centerY
        }

        // 이전 coroutine job cancel 필수
        curDrawingJob?.let{ job ->
            if(job.isActive) {
                job.cancel()
            }
        }
        curDrawingJob = CoroutineScope(Dispatchers.Main).launch {
            // 이전에 진행되던 천칭의 위치를 기준으로 변화
            val lastStartY = curStartY
            val lastEndY = curEndY

            // 지수함수 그래프 진행도에 따라 서서히 천칭을 변화시킴
            val endGraphY = 1.1f.pow(100)
            for(n in 1.. 100){
                val curGraphYRatio = 1.1f.pow(n) / endGraphY
                curStartY = lastStartY + (balanceLineStartY - lastStartY) * curGraphYRatio
                curEndY = lastEndY + (balanceLineEndY - lastEndY) * curGraphYRatio
                invalidate()
                delay(5)
            }
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = getDefaultSize(suggestedMinimumWidth, widthMeasureSpec)
        val height = getDefaultSize(suggestedMinimumHeight, heightMeasureSpec)

        val min = Math.min(width, height)
        radius = (min - paddingLeft - 90) / 2

        centerX = (width/2).toFloat()
        centerY = (height/2).toFloat()

        balanceLineStartY = centerY
        balanceLineEndY = centerY

        curStartY = centerY
        curEndY = centerY

        // totalRectF: 도형을 그리는 최대 범위 설정
        totalRectF.apply {
            set(centerX - radius, centerY - radius, centerX + radius, centerY + radius)
        }

        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {

        drawBalance(canvas, curStartY, curEndY)
        super.onDraw(canvas)
    }

    private fun drawBalance(canvas : Canvas, startY: Float, endY : Float){
        val leaningCountTextSize = 40f
        val leftBounds = Rect()
        paint.getTextBounds(leftLeaningCount.toString(), 0, leftLeaningCount.toString().length, leftBounds)
        val leftTextX = centerX - 100f - leftBounds.width() / 2
        canvas.drawText(leftLeaningCount.toString(), leftTextX, startY - 30, paint.apply {
            color = Color.parseColor("#000000")
            typeface = resources.getFont(R.font.godo_m)
            strokeWidth = 2f
            style = Paint.Style.STROKE
            textSize = leaningCountTextSize
        })
        canvas.drawText(leftLeaningCount.toString(), leftTextX, startY - 30, paint.apply {
            color = Color.parseColor("#52E4DC")
            typeface = resources.getFont(R.font.godo_m)
            strokeWidth = 1f
            style = Paint.Style.FILL
            textSize = leaningCountTextSize
        })
        canvas.drawLine(centerX - 100f, startY, centerX, centerY, paint.apply {
            color = Color.parseColor("#000000")
            strokeWidth = 12f
            style = Paint.Style.STROKE
        })
        canvas.drawLine(centerX - 100f, startY, centerX, centerY, paint.apply {
            color = Color.parseColor("#52E4DC")
            strokeWidth = 10f
            style = Paint.Style.STROKE
        })
        drawBasket(canvas, centerX - 100f, startY, paint.apply {
            color = Color.parseColor("#52E4DC")
            strokeWidth = 2f
            style = Paint.Style.STROKE
        })

        val rightBounds = Rect()
        paint.getTextBounds(rightLeaningCount.toString(), 0, rightLeaningCount.toString().length, rightBounds)
        val rightTextX = centerX + 100f - rightBounds.width() / 2
        canvas.drawText(rightLeaningCount.toString(), rightTextX, endY - 30, paint.apply {
            color = Color.parseColor("#000000")
            typeface = resources.getFont(R.font.godo_m)
            strokeWidth = 2f
            style = Paint.Style.STROKE
            textSize = leaningCountTextSize
        })
        canvas.drawText(rightLeaningCount.toString(), rightTextX, endY - 30, paint.apply {
            color = Color.parseColor("#FF9DFF")
            typeface = resources.getFont(R.font.godo_m)
            strokeWidth = 1f
            style = Paint.Style.FILL
            textSize = leaningCountTextSize
        })
        canvas.drawLine(centerX, centerY, centerX + 100f, endY, paint.apply {
            color = Color.parseColor("#000000")
            strokeWidth = 12f
            style = Paint.Style.STROKE
        })
        canvas.drawLine(centerX, centerY, centerX + 100f, endY, paint.apply {
            color = Color.parseColor("#FF9DFF")
            strokeWidth = 10f
            style = Paint.Style.STROKE
        })
        drawBasket(canvas, centerX + 100f, endY, paint.apply {
            color = Color.parseColor("#FF9DFF")
            strokeWidth = 2f
            style = Paint.Style.STROKE
        })
    }

    private fun drawBasket(canvas: Canvas, locateX: Float, locateY: Float, paint: Paint){
        val blackStrokePaint = Paint().apply {
            color = Color.parseColor("#000000")
            strokeWidth = paint.strokeWidth + 0.5f
            style = Paint.Style.STROKE
        }
        canvas.drawLine(locateX, locateY, locateX - 10, locateY + 50, blackStrokePaint)
        canvas.drawLine(locateX, locateY, locateX - 10, locateY + 50, paint)
        canvas.drawLine(locateX, locateY, locateX + 10, locateY + 50, blackStrokePaint)
        canvas.drawLine(locateX, locateY, locateX + 10, locateY + 50, paint)

        val basketRectF = RectF()
        val basketRadius = 10f
        basketRectF.set(locateX - basketRadius, locateY + 50 - basketRadius, locateX + basketRadius, locateY + 50 + basketRadius)

        canvas.drawArc(basketRectF, 0f, 180f, true, blackStrokePaint)
        canvas.drawArc(basketRectF, 0f, 180f, true, paint)
    }

}