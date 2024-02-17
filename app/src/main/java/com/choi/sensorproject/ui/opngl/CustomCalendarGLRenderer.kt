package com.choi.sensorproject.ui.opngl

import android.R.attr.height
import android.R.attr.width
import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.GLU
import android.opengl.Matrix
import com.example.sensorproject.R
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.Float.Companion.POSITIVE_INFINITY
import kotlin.math.abs
import kotlin.math.pow


class CustomCalendarGLRenderer(val context: Context, val resources: Resources): GLSurfaceView.Renderer {

    private val dayCubeCount = 70

    private var aspectRatio: Float = 0f

    private val dayCubes: MutableList<DayCube> = mutableListOf()

    private var viewPort = intArrayOf(0, 0, 0, 0)

    private val baseBitmapImage = BitmapFactory.decodeResource(context.getResources(), R.drawable.calendar_tile)

    inner class DayCube(val locateX: Float, val locateY :Float, var locateZ: Float, val dayNumber: Int){
        val vPMatrix = FloatArray(16)
        val projectionMatrix = FloatArray(16)
        val viewMatrix = FloatArray(16)
        var finalBitmapImage: Bitmap
        val cube = Pin()
        val transparency = 255 - (abs(dayNumber - dayCubeCount / 2) * 1.9f / dayCubeCount * 255).toInt()

        init{
            // canvas에 이미지를 그리고 텍스트를 추가하는 작업을 미리 수행
            finalBitmapImage =
                Bitmap.createBitmap(baseBitmapImage.getWidth(), baseBitmapImage.getHeight(), baseBitmapImage.getConfig())
            val canvas = Canvas(finalBitmapImage)
            canvas.drawBitmap(baseBitmapImage, 0f, 0f, Paint().apply {
                alpha = transparency
            })
            val dayNumberPaint = Paint().apply {
                color = Color.parseColor("#20B2AA")
                typeface = resources.getFont(R.font.godo_m)
                strokeWidth = 1f
                style = Paint.Style.FILL
                textSize = 200f
                alpha = transparency
            }

            val dayNumberTextBounds = Rect()
            dayNumberPaint.getTextBounds(dayNumber.toString(), 0, dayNumber.toString().length, dayNumberTextBounds)
            val dayNumberTextX = finalBitmapImage.getWidth()/2f - dayNumberTextBounds.width() / 2

            canvas.drawText(dayNumber.toString(),dayNumberTextX , finalBitmapImage.getHeight()/2f, dayNumberPaint)
        }

        fun readyToDraw(){
            // readyToDraw 단계에서 적용할 bitmap를 확정하면 다수의 객체를 그릴 때 마지막 객체에 적용할 bitmap이 전체 객체에 적용되는 문제가 생김
            cube.readyToDraw(baseBitmapImage)
        }
        fun draw(){
            // draw 직전에 texImage2D를 호출하여 적용할 bitmap를 확정
            cube.changeImage(finalBitmapImage)
            cube.draw(vPMatrix)
        }

        fun resetMatrix(){
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
        for(n in 0 until dayCubeCount){
            val dayOfWeek = (n % 7).toFloat() / 7
            val week = (n / 7).toFloat() / (dayCubeCount / 7)
            // x: -0.9 ~ 0.9 사이에 위치, y: -0.9 ~ 0.9 사이에 위치
            val dayCube = DayCube(dayOfWeek * 1.8f - 0.9f, -(week * 1.8f - 0.9f), week * 1.1f - 1f, n)
            Matrix.setIdentityM(dayCube.vPMatrix, 0)
            Matrix.setIdentityM(dayCube.projectionMatrix, 0)
            Matrix.setIdentityM(dayCube.viewMatrix, 0)
            dayCube.resetMatrix()
            dayCubes.add(dayCube)
        }
    }


    override fun onSurfaceCreated(unused: GL10, config: EGLConfig) {
        GLES20.glClearDepthf(1.0f)
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)
        GLES20.glDepthFunc(GLES20.GL_LEQUAL)
        for(n in 0 until dayCubeCount){
            // 여러 개의 객체에 대해 draw 없이 readyToDraw만 반복하면 모든 객체가 마지막으로 readyToDraw를 호출한 객체와 같게 적용되는 문제가 생김
            dayCubes[n].readyToDraw()
        }
    }

    override fun onDrawFrame(unused: GL10) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT) // 없으면 움직일 때마다 잔상 남음
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT)

        for(n in 0 until dayCubeCount){
            // readyToDraw를 onDrawFrame에서 실행 시 오류
            dayCubes[n].draw()
        }
    }

    override fun onSurfaceChanged(unused: GL10, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height) // 뷰포트를 전체화면으로 맞춤
        viewPort = intArrayOf(0, 0, width, height)
        aspectRatio = width.toFloat() / height // 가로 세로 비율을 구함
        resetAllMatrix()
    }

    fun resetAllMatrix(){
        for(n in 0 until dayCubeCount){
            dayCubes[n].resetMatrix()
        }
    }

    // 선택한 모뎉을 앞으로 나오게 함
    fun selectTouchedDate(touchX: Float, touchY: Float){
        var mostNearModel: DayCube? = null
        var mostNearDistance = POSITIVE_INFINITY

        for(n in 0 until dayCubeCount) {
            val curModel = dayCubes[n]

            val realLocate = floatArrayOf(0.0f, 0.0f, 0.0f, 0.0f)
//            val result = GLU.gluUnProject(
//                touchX, touchY, 0.2f, curModel.viewMatrix, 0, curModel.projectionMatrix, 0, viewPort, 0,
//                realLocate, 0
//            )

            // 객체의 중심점을 화면에 보여지는 위치로 변환
            val result = GLU.gluProject(
                curModel.locateX, -curModel.locateY, curModel.locateZ, curModel.viewMatrix, 0, curModel.projectionMatrix, 0, viewPort, 0,
                realLocate, 0
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
        for(n in 0 until dayCubeCount) {
            val curModel = dayCubes[n]
            if(curModel == mostNearModel){
                curModel.moveZToFront()
            }
            else{
                curModel.resetZ()
            }
        }
    }

}