package com.choi.sensorproject.ui.opngl

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import com.example.sensorproject.R
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10


class CustomGLRenderer(val context: Context): GLSurfaceView.Renderer {

    private lateinit var mTriangle : Triangle
    private lateinit var mPhone2D: Phone2D
    private lateinit var mCube: Cube
    private lateinit var mPhone: Phone
    private lateinit var mPin: Pin

    private val phoneVPMatrix = FloatArray(16)
    private val phoneProjectionMatrix = FloatArray(16)
    private val phoneViewMatrix = FloatArray(16)

    private val pinVPMatrix = FloatArray(16)
    private val pinProjectionMatrix = FloatArray(16)
    private val pinViewMatrix = FloatArray(16)

    private var aspectRatio: Float = 0f

    private var appPlayingImage: Bitmap? = null
    private var appIcon: Bitmap? = null

    init{
        Matrix.setIdentityM(phoneProjectionMatrix, 0)
        Matrix.setIdentityM(phoneViewMatrix, 0)
        Matrix.setIdentityM(phoneVPMatrix, 0)

        Matrix.setIdentityM(pinProjectionMatrix, 0)
        Matrix.setIdentityM(pinViewMatrix, 0)
        Matrix.setIdentityM(pinVPMatrix, 0)
    }


    override fun onSurfaceCreated(unused: GL10, config: EGLConfig) {
        GLES20.glClearDepthf(1.0f)
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)
        GLES20.glDepthFunc(GLES20.GL_LEQUAL)
        mPhone2D = Phone2D()
        val basePhoneImage = BitmapFactory.decodeResource(context.getResources(), R.drawable.phone)
        mPhone2D.readyToDraw(basePhoneImage)

        mPin = Pin()
        mPin.readyToDraw(basePhoneImage)
    }

    override fun onDrawFrame(unused: GL10) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT) // 없으면 움직일 때마다 잔상 남음
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT)

        // 실행 중이었던 앱 별 미리 설정해 둔 이미지를 띄움 (없을 경우 기본 이미지)
        if(appPlayingImage != null){
            mPhone2D.changeImage(appPlayingImage!!)
        }
        else{
            val basePhoneImage = BitmapFactory.decodeResource(context.getResources(), R.drawable.phone)
            mPhone2D.changeImage(basePhoneImage)
        }
        mPhone2D.draw(phoneVPMatrix)

        // 실행 중이었던 앱 별 미리 설정해 둔 이미지를 띄움 (없을 경우 기본 이미지)
        if(appIcon != null){
            mPin.changeImage(appIcon!!)
        }
        else{
            val basePhoneImage = BitmapFactory.decodeResource(context.getResources(), R.drawable.phone)
            mPin.changeImage(basePhoneImage)
        }
        mPin.draw(pinVPMatrix)
    }

    override fun onSurfaceChanged(unused: GL10, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height) // 뷰포트를 전체화면으로 맞춤
        aspectRatio = width.toFloat() / height // 가로 세로 비율을 구함
        resetPhoneMatrix()
        resetPinMatrix()
    }

    fun rotatePhone(dx:Float, dy:Float){
        Matrix.rotateM(phoneVPMatrix, 0, dx * aspectRatio, 1f, 0f, 0f)
        Matrix.rotateM(phoneVPMatrix, 0, dy * aspectRatio, 0f, 1f, 0f)
    }
    fun rotatePhone(dx:Float, dy:Float, dz: Float){
        Matrix.rotateM(phoneVPMatrix, 0, dx * aspectRatio, 1f, 0f, 0f)
        Matrix.rotateM(phoneVPMatrix, 0, dy * aspectRatio, 0f, 1f, 0f)
        Matrix.rotateM(phoneVPMatrix, 0, dz * aspectRatio, 0f, 0f, 1f)
    }

    fun translatePin(dx:Float, dy:Float, dz: Float){
        Matrix.translateM(pinVPMatrix, 0, dx, dy, dz)
    }

    fun resetPhoneMatrix(){
        Matrix.perspectiveM(phoneProjectionMatrix, 0, 60f, aspectRatio, 1f, 7f)
        Matrix.setLookAtM(
            phoneViewMatrix, 0,
            0.0f, 0.0f, 2.5f,
            0.0f, 0.0f, 0.0f,
            0.0f, 1.0f, 0.0f
        )
        // mvp = p * v * m (곱하는 순서 중요함)
        Matrix.multiplyMM(phoneVPMatrix, 0, phoneProjectionMatrix, 0, phoneViewMatrix, 0)
    }

    fun resetPinMatrix(){
        Matrix.multiplyMM(pinVPMatrix, 0, pinProjectionMatrix, 0, pinViewMatrix, 0)
    }

    // 이 함수로 appPlayingImage를 변경해 두면 실시간으로 작동하는 onDrawFrame에서 적용할 수 있음
    fun changeAppPlayingImage(appPlayingImage: Bitmap?){
        appPlayingImage?.let{
            // 가로가 세로보다 길 경우 90도 회전하여 출력
            if(it.width > it.height){
                val matrix = android.graphics.Matrix()
                matrix.setRotate(90f)
                val rotatedAppPlayingImage = Bitmap.createBitmap(it, 0, 0, it.width, it.height, matrix, true)
                this.appPlayingImage = rotatedAppPlayingImage
                return
            }
        }
       this.appPlayingImage = appPlayingImage
    }

    fun changeAppIcon(appIcon: Bitmap?){
        this.appIcon = appIcon
    }
}