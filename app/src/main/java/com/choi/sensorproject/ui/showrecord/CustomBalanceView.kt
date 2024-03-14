package com.choi.sensorproject.ui.showrecord

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
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

class CustomBalanceView(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {

    private val paint = Paint()
    private val totalRectF = RectF()

    private var radius = 0
    private var centerX: Float = 0f
    private var centerY: Float = 0f
    private var balanceLineStartY = 0f
    private var balanceLineEndY = 0f
    private var curLineStartY = 0f
    private var curLineEndY = 0f

    private var leftBigBasketX = 0f
    private var rightBigBasketX = 0f

    private var frontLeaningCount = 0
    private var backLeaningCount = 0
    private var leftLeaningCount = 0
    private var rightLeaningCount = 0

    private var frontLeftLeaningCount = 0
    private var frontRightLeaningCount = 0
    private var backLeftLeaningCount = 0
    private var backRightLeaningCount = 0

    private var curBasketFillRatio = 0f

    private val leaningCountTextSize = 40f

    private var curDrawingJob: Job? = null


    init {
        setLayerType(LAYER_TYPE_SOFTWARE, null) // PorterDuff.Mode.SRC_IN을 적용하기 위해 필요
    }

    fun setCurModel(recordsForHourUIModel: RecordsForHourUIModel){
        frontLeaningCount = 0
        backLeaningCount = 0
        leftLeaningCount = 0
        rightLeaningCount = 0

        frontLeftLeaningCount = 0
        frontRightLeaningCount = 0
        backLeftLeaningCount = 0
        backRightLeaningCount = 0

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

            if(frontLeaning && leftLeaning) frontLeftLeaningCount +=1
            else if(frontLeaning) frontRightLeaningCount +=1
            else if(leftLeaning) backLeftLeaningCount +=1
            else backRightLeaningCount +=1
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
            val lastStartY = curLineStartY
            val lastEndY = curLineEndY

            // 지수함수 그래프 진행도에 따라 서서히 천칭과 바구니를 변화시킴
            val endGraphY = 1.1f.pow(100)
            for(n in 1.. 100){
                val curGraphYRatio = 1.1f.pow(n) / endGraphY
                curLineStartY = lastStartY + (balanceLineStartY - lastStartY) * curGraphYRatio
                curLineEndY = lastEndY + (balanceLineEndY - lastEndY) * curGraphYRatio
                curBasketFillRatio = n.toFloat() /100
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
        curLineStartY = centerY
        curLineEndY = centerY

        leftBigBasketX = centerX / 2
        rightBigBasketX = centerX / 2 * 3

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

        drawBalance(canvas, curLineStartY, curLineEndY)
        drawDetailBasket(canvas, curBasketFillRatio)
        super.onDraw(canvas)
    }

    private fun drawBalance(canvas : Canvas, startY: Float, endY : Float){
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
        drawBasket(canvas, centerX - 100f, startY, 10f, 50f, paint.apply {
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
        drawBasket(canvas, centerX + 100f, endY, 10f, 50f, paint.apply {
            color = Color.parseColor("#FF9DFF")
            strokeWidth = 2f
            style = Paint.Style.STROKE
        })
    }

    private fun drawDetailBasket(canvas: Canvas, basketFillRatio: Float){

        // 천칭 양 옆에 두 개의 큰 바구니를 그림
        drawBasket(canvas, leftBigBasketX, centerY - 50f, 50f, 100f, paint.apply {
            color = Color.parseColor("#52E4DC")
            strokeWidth = 5f
            style = Paint.Style.STROKE
        })
        drawBasket(canvas, rightBigBasketX, centerY - 50f, 50f, 100f, paint.apply {
            color = Color.parseColor("#FF9DFF")
            strokeWidth = 5f
            style = Paint.Style.STROKE
        })

        // 바구니 옆에 텍스트 출력
        val frontLeftTextBounds = Rect()
        paint.getTextBounds(frontLeftLeaningCount.toString(), 0, frontLeftLeaningCount.toString().length, frontLeftTextBounds)
        val frontLeftTextX = leftBigBasketX - 100f - frontLeftTextBounds.width() / 2
        canvas.drawText(frontLeftLeaningCount.toString(), frontLeftTextX, centerY + 50f, paint.apply {
            color = Color.parseColor("#20B2AA")
            typeface = resources.getFont(R.font.godo_m)
            strokeWidth = 1f
            style = Paint.Style.FILL
            textSize = leaningCountTextSize
        })

        val backLeftTextBounds = Rect()
        paint.getTextBounds(backLeftLeaningCount.toString(), 0, backLeftLeaningCount.toString().length, backLeftTextBounds)
        val backLeftTextX = leftBigBasketX - 100f - backLeftTextBounds.width() / 2
        canvas.drawText(backLeftLeaningCount.toString(), backLeftTextX, centerY, paint.apply {
            color = Color.parseColor("#52E4DC")
            typeface = resources.getFont(R.font.godo_m)
            strokeWidth = 1f
            style = Paint.Style.FILL
            textSize = leaningCountTextSize
        })

        val frontRightTextBounds = Rect()
        paint.getTextBounds(frontRightLeaningCount.toString(), 0, frontRightLeaningCount.toString().length, frontRightTextBounds)
        val frontRightTextX = rightBigBasketX + 100f - frontRightTextBounds.width() / 2
        canvas.drawText(frontRightLeaningCount.toString(), frontRightTextX, centerY + 50f, paint.apply {
            color = Color.parseColor("#BA55D3")
            typeface = resources.getFont(R.font.godo_m)
            strokeWidth = 1f
            style = Paint.Style.FILL
            textSize = leaningCountTextSize
        })

        val backRightTextBounds = Rect()
        paint.getTextBounds(backRightLeaningCount.toString(), 0, backRightLeaningCount.toString().length, backRightTextBounds)
        val backRightTextX = rightBigBasketX + 100f - backRightTextBounds.width() / 2
        canvas.drawText(backRightLeaningCount.toString(), backRightTextX, centerY, paint.apply {
            color = Color.parseColor("#FF9DFF")
            typeface = resources.getFont(R.font.godo_m)
            strokeWidth = 1f
            style = Paint.Style.FILL
            textSize = leaningCountTextSize
        })

        // 바구니 안에 빈 부분으로 채우기
        val blankFillPaint = Paint().apply {
            color = Color.parseColor("#FFFFFF")
            strokeWidth = 0f
            style = Paint.Style.FILL
        }
        drawBasket(canvas, leftBigBasketX, centerY - 50f, 50f, 100f, blankFillPaint)
        drawBasket(canvas, rightBigBasketX, centerY - 50f, 50f, 100f, blankFillPaint)

        // front/back 비율에 따라 바구니 채우기 (basketFillRatio가 커짐에 따라 점점 차오름)
        val frontLeftPaint = Paint()
        val frontLeftRatio = frontLeftLeaningCount.toFloat() / (frontLeftLeaningCount.toFloat() + backLeftLeaningCount.toFloat())
        val frontLeftWaterHeight =
            if(frontLeftLeaningCount + backLeftLeaningCount != 0)
                    (frontLeftRatio * 0.5 * basketFillRatio * 100).toFloat() else 0f
        frontLeftPaint.apply{
            color = Color.parseColor("#20B2AA")
            strokeWidth = 2f
            style = Paint.Style.FILL
        }
        frontLeftPaint.setXfermode(PorterDuffXfermode(PorterDuff.Mode.SRC_IN))

        val backLeftPaint = Paint()
        val backLeftRatio = backLeftLeaningCount.toFloat() / (frontLeftLeaningCount.toFloat() + backLeftLeaningCount.toFloat())
        val backLeftWaterHeight =
            if(frontLeftLeaningCount + backLeftLeaningCount != 0)
                    (backLeftRatio * 0.5 * basketFillRatio * 100).toFloat() else 0f
        backLeftPaint.apply{
            color = Color.parseColor("#52E4DC")
            strokeWidth = 2f
            style = Paint.Style.FILL
        }
        backLeftPaint.setXfermode(PorterDuffXfermode(PorterDuff.Mode.SRC_IN))

        val frontRightPaint = Paint()
        val frontRightRatio = frontRightLeaningCount.toFloat() / (frontRightLeaningCount.toFloat() + backRightLeaningCount.toFloat())
        val frontRightWaterHeight =
            if(frontRightLeaningCount + backRightLeaningCount != 0)
                (frontRightRatio * 0.5 * basketFillRatio * 100).toFloat() else 0f
        frontRightPaint.apply{
            color = Color.parseColor("#BA55D3")
            strokeWidth = 2f
            style = Paint.Style.FILL
        }
        frontRightPaint.setXfermode(PorterDuffXfermode(PorterDuff.Mode.SRC_IN))

        val backRightPaint = Paint()
        val backRightRatio = backRightLeaningCount.toFloat() / (frontRightLeaningCount.toFloat() + backRightLeaningCount.toFloat())
        val backRightWaterHeight =
            if(frontRightLeaningCount + backRightLeaningCount != 0)
                (backRightRatio * 0.5 * basketFillRatio * 100).toFloat() else 0f
        backRightPaint.apply{
            color = Color.parseColor("#FF9DFF")
            strokeWidth = 2f
            style = Paint.Style.FILL
        }
        backRightPaint.setXfermode(PorterDuffXfermode(PorterDuff.Mode.SRC_IN))

        canvas.drawRect(leftBigBasketX - 50f, centerY + 100f - frontLeftWaterHeight,
            leftBigBasketX + 50f, centerY + 100f, frontLeftPaint)
        canvas.drawRect(leftBigBasketX - 50f, centerY + 100f - frontLeftWaterHeight - backLeftWaterHeight,
            leftBigBasketX + 50f, centerY + 100f - frontLeftWaterHeight, backLeftPaint)

        canvas.drawRect(rightBigBasketX - 50f, centerY + 100f - frontRightWaterHeight,
            rightBigBasketX + 50f, centerY + 100f, frontRightPaint)
        canvas.drawRect(rightBigBasketX - 50f, centerY + 100f - frontRightWaterHeight - backRightWaterHeight,
            rightBigBasketX + 50f, centerY + 100f - frontRightWaterHeight, backRightPaint)

    }

    private fun drawBasket(canvas: Canvas, locateX: Float, locateY: Float, basketRadius: Float, ropeHeight: Float, paint: Paint){
        val blackStrokePaint = Paint().apply {
            color = Color.parseColor("#000000")
            strokeWidth = paint.strokeWidth + 0.5f
            style = Paint.Style.STROKE
        }

        canvas.drawLine(locateX, locateY, locateX - basketRadius, locateY + ropeHeight, blackStrokePaint)
        canvas.drawLine(locateX, locateY, locateX - basketRadius, locateY + ropeHeight, paint)
        canvas.drawLine(locateX, locateY, locateX + basketRadius, locateY + ropeHeight, blackStrokePaint)
        canvas.drawLine(locateX, locateY, locateX + basketRadius, locateY + ropeHeight, paint)

        val basketRectF = RectF()
        basketRectF.set(locateX - basketRadius, locateY + ropeHeight - basketRadius, locateX + basketRadius, locateY + ropeHeight + basketRadius)

        canvas.drawArc(basketRectF, 0f, 180f, true, blackStrokePaint)
        canvas.drawArc(basketRectF, 0f, 180f, true, paint)

    }
}