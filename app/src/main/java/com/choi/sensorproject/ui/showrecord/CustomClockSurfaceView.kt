package com.choi.sensorproject.ui.showrecord

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.Point
import android.graphics.PorterDuff
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import com.choi.sensorproject.service.Orientation
import com.choi.sensorproject.ui.model.RecordsForHourUIModel
import com.choi.sensorproject.ui.model.SensorRecordUIModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import kotlin.math.atan2
import kotlin.math.pow
import kotlin.math.sqrt

@SuppressLint("SimpleDateFormat")
class CustomClockSurfaceView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : SurfaceView(context, attrs), SurfaceHolder.Callback {
    override fun surfaceCreated(holder: SurfaceHolder) {
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
    }

    var touchListener: TouchListener? = null
    var drawSuccessListener: DrawSuccessListener? = null

    private val totalRectF = RectF()
    private val insideRectF = RectF()
    private val outsideRectF = RectF()
    private val paint = Paint()
    private var radius = 0
    private var insideRadius = 0
    private var outsideRadius = 0
    private var arcStrokeWidth = 100f

    private var centerX: Float = 0f
    private var centerY: Float = 0f

    private var sensorRecordMap: MutableMap<Int, SensorRecordUIModel> = mutableMapOf()

    @SuppressLint("SimpleDateFormat")
    private val timeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    @SuppressLint("SimpleDateFormat")
    private val minuteFormat = SimpleDateFormat("mm")
    @SuppressLint("SimpleDateFormat")
    private val secondFormat = SimpleDateFormat("ss")

    private var surfaceHolder: SurfaceHolder

    private var curDrawingJob: Job ? = null

    init{
        holder.setFormat(PixelFormat.RGBA_8888)
        holder.setFormat(PixelFormat.TRANSLUCENT)
        setZOrderOnTop(true)
        surfaceHolder = this.holder
        surfaceHolder.addCallback(this)
    }

    fun setCurModel(recordsForHourUIModel: RecordsForHourUIModel){
        // SurfaceView는 MainThread가 아닌 Thread를 활용하여 화면을 그릴 수 있음
        // 이전 coroutine job cancel 필수
        curDrawingJob?.let{ job ->
            if(job.isActive) {
                job.cancel()
            }
        }
        curDrawingJob = runDrawingJob(recordsForHourUIModel)
    }

    @SuppressLint("DrawAllocation")
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = getDefaultSize(suggestedMinimumWidth, widthMeasureSpec)
        val height = getDefaultSize(suggestedMinimumHeight, heightMeasureSpec)

        val min = Math.min(width, height)
        radius = (min - paddingLeft - 90) / 2
        insideRadius = radius - arcStrokeWidth.toInt() / 2
        outsideRadius = radius + arcStrokeWidth.toInt() / 2

        centerX = (width/2).toFloat()
        centerY = (height/2).toFloat()

        // totalRectF: 도형을 그리는 최대 범위 설정
        totalRectF.apply {
            set(centerX - radius, centerY - radius, centerX + radius, centerY + radius)
        }
        insideRectF.apply {
            set(
                centerX - insideRadius,
                centerY - insideRadius,
                centerX + insideRadius,
                centerY + insideRadius
            )
        }
        outsideRectF.apply {
            set(
                centerX - outsideRadius,
                centerY - outsideRadius,
                centerX + outsideRadius,
                centerY + outsideRadius
            )
        }

        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    // lockHardwareCanvas와 unlockCanvasAndPost를 활용하면 GPU가 작업을 담당하여 빠른 속도로 화면에 그릴 수 있음
    private fun runDrawingJob(recordsForHourUIModel: RecordsForHourUIModel): Job {
        return CoroutineScope(Dispatchers.Default).launch {
            delay(100) // dialog 를 띄우기에 충분한 시간 부여

            // 특이점: lockHardwareCanvas와 unlockCanvasAndPost 사이에 delay를 사용할 수 없음
            // 지우는 과정과 그리는 과정을 나눠서 각각 Post하면 잔상 생김

            val canvas = surfaceHolder.lockHardwareCanvas() // GPU에서 렌더링하기 위한 버퍼를 잠그고 그리기에 사용할 수 있도록 캔버스를 반환
            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR) // 이전에 그려진 것 제거
            drawCanvas(canvas, recordsForHourUIModel)
            surfaceHolder.unlockCanvasAndPost(canvas) // 버퍼를 잠금 해제하여 컴포지터로 전송
            drawSuccessListener?.onDrawSuccess() // 작업이 완료되었다고 알림 (fragment에서 override하여 callback을 받을 수 있도록 함)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event?.let{

            // 원의 중심점과 터치 포인트 사이의 각도와 거리를 계산
            val angle = 180 - atan2((it.x - centerX).toDouble(), (it.y - centerY).toDouble()) * 180 / Math.PI
            val distance = sqrt((it.x - centerX).pow(2) +(it.y - centerY).pow(2))

            // 터치 포인트가 그려진 범위 안에 있으면 각도->시간초 변환 후 시간초에 기록된 sensorRecord를 넘겨줌
            if(insideRadius <= distance && distance <= outsideRadius){
                sensorRecordMap.get((angle * 10).toInt())?.let { curRecord ->
                    touchListener?.onSensorRecordTouch(curRecord)
                }
            }

        }
        return true
    }

    private fun drawCanvas(canvas: Canvas, recordsForHourUIModel: RecordsForHourUIModel) {
        updateSensorRecordMap(recordsForHourUIModel)
        for (sec in 0 until 3600) {
            val startAngle = -90f + 0.1f * sec
            val sweepAngle = 0.1f
            sensorRecordMap.get(sec)?.let{ curRecord ->
                // 기기가 앞쪽으로 쏠림: 연한색, 뒤쪽: 진한색
                // 기기가 왼쪽으로 쏠림: 주황색, 오른쪽: 보라색
                var frontLeaning = false
                var leftLeaning = false

                when (curRecord.orientation) {
                    Orientation.Portrait -> {
                        frontLeaning = curRecord.zAngle < 0
                        leftLeaning = curRecord.xAngle >= 0
                    }

                    Orientation.LandScape -> {
                        frontLeaning = curRecord.zAngle < 0
                        leftLeaning = curRecord.xAngle.let { xAngle ->
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

                // 화면이 꺼져 있으면 검은색으로 결정
                if(curRecord.isScreenOn == false){
                    canvas.drawArc(totalRectF, startAngle, sweepAngle, false, paint.apply {
                        color = Color.BLACK
                        strokeWidth = arcStrokeWidth
                        style = Paint.Style.STROKE
                    })
                }
                // 화면이 켜져 있으면 각도에 따라 색 결정
                else if (frontLeaning && leftLeaning) {
                    canvas.drawArc(totalRectF, startAngle, sweepAngle, false, paint.apply {
                        color = Color.parseColor("#20B2AA")
                        strokeWidth = arcStrokeWidth
                        style = Paint.Style.STROKE
                    })
                } else if (frontLeaning && leftLeaning.not()) {
                    canvas.drawArc(totalRectF, startAngle, sweepAngle, false, paint.apply {
                        color = Color.parseColor("#BA55D3")
                        strokeWidth = arcStrokeWidth
                        style = Paint.Style.STROKE
                    })
                } else if (frontLeaning.not() && leftLeaning) {
                    canvas.drawArc(totalRectF, startAngle, sweepAngle, false, paint.apply {
                        color = Color.parseColor("#52E4DC")
                        strokeWidth = arcStrokeWidth
                        style = Paint.Style.STROKE
                    })
                } else if (frontLeaning.not() && leftLeaning.not()) {
                    canvas.drawArc(totalRectF, startAngle, sweepAngle, false, paint.apply {
                        color = Color.parseColor("#FF9DFF")
                        strokeWidth = arcStrokeWidth
                        style = Paint.Style.STROKE
                    })
                }
            } ?: run {
                canvas.drawArc(totalRectF, startAngle, sweepAngle, false, paint.apply {
                    color = Color.GRAY
                    strokeWidth = 100f
                    style = Paint.Style.STROKE
                })
            }

            // 호의 안쪽, 바깥쪽에 검은 선 그리기
            canvas.drawArc(insideRectF, startAngle, sweepAngle, false, paint.apply {
                color = Color.BLACK
                strokeWidth = 5f
                style = Paint.Style.STROKE
            })
            canvas.drawArc(outsideRectF, startAngle, sweepAngle, false, paint.apply {
                color = Color.BLACK
                strokeWidth = 5f
                style = Paint.Style.STROKE
            })
        }
    }

    private fun updateSensorRecordMap(recordsForHourUIModel: RecordsForHourUIModel){
        sensorRecordMap.clear()
        for(curRecord in recordsForHourUIModel.records){
            val curDate = timeFormat.parse(curRecord.recordTime)
            if (curDate != null) {
                val curMinute = minuteFormat.format(curDate).toInt()
                val curSecond = secondFormat.format(curDate).toInt()
                val curTotalSeconds = curMinute * 60 + curSecond
                sensorRecordMap.put(curTotalSeconds, curRecord)
            }
        }
    }
}