package com.choi.sensorproject.ui.opngl

import android.R.attr.height
import android.R.attr.width
import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.Rect
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.GLU
import android.opengl.Matrix
import com.choi.sensorproject.ui.showrecord.CalendarListener
import com.example.sensorproject.R
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.Float.Companion.POSITIVE_INFINITY
import kotlin.math.abs
import kotlin.math.pow


class CustomCalendarGLRenderer(val context: Context, val resources: Resources): GLSurfaceView.Renderer{

    private val calendar = Calendar.getInstance()
    var calendarListener: CalendarListener? = null

    @SuppressLint("SimpleDateFormat")
    private val dayFormat = SimpleDateFormat("dd")
    @SuppressLint("SimpleDateFormat")
    private val yearMonthFormat = SimpleDateFormat("yyyy년 MM월")

    private val horizontalSize = 7
    private val verticalSize = 9

    private var aspectRatio: Float = 0f

    private val dayCubes: ArrayDeque<MutableList<DayCube>> = ArrayDeque()

    private var viewPort = intArrayOf(0, 0, 0, 0)

    private val baseBitmapImage = BitmapFactory.decodeResource(context.getResources(), R.drawable.calendar_tile)

    // 드레그에 따라 붙는 가중치 Y
    private var additionalY = 0f
    // 드레그에 따라 붙는 가중치 Z
    private var additionalZ = 0f

    inner class DayCube(var date: Date){
        var verticalNum = 0
        var horizontalNum = 0
        val vPMatrix = FloatArray(16)
        val projectionMatrix = FloatArray(16)
        val viewMatrix = FloatArray(16)
        val cube = Pin()

        var dayBitmapImage = Bitmap.createBitmap(baseBitmapImage.getWidth(), baseBitmapImage.getHeight(), baseBitmapImage.getConfig())
        val canvas = Canvas(dayBitmapImage)

        var locateX = 0f
        var locateY = 0f
        var locateZ = 0f

        init{
            // 이미지를 그리고 텍스트를 추가하는 작업을 미리 수행
            updateDayImage()
        }

        fun setCurDate(date : Date){
            this.date = date
            updateDayImage()
        }

        fun updateLocateInDayCubes(){
            for(vNum in 0 until verticalSize) {
                for (hNum in 0 until horizontalSize) {
                    if(dayCubes[vNum][hNum] == this){
                        verticalNum = vNum
                        horizontalNum = hNum
                        break
                    }
                }
            }
        }

        fun updateDayImage(){
            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
            canvas.drawBitmap(baseBitmapImage, 0f, 0f, Paint())
            val dayNumberPaint = Paint().apply {
                color = Color.parseColor("#20B2AA")
                typeface = resources.getFont(R.font.godo_m)
                strokeWidth = 1f
                style = Paint.Style.FILL
                textSize = 200f
            }

            val day = dayFormat.format(date.time)

            val dayNumberTextBounds = Rect()
            dayNumberPaint.getTextBounds(day.toString(), 0, day.toString().length, dayNumberTextBounds)
            val dayNumberTextX = dayBitmapImage.getWidth()/2f - dayNumberTextBounds.width() / 2

            canvas.drawText(day.toString(),dayNumberTextX , dayBitmapImage.getHeight()/2f, dayNumberPaint)
        }

        fun readyToDraw(){
            // readyToDraw 단계에서 적용할 bitmap를 확정하면 다수의 객체를 그릴 때 마지막 객체에 적용할 bitmap이 전체 객체에 적용되는 문제가 생김
            cube.readyToDraw(baseBitmapImage)
        }
        fun draw(){
            // draw 직전에 texImage2D를 호출하여 적용할 bitmap를 확정
            cube.changeImage(dayBitmapImage)
            cube.draw(vPMatrix)
        }

        fun resetMatrix(){
            updateLocateInDayCubes()
            val horizontalRatio = horizontalNum.toFloat() / horizontalSize
            val verticalRatio = verticalNum.toFloat() / verticalSize // 0 ~ 1 사이에 위치
            // x: -0.9 ~ 0.9 사이에 위치, y: -0.9 ~ 0.9 사이에 위치, z: -1.1f ~ 0f 사이에 위치
            locateX = horizontalRatio * 1.8f - 0.9f
            locateY = -(verticalRatio * 1.8f - 0.9f) + additionalY
            locateZ = verticalRatio * 1.1f - 1.1f + additionalZ

            Matrix.perspectiveM(projectionMatrix, 0, 80f, aspectRatio, 1f, 10f)
            Matrix.setLookAtM(
                viewMatrix, 0,
                0.0f, 0.0f, 2.0f,
                0.0f, 0.0f, 0.0f,
                0.0f, 1.0f, 0.0f
            )
            // mvp = p * v * m (곱하는 순서 중요함)
            Matrix.multiplyMM(vPMatrix, 0, projectionMatrix, 0, viewMatrix, 0)
            Matrix.multiplyMM(vPMatrix, 0, projectionMatrix, 0, viewMatrix, 0)

            translate(locateX, locateY, locateZ)
            rotate(-50f, 0f, 0f)
        }

        fun moveZToFront(){
            resetMatrix()
            translate(0f, 0f, 0.1f)
        }

        fun resetZ(){
            resetMatrix()
        }

        fun translate(dx:Float, dy:Float, dz: Float){
            Matrix.translateM(vPMatrix, 0, dx, dy, dz)
        }

        fun rotate(dx:Float, dy:Float, dz: Float){
            Matrix.rotateM(vPMatrix, 0, dx * aspectRatio, 1f, 0f, 0f)
            Matrix.rotateM(vPMatrix, 0, dy * aspectRatio, 0f, 1f, 0f)
            Matrix.rotateM(vPMatrix, 0, dz * aspectRatio, 0f, 0f, 1f)
        }
    }

    init{
        // 현재 날짜가 중앙에 오도록 달력 설정
        calendar.time = Date(System.currentTimeMillis())
        calendar.add(Calendar.WEEK_OF_YEAR, - verticalSize / 2)
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        calendar.add(Calendar.DATE, -dayOfWeek + 1)

        calendarListener?.onShowingYearMonthUpdate(yearMonthFormat.format(calendar.time))

        for(vNum in 0 until verticalSize){
            dayCubes.addLast(mutableListOf())
            for(hNum in 0 until horizontalSize){
                val dayCube = DayCube(calendar.time)
                Matrix.setIdentityM(dayCube.vPMatrix, 0)
                Matrix.setIdentityM(dayCube.projectionMatrix, 0)
                Matrix.setIdentityM(dayCube.viewMatrix, 0)
                dayCubes[vNum].add(dayCube)

                calendar.add(Calendar.DATE, 1)
            }
        }
        calendar.clear()
        resetAllMatrix()
    }


    override fun onSurfaceCreated(unused: GL10, config: EGLConfig) {
        GLES20.glClearDepthf(1.0f)
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)
        GLES20.glDepthFunc(GLES20.GL_LEQUAL)
        for(vNum in 0 until verticalSize){
            for(hNum in 0 until horizontalSize) {
                // 여러 개의 객체에 대해 draw 없이 readyToDraw만 반복하면 모든 객체가 마지막으로 readyToDraw를 호출한 객체와 같게 적용되는 문제가 생김
                dayCubes[vNum][hNum].readyToDraw()
            }
        }
    }

    override fun onDrawFrame(unused: GL10) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT) // 없으면 움직일 때마다 잔상 남음
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT)

        for(vNum in 0 until verticalSize){
            for(hNum in 0 until horizontalSize) {
                // readyToDraw를 onDrawFrame에서 실행 시 오류
                dayCubes[vNum][hNum].draw()
            }
        }
    }

    override fun onSurfaceChanged(unused: GL10, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height) // 뷰포트를 전체화면으로 맞춤
        viewPort = intArrayOf(0, 0, width, height)
        aspectRatio = width.toFloat() / height // 가로 세로 비율을 구함
        resetAllMatrix()
    }

    fun resetAllMatrix(){
        for(vNum in 0 until verticalSize){
            for(hNum in 0 until horizontalSize) {
                dayCubes[vNum][hNum].resetMatrix()
            }
        }
    }

    // 선택한 모뎉을 앞으로 나오게 함
    fun selectTouchedDate(touchX: Float, touchY: Float){
        var mostNearModel: DayCube? = null
        var mostNearDistance = POSITIVE_INFINITY

        for(vNum in 0 until verticalSize){
            for(hNum in 0 until horizontalSize) {
                val curModel = dayCubes[vNum][hNum]

                val realLocate = floatArrayOf(0.0f, 0.0f, 0.0f, 0.0f)

                // 객체의 중심점을 화면에 보여지는 위치로 변환
                val result = GLU.gluProject(curModel.locateX, -curModel.locateY, curModel.locateZ,
                    curModel.viewMatrix, 0, curModel.projectionMatrix, 0,
                    viewPort, 0, realLocate, 0
                )


                // 실제 좌표와 가장 가까운 모델을 구함
                if (result == GLES20.GL_TRUE) {
                    val curDistance =
                        (touchX - realLocate[0]).pow(2) + (touchY - realLocate[1]).pow(2)
                    if (curDistance < mostNearDistance) {
                        mostNearDistance = curDistance
                        mostNearModel = curModel
                    }
                }
            }
        }

        for(vNum in 0 until verticalSize) {
            for (hNum in 0 until horizontalSize) {
                val curModel = dayCubes[vNum][hNum]
                if(curModel == mostNearModel){
                    curModel.moveZToFront()
                    calendarListener?.onSelectedDateUpdate(curModel.date)
                }
                else{
                    curModel.resetZ()
                }
            }
        }

    }

    // 위, 아래로 드래그하면 이전 달, 다음 달이 표시됨
    fun dragY(movedY: Float){
        additionalY += -movedY / 1000
        additionalZ += movedY / 1000

        if(additionalY > 0.2){

            val verticalFirstDayCubes = dayCubes.first()
            for(dayCube in verticalFirstDayCubes){
                calendar.time = dayCubes[verticalSize - 1][dayCube.horizontalNum].date
                calendar.add(Calendar.DATE, 7)
                dayCube.setCurDate(calendar.time)
                calendar.clear()
                dayCube.draw()
            }
            dayCubes.removeFirst()
            dayCubes.addLast(verticalFirstDayCubes)
        }
        else if(additionalY < -0.2){
            val verticalLastDayCubes = dayCubes.last()
            for(dayCube in verticalLastDayCubes){
                calendar.time = dayCubes[0][dayCube.horizontalNum].date
                calendar.add(Calendar.DATE, -7)
                dayCube.setCurDate(calendar.time)
                calendar.clear()
                dayCube.draw()
            }
            dayCubes.removeLast()
            dayCubes.addFirst(verticalLastDayCubes)
        }
        resetAllMatrix()
        calendarListener?.onShowingYearMonthUpdate(yearMonthFormat.format(dayCubes[verticalSize / 2][0].date))

        if(abs(additionalY) > 0.2){
            additionalY = 0f
            additionalZ = 0f
        }
    }
}