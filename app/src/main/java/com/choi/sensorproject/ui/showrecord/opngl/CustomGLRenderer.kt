package com.choi.sensorproject.ui.showrecord.opngl

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

    private lateinit var mPhoneFront2D: Phone2D
    private lateinit var mPhoneBack2D: Phone2D
    private lateinit var mPin: Pin

    private val phoneFrontVPMatrix = FloatArray(16)
    private val phoneBackVPMatrix = FloatArray(16)
    private val phoneProjectionMatrix = FloatArray(16)
    private val phoneViewMatrix = FloatArray(16)

    private val pinVPMatrix = FloatArray(16)
    private val pinProjectionMatrix = FloatArray(16)
    private val pinViewMatrix = FloatArray(16)

    private var aspectRatio: Float = 0f

    private var appPlayingImage: Bitmap? = null
    private var appIcon: Bitmap? = null

    val baseFrontPhoneImage = BitmapFactory.decodeResource(context.getResources(), R.drawable.phone)
    val baseBackPhoneImage = BitmapFactory.decodeResource(context.getResources(), R.drawable.phone_back)

    init{
        Matrix.setIdentityM(phoneProjectionMatrix, 0)
        Matrix.setIdentityM(phoneViewMatrix, 0)
        Matrix.setIdentityM(phoneFrontVPMatrix, 0)
        Matrix.setIdentityM(phoneBackVPMatrix, 0)

        Matrix.setIdentityM(pinProjectionMatrix, 0)
        Matrix.setIdentityM(pinViewMatrix, 0)
        Matrix.setIdentityM(pinVPMatrix, 0)
    }


    override fun onSurfaceCreated(unused: GL10, config: EGLConfig) {
        GLES20.glClearDepthf(1.0f)
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)
        GLES20.glDepthFunc(GLES20.GL_LEQUAL)

        mPhoneFront2D = Phone2D(true)
        mPhoneFront2D.readyToDraw(baseFrontPhoneImage)

        mPhoneBack2D = Phone2D(false)
        mPhoneBack2D.readyToDraw(baseBackPhoneImage)

        mPin = Pin()
        mPin.readyToDraw(baseFrontPhoneImage)
    }

    override fun onDrawFrame(unused: GL10) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT) // 없으면 움직일 때마다 잔상 남음
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT)

        // 실행 중이었던 앱 별 미리 설정해 둔 이미지를 띄움 (없을 경우 기본 이미지)
        if(appPlayingImage != null){
            mPhoneFront2D.changeImage(appPlayingImage!!)
        }
        else{
            // 이곳에서 BitmapFactory.decodeResource를 하면 속도가 느려짐
            mPhoneFront2D.changeImage(baseFrontPhoneImage)
        }
        mPhoneFront2D.draw(phoneFrontVPMatrix)

        // 이미지가 바뀌지 않더라도 mPhoneFront2D에서 바꾼 이미지가 적용되지 않도록 재설정해야함 (GLES20을 공유하기 때문)
        mPhoneBack2D.changeImage(baseBackPhoneImage)
        mPhoneBack2D.draw(phoneBackVPMatrix)

        // 실행 중이었던 앱 별 미리 설정해 둔 이미지를 띄움 (없을 경우 기본 이미지)
        if(appIcon != null){
            mPin.changeImage(appIcon!!)
        }
        else{
            mPin.changeImage(baseFrontPhoneImage)
        }
        mPin.draw(pinVPMatrix)
    }

    override fun onSurfaceChanged(unused: GL10, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height) // 뷰포트를 전체화면으로 맞춤
        aspectRatio = width.toFloat() / height // 가로 세로 비율을 구함
        resetPhoneMatrix()
        resetPinMatrix()
    }

    fun rotatePhone(dx:Float, dy:Float, dz: Float){
        Matrix.rotateM(phoneFrontVPMatrix, 0, dx * aspectRatio, 1f, 0f, 0f)
        Matrix.rotateM(phoneFrontVPMatrix, 0, dy * aspectRatio, 0f, 1f, 0f)
        Matrix.rotateM(phoneFrontVPMatrix, 0, dz * aspectRatio, 0f, 0f, 1f)
        Matrix.rotateM(phoneBackVPMatrix, 0, dx * aspectRatio, 1f, 0f, 0f)
        Matrix.rotateM(phoneBackVPMatrix, 0, dy * aspectRatio, 0f, 1f, 0f)
        Matrix.rotateM(phoneBackVPMatrix, 0, dz * aspectRatio, 0f, 0f, 1f)
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
        Matrix.multiplyMM(phoneFrontVPMatrix, 0, phoneProjectionMatrix, 0, phoneViewMatrix, 0)
        Matrix.multiplyMM(phoneBackVPMatrix, 0, phoneProjectionMatrix, 0, phoneViewMatrix, 0)
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