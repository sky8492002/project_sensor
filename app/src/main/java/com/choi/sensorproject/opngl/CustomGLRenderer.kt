package com.choi.sensorproject.opngl

import android.R.attr.bitmap
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import com.example.sensorproject.R
import java.io.FileInputStream
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10


class CustomGLRenderer(val context: Context): GLSurfaceView.Renderer {

    private lateinit var mTriangle : Triangle
    private lateinit var mSquare: Square
    private lateinit var mCube: Cube
    private lateinit var mPhone: Phone

    private val vPMatrix = FloatArray(16)
    private val projectionMatrix = FloatArray(16)
    private val viewMatrix = FloatArray(16)

    private var aspectRatio: Float = 0f

    init{
        Matrix.setIdentityM(projectionMatrix, 0)
        Matrix.setIdentityM(viewMatrix, 0)
        Matrix.setIdentityM(vPMatrix, 0)
    }


    override fun onSurfaceCreated(unused: GL10, config: EGLConfig) {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)
        GLES20.glClearDepthf(1.0f);
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)
        GLES20.glDepthFunc(GLES20.GL_LEQUAL)
        mSquare = Square()
        //mCube = Cube()
        mPhone = Phone()

        //val bitmapImage = getDrawable(context, R.drawable.medal)!!.toBitmap()
        val bitmapImage = BitmapFactory.decodeResource(context.getResources(), R.drawable.test)
        //val bitmapImage = BitmapFactory.decodeStream(context.assets.open("test.png"))
        mSquare.readyToDraw(bitmapImage)
    }

    override fun onDrawFrame(unused: GL10) {
        // Redraw background color
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT)
        mSquare.draw(vPMatrix)
        //mCube.draw(vPMatrix)
        //mPhone.draw(vPMatrix)
    }

    override fun onSurfaceChanged(unused: GL10, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height) // 뷰포트를 전체화면으로 맞춤

        // 보는 시점 설정
        aspectRatio = width.toFloat() / height // 가로 세로 비율을 구함

        Matrix.perspectiveM(projectionMatrix, 0, 60f, aspectRatio, 1f, 7f)
        Matrix.setLookAtM(
            viewMatrix, 0,
            0.0f, 0.0f, -2.0f,
            0.0f, 0.0f, 0.0f,
            0.0f, 1.0f, 0.0f
        )
        // mvp = p * v * m (곱하는 순서 중요함)
        Matrix.multiplyMM(vPMatrix, 0, projectionMatrix, 0, viewMatrix, 0)
    }

    fun rotate(dx:Float, dy:Float){
        Matrix.rotateM(vPMatrix, 0, dx * aspectRatio, 0f, 1f, 0f)
        Matrix.rotateM(vPMatrix, 0, dy * aspectRatio, 1f, 0f, 0f)
    }
}