package com.choi.sensorproject.ui.showrecord

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PixelFormat
import android.graphics.Point
import android.graphics.PorterDuff
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Region
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import com.choi.sensorproject.service.Orientation
import com.choi.sensorproject.ui.model.RecordsForHourUIModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat

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
        curModel = recordsForHourUIModel
        regions.clear()
        // SurfaceView는 MainThread가 아닌 Thread를 활용하여 화면을 그릴 수 있음
        //DrawingThread().start()

        // 이전 coroutine job cancel 필수
        curDrawingJob?.let{ job ->
            if(job.isActive) {
                job.cancel()
            }
        }
        curDrawingJob = runDrawingJob()
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
    private inner class DrawingThread(): Thread() {
        override fun run() {
            val canvas = surfaceHolder.lockHardwareCanvas() // GPU에서 렌더링하기 위한 버퍼를 잠그고 그리기에 사용할 수 있도록 캔버스를 반환
            drawCanvas(canvas)
            surfaceHolder.unlockCanvasAndPost(canvas) // 버퍼를 잠금 해제하여 컴포지터로 전송
        }
    }

    // Dispatchers.Main이 아닌 CorutineScope를 launch하면 빠른 속도로 화면에 그릴 수 있음
    fun runDrawingJob(): Job {
        return CoroutineScope(Dispatchers.IO).launch {
            val canvas = surfaceHolder.lockHardwareCanvas() // GPU에서 렌더링하기 위한 버퍼를 잠그고 그리기에 사용할 수 있도록 캔버스를 반환
            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.MULTIPLY) // 이전에 그려진 것 제거
            drawCanvas(canvas)
            surfaceHolder.unlockCanvasAndPost(canvas) // 버퍼를 잠금 해제하여 컴포지터로 전송
        }
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

    fun drawCanvas(canvas: Canvas) {

        val centerX = (width.div(2)).toFloat()
        val centerY = (height.div(2)).toFloat()

        // totalRectF: 도형을 그리는 최대 범위 설정
        totalRecF.apply {
            set(centerX - radius, centerY - radius, centerX + radius, centerY + radius)
        }
        insideRecF.apply {
            set(
                centerX - insideRadius,
                centerY - insideRadius,
                centerX + insideRadius,
                centerY + insideRadius
            )
        }
        outsideRecF.apply {
            set(
                centerX - outsideRadius,
                centerY - outsideRadius,
                centerX + outsideRadius,
                centerY + outsideRadius
            )
        }

        curModel?.let { curModel ->
            var curIndex = 0
            for (sec in 0 until 3600) {

                val startAngle = -90f + 0.1f * sec
                val sweepAngle = 0.1f

                if (curIndex >= curModel.records.size) {
                    break
                }
                val curRecord = curModel.records[curIndex]
                val curDate = timeFormat.parse(curRecord.recordTime)
                if (curDate != null) {
                    val curMinute = minuteFormat.format(curDate).toInt()
                    val curSecond = secondFormat.format(curDate).toInt()
                    val curTotalSeconds = curMinute * 60 + curSecond

                    if (curTotalSeconds == sec) {

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

                        if (frontLeaning && leftLeaning) {
                            canvas.drawArc(totalRecF, startAngle, sweepAngle, false, paint.apply {
                                color = Color.parseColor("#FFECB3")
                                strokeWidth = arcStrokeWidth
                                style = Paint.Style.STROKE
                            })
                        } else if (frontLeaning && leftLeaning.not()) {
                            canvas.drawArc(totalRecF, startAngle, sweepAngle, false, paint.apply {
                                color = Color.parseColor("#E1BEE7")
                                strokeWidth = arcStrokeWidth
                                style = Paint.Style.STROKE
                            })
                        } else if (frontLeaning.not() && leftLeaning) {
                            canvas.drawArc(totalRecF, startAngle, sweepAngle, false, paint.apply {
                                color = Color.parseColor("#FF8F00")
                                strokeWidth = arcStrokeWidth
                                style = Paint.Style.STROKE
                            })
                        } else if (frontLeaning.not() && leftLeaning.not()) {
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
                            set(
                                outsideRecF.left.toInt(), outsideRecF.top.toInt(),
                                outsideRecF.right.toInt(), outsideRecF.bottom.toInt()
                            )
                        })
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

                        curIndex += 1
                    } else {
                        canvas.drawArc(totalRecF, startAngle, sweepAngle, false, paint.apply {
                            color = Color.GRAY
                            strokeWidth = 100f
                            style = Paint.Style.STROKE
                        })
                    }

                } else {
                    canvas.drawArc(totalRecF, startAngle, sweepAngle, false, paint.apply {
                        color = Color.GRAY
                        strokeWidth = 100f
                        style = Paint.Style.STROKE
                    })
                }
            }
        }

    }

}