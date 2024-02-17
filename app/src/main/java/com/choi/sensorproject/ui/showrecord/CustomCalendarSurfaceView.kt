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
import android.widget.CalendarView
import com.choi.sensorproject.service.Orientation
import com.choi.sensorproject.ui.model.RecordsForHourUIModel
import com.choi.sensorproject.ui.model.SensorRecordUIModel
import com.example.sensorproject.R
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
class CustomCalendarSurfaceView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : SurfaceView(context, attrs), SurfaceHolder.Callback {
    override fun surfaceCreated(holder: SurfaceHolder) {
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
    }

    private val totalRectF = RectF()
    private var radius = 0

    private var centerX: Float = 0f
    private var centerY: Float = 0f

    private var surfaceHolder: SurfaceHolder

    private var curDrawingJob: Job ? = null

    private var paint = Paint()

    private var curModels: List<RecordsForHourUIModel> = mutableListOf()

    init{
        holder.setFormat(PixelFormat.RGBA_8888)
        holder.setFormat(PixelFormat.TRANSLUCENT)
        setZOrderOnTop(true)
        surfaceHolder = this.holder
        surfaceHolder.addCallback(this)
    }

    fun setDayModels(models: List<RecordsForHourUIModel>){
        curModels = models
    }

    fun update(){
        // 이전 coroutine job cancel 필수
        curDrawingJob?.let{ job ->
            if(job.isActive) {
                job.cancel()
            }
        }
        curDrawingJob = runDrawingJob()
    }

    @SuppressLint("DrawAllocation")
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = getDefaultSize(suggestedMinimumWidth, widthMeasureSpec)
        val height = getDefaultSize(suggestedMinimumHeight, heightMeasureSpec)

        val min = Math.min(width, height)
        radius = (min - paddingLeft - 90) / 2

        centerX = (width/2).toFloat()
        centerY = (height/2).toFloat()

        // totalRectF: 도형을 그리는 최대 범위 설정
        totalRectF.apply {
            set(centerX - radius, centerY - radius, centerX + radius, centerY + radius)
        }

        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    // lockHardwareCanvas와 unlockCanvasAndPost를 활용하면 GPU가 작업을 담당하여 빠른 속도로 화면에 그릴 수 있음
    private fun runDrawingJob(): Job {
        return CoroutineScope(Dispatchers.Default).launch {
            val canvas = surfaceHolder.lockHardwareCanvas() // GPU에서 렌더링하기 위한 버퍼를 잠그고 그리기에 사용할 수 있도록 캔버스를 반환
            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR) // 이전에 그려진 것 제거
            drawCanvas(canvas)
            surfaceHolder.unlockCanvasAndPost(canvas) // 버퍼를 잠금 해제하여 컴포지터로 전송
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event?.let{

        }
        return true
    }

    private fun drawCanvas(canvas: Canvas) {

        for(n in 0 until  10){
            canvas.drawText(n.toString(), totalRectF.width() * n.toFloat() / 10, 50f, paint.apply {
                color = Color.parseColor("#000000")
                typeface = resources.getFont(R.font.godo_m)
                strokeWidth = 2f
                style = Paint.Style.STROKE
                textSize = 60f
            })
        }
        for(model in curModels){
        }
    }
}