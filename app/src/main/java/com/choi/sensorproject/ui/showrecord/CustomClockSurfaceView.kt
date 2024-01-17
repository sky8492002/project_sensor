package com.choi.sensorproject.ui.showrecord

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
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
    private val insideExpansionRectF = RectF()
    private val outsideExpansionRectF = RectF()
    private val paint = Paint()
    private var radius = 0
    private var insideRadius = 0
    private var outsideRadius = 0
    private var insideExpansionRadius = 0
    private var outsideExpansionRadius = 0
    private var arcStrokeWidth = 100f

    // onTouchEvent에서 필요 (regions의 index는 sensorRecordMap의 key와 초단위로 1:1대응)
    private var regions: MutableList<Region> = mutableListOf()
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
        insideExpansionRadius = radius - arcStrokeWidth.toInt()
        outsideExpansionRadius = radius + arcStrokeWidth.toInt()

        val centerX = (width.div(2)).toFloat()
        val centerY = (height.div(2)).toFloat()

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

        insideExpansionRectF.apply {
            set(
                centerX - insideExpansionRadius,
                centerY - insideExpansionRadius,
                centerX + insideExpansionRadius,
                centerY + insideExpansionRadius
            )
        }

        outsideExpansionRectF.apply {
            set(
                centerX - outsideExpansionRadius,
                centerY - outsideExpansionRadius,
                centerX + outsideExpansionRadius,
                centerY + outsideExpansionRadius
            )
        }

        regions.clear()
        for(sec in 0 until 3600){

            val midAngle = -90f + 0.1f * sec + 0.05f // drawCanvas에서 그릴 호의 중간 각도
            val sweepAngle = 10f // 0.1f는 너무 작아서 터치 포인트가 어떠한 region에도 포함되지 않을 수 있음
            val startAngle = midAngle - sweepAngle / 2

            // midAngle 각도를 중심으로 양쪽으로 sweepAngle/2 씩 확장된 범위를 region으로 설정 (touchPath는 두 호를 잇는 경로를 설정함)
            val touchPath = Path()
            touchPath.arcTo(insideExpansionRectF, startAngle, sweepAngle)
            touchPath.arcTo(outsideExpansionRectF, startAngle, sweepAngle)
            touchPath.close()
            val curRegion = Region(Rect().apply {
                set(
                    outsideExpansionRectF.left.toInt(), outsideExpansionRectF.top.toInt(),
                    outsideExpansionRectF.right.toInt(), outsideExpansionRectF.bottom.toInt()
                )
            })
            curRegion.setPath(touchPath, curRegion)
            regions.add(curRegion)
        }

        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    // Dispatchers.Main이 아닌 CorutineScope를 launch하면 빠른 속도로 화면에 그릴 수 있음
    private fun runDrawingJob(recordsForHourUIModel: RecordsForHourUIModel): Job {
        return CoroutineScope(Dispatchers.Default).launch {
            delay(100)

            var canvas = surfaceHolder.lockHardwareCanvas() // GPU에서 렌더링하기 위한 버퍼를 잠그고 그리기에 사용할 수 있도록 캔버스를 반환
            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR) // 이전에 그려진 것 제거
            surfaceHolder.unlockCanvasAndPost(canvas) // 버퍼를 잠금 해제하여 컴포지터로 전송

            // 특이점: lockHardwareCanvas와 unlockCanvasAndPost 사이에 delay를 사용할 수 없음
            // 한꺼번에 Post하면 지워지는 것이 보이지 않으므로 각각 Post 하되, 그리기 전에 한번 더 지워줌 (잔상 방지)

            canvas = surfaceHolder.lockHardwareCanvas() // GPU에서 렌더링하기 위한 버퍼를 잠그고 그리기에 사용할 수 있도록 캔버스를 반환
            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR) // 이전에 그려진 것 제거
            drawCanvas(canvas, recordsForHourUIModel)
            surfaceHolder.unlockCanvasAndPost(canvas) // 버퍼를 잠금 해제하여 컴포지터로 전송
            drawSuccessListener?.onDrawClockViewSuccess() // 작업이 완료되었다고 알림 (fragment에서 override하여 callback을 받을 수 있도록 함)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event?.let{

            val point = Point()
            point.x = it.x.toInt()
            point.y = it.y.toInt()

            val secList = mutableListOf<Int>()

            if(event.action == MotionEvent.ACTION_DOWN) {
                // 터치 포인트를 포함하는 region 중 sensorRecord가 있는 시간초를 secList에 담음
                for (sec in 0 until regions.size) {
                    if (regions[sec].contains(point.x, point.y)) {
                        sensorRecordMap.get(sec)?.let{
                            secList.add(sec)
                        }
                    }
                }
            }

            if(secList.isEmpty().not()){
                // secList의 중간 시간초에 기록된 sensorRecord를 찾아 넘겨줌 (map을 활용하여 탐색 속도 향상)
                sensorRecordMap.get(secList[secList.size/2])?.let { curRecord ->
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